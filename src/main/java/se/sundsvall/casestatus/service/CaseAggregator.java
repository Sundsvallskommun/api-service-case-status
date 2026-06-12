package se.sundsvall.casestatus.service;

import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.supportmanagement.Errand;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.configuration.AsyncConfig;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.OpenEMapper;
import se.sundsvall.casestatus.service.mapper.SupportManagementMapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;

/**
 * Aggregates {@link CaseStatusResponse} entries across all backing systems (CaseManagement, OeP, SupportManagement and
 * the local Open-E cache) for a given party or organization. The same pipeline — parallel fetch, multi-sign override,
 * OPEN_E_PLATFORM duplicate filter and optional draft filter — is applied to both flows.
 */
@Component
public class CaseAggregator {

	private static final Set<String> DRAFT_STATUSES = Set.of("utkast");

	private final PartyIntegration partyIntegration;
	private final CaseManagementIntegration caseManagementIntegration;
	private final CaseManagementMapper caseManagementMapper;
	private final OepIntegratorClient oepIntegratorClient;
	private final OpenEMapper openEMapper;
	private final CaseRepository caseRepository;
	private final SupportManagementService supportManagementService;
	private final SupportManagementMapper supportManagementMapper;
	private final StatusVocabulary statusVocabulary;
	private final Executor mdcAwareExecutor;

	public CaseAggregator(final PartyIntegration partyIntegration,
		final CaseManagementIntegration caseManagementIntegration,
		final CaseManagementMapper caseManagementMapper,
		final OepIntegratorClient oepIntegratorClient,
		final OpenEMapper openEMapper,
		final CaseRepository caseRepository,
		final SupportManagementService supportManagementService,
		final SupportManagementMapper supportManagementMapper,
		final StatusVocabulary statusVocabulary,
		final @Qualifier(AsyncConfig.MDC_EXECUTOR) Executor mdcAwareExecutor) {
		this.partyIntegration = partyIntegration;
		this.caseManagementIntegration = caseManagementIntegration;
		this.caseManagementMapper = caseManagementMapper;
		this.oepIntegratorClient = oepIntegratorClient;
		this.openEMapper = openEMapper;
		this.caseRepository = caseRepository;
		this.supportManagementService = supportManagementService;
		this.supportManagementMapper = supportManagementMapper;
		this.statusVocabulary = statusVocabulary;
		this.mdcAwareExecutor = mdcAwareExecutor;
	}

	public List<CaseStatusResponse> aggregateForParty(final String partyId, final String municipalityId, final boolean includeDrafts) {
		final var cmFuture = caseManagementByPartyAsync(partyId, municipalityId);
		final var oepFuture = oepByPartyAsync(partyId, municipalityId);
		final var multisignFuture = oepMultisignByPartyAsync(partyId, municipalityId);

		// SupportManagement stores partyId in stakeholders.externalId for both private persons and enterprises
		final var supportFuture = supportManagementByExternalIdAsync(partyId, municipalityId);

		return runPipeline(List.of(cmFuture, oepFuture, supportFuture), multisignFuture, includeDrafts);
	}

	public List<CaseStatusResponse> aggregateForOrg(final String organizationNumber, final String municipalityId) {
		final var cmFuture = caseManagementByOrgAsync(organizationNumber, municipalityId);
		final var localOpenEFuture = localOpenEByOrgAsync(organizationNumber, municipalityId);
		final var supportFuture = supportManagementByOrganizationNumberAsync(organizationNumber, municipalityId);

		// Org flow has no multi-sign source and preserves the historical "no draft filtering" behavior.
		return runPipeline(List.of(cmFuture, localOpenEFuture, supportFuture), CompletableFuture.completedFuture(emptyList()), true);
	}

	/**
	 * Joins all primary source futures, removes any regular entry that is shadowed by a multi-sign entry for the same
	 * flowInstanceId, applies the OPEN_E_PLATFORM duplicate + draft filter, then appends the multi-sign entries last so
	 * they bypass both filters.
	 */
	private List<CaseStatusResponse> runPipeline(final List<CompletableFuture<List<CaseStatusResponse>>> primarySources, final CompletableFuture<List<CaseStatusResponse>> multisignSource, final boolean includeDrafts) {

		final var multisignStatuses = multisignSource.join();
		final var multisignIds = multisignStatuses.stream()
			.map(CaseStatusResponse::getExternalCaseId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		final var primaryStatuses = primarySources.stream()
			.map(CompletableFuture::join)
			.flatMap(List::stream)
			.filter(response -> isNotOverriddenByMultisign(response, multisignIds))
			.toList();

		return Stream.concat(filterResponses(primaryStatuses, includeDrafts).stream(), multisignStatuses.stream())
			.toList();
	}

	/**
	 * Returns true when a regular response should be kept, i.e. it is not shadowed by a multi-sign entry for the same
	 * flow instance. Multi-sign cases take precedence because they represent an actionable state (awaiting signature) the
	 * consumer must surface. Responses without an externalCaseId cannot be matched as duplicates and are always kept.
	 */
	private boolean isNotOverriddenByMultisign(final CaseStatusResponse response, final Set<String> multisignIds) {
		if (response.getExternalCaseId() == null) {
			return true;
		}
		return !multisignIds.contains(response.getExternalCaseId());
	}

	/**
	 * Removes drafts (when {@code includeDrafts} is false) and removes the OPEN_E_PLATFORM copy when another system
	 * returned the same externalCaseId. Responses with a null externalCaseId are never treated as duplicates.
	 */
	List<CaseStatusResponse> filterResponses(final List<CaseStatusResponse> responses, final boolean includeDrafts) {
		if (responses == null) {
			return emptyList();
		}

		final var filterDrafts = draftFilter(includeDrafts);

		final var nullExternalCaseIdStream = responses.stream()
			.filter(response -> response.getExternalCaseId() == null)
			.filter(filterDrafts);

		final var filteredStream = responses.stream()
			.filter(response -> response.getExternalCaseId() != null)
			.filter(filterDrafts)
			.collect(groupingBy(CaseStatusResponse::getExternalCaseId))
			.entrySet().stream()
			.flatMap(entry -> dropOpenEDuplicates(entry.getValue()));

		return Stream.concat(nullExternalCaseIdStream, filteredStream)
			.toList();
	}

	private Stream<CaseStatusResponse> dropOpenEDuplicates(final List<CaseStatusResponse> entries) {
		if (entries.size() > 1 && entries.stream().anyMatch(response -> OPEN_E_PLATFORM.equals(response.getSystem()))) {
			return entries.stream().filter(response -> !OPEN_E_PLATFORM.equals(response.getSystem()));
		}
		return entries.stream();
	}

	private Predicate<CaseStatusResponse> draftFilter(final boolean includeDrafts) {
		return response -> includeDrafts || !DRAFT_STATUSES.contains(ofNullable(response.getStatus()).orElse("").toLowerCase());
	}

	private CompletableFuture<List<CaseStatusResponse>> caseManagementByPartyAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.toList(), mdcAwareExecutor);
	}

	private CompletableFuture<List<CaseStatusResponse>> caseManagementByOrgAsync(final String organizationNumber, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> caseManagementIntegration.getCaseStatusForOrganizationNumber(organizationNumber, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.toList(), mdcAwareExecutor);
	}

	private CompletableFuture<List<CaseStatusResponse>> oepByPartyAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> oepIntegratorClient.getCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId, true).stream()
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList(), mdcAwareExecutor);
	}

	private CompletableFuture<List<CaseStatusResponse>> oepMultisignByPartyAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> oepIntegratorClient.getMultisignCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId, true).stream()
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList(), mdcAwareExecutor);
	}

	private CompletableFuture<List<CaseStatusResponse>> localOpenEByOrgAsync(final String organizationNumber, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> caseRepository.findByOrganisationNumberAndMunicipalityId(organizationNumber, municipalityId).stream()
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList(), mdcAwareExecutor);
	}

	private CompletableFuture<List<CaseStatusResponse>> supportManagementByExternalIdAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> mapSupportManagementErrands(municipalityId, supportManagementService.getSupportManagementCasesByExternalId(municipalityId, partyId)),
			mdcAwareExecutor);
	}

	/**
	 * SupportManagement stores partyId (not organization number) in stakeholders.externalId, so the organization number
	 * must be translated via Party before searching. An organization number unknown to Party yields no SupportManagement
	 * matches while the other sources still contribute.
	 */
	private CompletableFuture<List<CaseStatusResponse>> supportManagementByOrganizationNumberAsync(final String organizationNumber, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> partyIntegration.getPartyIdByOrganizationNumber(municipalityId, organizationNumber)
			.map(partyId -> mapSupportManagementErrands(municipalityId, supportManagementService.getSupportManagementCasesByExternalId(municipalityId, partyId)))
			.orElse(emptyList()), mdcAwareExecutor);
	}

	public List<CaseStatusResponse> mapSupportManagementErrands(final String municipalityId, final Map<String, List<Errand>> errandsByNamespace) {
		return errandsByNamespace.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(errand -> supportManagementMapper.toCaseStatusResponse(
					errand,
					entry.getKey(),
					statusVocabulary.lookupBySupportManagementStatus(errand.getStatus()),
					supportManagementService.getClassificationDisplayName(municipalityId, entry.getKey(), errand))))
			.toList();
	}
}
