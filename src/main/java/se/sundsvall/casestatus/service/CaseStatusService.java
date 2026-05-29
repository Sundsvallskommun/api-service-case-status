package se.sundsvall.casestatus.service;

import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.supportmanagement.Errand;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.configuration.AsyncConfig;
import se.sundsvall.casestatus.integration.casedata.CaseDataIntegration;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.OpenEMapper;
import se.sundsvall.casestatus.service.mapper.SupportManagementMapper;
import se.sundsvall.dept44.problem.Problem;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StringUtils.hasText;
import static se.sundsvall.casestatus.util.Constants.CASE_NOT_FOUND;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.FormattingUtil.getFormattedOrganizationNumber;

@Service
public class CaseStatusService {

	private static final Set<String> DRAFT_STATUSES = Set.of("utkast"); // Draft statuses that should be filtered out.

	private final CaseManagementIntegration caseManagementIntegration;
	private final OepIntegratorClient oepIntegratorClient;
	private final CaseRepository caseRepository;
	private final PartyIntegration partyIntegration;
	private final SupportManagementService supportManagementService;

	private final CaseManagementMapper caseManagementMapper;
	private final CaseDataIntegration caseDataIntegration;
	private final SupportManagementMapper supportManagementMapper;
	private final OpenEMapper openEMapper;
	private final StatusVocabulary statusVocabulary;
	private final Executor mdcAwareExecutor;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository,
		final PartyIntegration partyIntegration,
		final SupportManagementService supportManagementService,
		final CaseManagementMapper caseManagementMapper,
		final CaseDataIntegration caseDataIntegration,
		final SupportManagementMapper supportManagementMapper,
		final OpenEMapper openEMapper,
		final StatusVocabulary statusVocabulary,
		final @Qualifier(AsyncConfig.MDC_EXECUTOR) Executor mdcAwareExecutor) {

		this.caseManagementIntegration = caseManagementIntegration;
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseRepository = caseRepository;
		this.partyIntegration = partyIntegration;
		this.supportManagementService = supportManagementService;
		this.caseManagementMapper = caseManagementMapper;
		this.caseDataIntegration = caseDataIntegration;
		this.supportManagementMapper = supportManagementMapper;
		this.openEMapper = openEMapper;
		this.statusVocabulary = statusVocabulary;
		this.mdcAwareExecutor = mdcAwareExecutor;
	}

	public OepStatusResponse getOepStatus(final String externalCaseId, final String municipalityId) {
		final var cmStatus = caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(CaseStatusDTO::getStatus)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));

		final var oepStatus = statusVocabulary.findOepStatusForCaseManagementStatus(cmStatus)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Could not find matching open-E status for status %s".formatted(cmStatus)));

		return openEMapper.toOepStatusResponse(oepStatus);
	}

	public CaseStatusResponse getCaseStatus(final String externalCaseId, final String municipalityId) {
		return caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.or(() -> caseRepository.findByFlowInstanceIdAndMunicipalityId(externalCaseId, municipalityId)
				.map(openEMapper::toCaseStatusResponse))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));
	}

	public CasePdfResponse getCasePdf(final String municipalityId, final String externalCaseId) {

		final var response = oepIntegratorClient.getCasePdfByFlowInstanceId(municipalityId, InstanceType.EXTERNAL, externalCaseId);
		final var body = response.getBody();

		if (response.getStatusCode().is4xxClientError() || body == null) {
			throw Problem.valueOf(NOT_FOUND, "Could not find PDF for case with externalCaseId %s".formatted(externalCaseId));
		}

		try {
			return openEMapper.toCasePdfResponse(externalCaseId, body);
		} catch (final IOException _) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to read PDF data");
		}

	}

	public List<CaseStatusResponse> getCaseStatuses(final String organizationNumber, final String municipalityId) {
		final List<CaseStatusResponse> statuses = new ArrayList<>();

		caseManagementIntegration.getCaseStatusForOrganizationNumber(organizationNumber, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		caseRepository.findByOrganisationNumberAndMunicipalityId(organizationNumber, municipalityId).stream()
			.map(openEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		getSupportManagementStatuses(getFormattedOrganizationNumber(organizationNumber), municipalityId, statuses);

		return statuses;
	}

	public List<CaseStatusResponse> getCaseStatusesForParty(final String partyId, final String municipalityId, boolean includeDrafts) {
		final var partyResult = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		final var cmFuture = getCaseManagementStatusesAsync(partyId, municipalityId);
		final var oepFuture = getOepStatusesAsync(partyId, municipalityId);
		final var multisignFuture = getOepMultisignStatusesAsync(partyId, municipalityId);

		// Support Management depends on the party type (PRIVATE -> partyId, ENTERPRISE -> formatted org no)
		final CompletableFuture<List<CaseStatusResponse>> supportFuture;
		if (partyResult.containsKey(PRIVATE)) {
			supportFuture = getSupportManagementStatusesAsync(partyId, municipalityId);
		} else if (partyResult.containsKey(ENTERPRISE)) {
			supportFuture = getSupportManagementStatusesAsync(getFormattedOrganizationNumber(partyResult.get(ENTERPRISE)), municipalityId);
		} else {
			return emptyList();
		}

		// Multi-sign cases take precedence over the regular OeP result for the same externalCaseId.
		final var multisignStatuses = multisignFuture.join();
		final var multisignIds = multisignStatuses.stream()
			.map(CaseStatusResponse::getExternalCaseId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		final var dedupedOepStatuses = oepFuture.join().stream()
			.filter(response -> isNotOverriddenByMultisign(response, multisignIds))
			.toList();

		final var regularStatuses = Stream.of(cmFuture.join(), dedupedOepStatuses, supportFuture.join())
			.flatMap(List::stream)
			.toList();

		// Multi-sign cases bypass both the duplicate-OeP filter and the draft filter — they are actionable items.
		return Stream.concat(filterResponses(regularStatuses, includeDrafts).stream(), multisignStatuses.stream())
			.toList();
	}

	private void getSupportManagementStatuses(final String partyId, final String municipalityId, final List<CaseStatusResponse> statuses) {
		supportManagementService.getSupportManagementCasesByExternalId(municipalityId, partyId)
			.forEach((namespace, errands) -> errands.stream()
				.map(errand -> supportManagementMapper.toCaseStatusResponse(
					errand,
					namespace,
					statusVocabulary.lookupBySupportManagementStatus(errand.getStatus()),
					getSupportManagementClassificationName(municipalityId, namespace, errand)))
				.forEach(statuses::add));
	}

	/**
	 * CaseStatusResponses are processed, and if there are duplicate externalCaseId's, some filtering is done. Responses
	 * with null externalCaseId's are not filtered. If there are multiple responses with the same externalCaseId, the ones
	 * with system
	 * OPEN_E_PLATFORM are removed.
	 *
	 * @param  responses     List of CaseStatusResponses
	 * @param  includeDrafts whether to include drafts in the result, or not.
	 * @return               Filtered list of CaseStatusResponses
	 */
	List<CaseStatusResponse> filterResponses(final List<CaseStatusResponse> responses, boolean includeDrafts) {

		if (responses == null) {
			return emptyList();
		}

		// Draft filter predicate (removes drafts, unless includeDrafts is true).
		final var filterDrafts = draftFilter(includeDrafts);

		final var nullExternalCaseIdStream = responses.stream()
			.filter(response -> response.getExternalCaseId() == null)
			.filter(filterDrafts);

		final var filteredStream = responses.stream()
			.filter(response -> response.getExternalCaseId() != null)
			.filter(filterDrafts)
			.collect(groupingBy(CaseStatusResponse::getExternalCaseId))
			.entrySet().stream()
			.flatMap(entry -> {
				final var entries = entry.getValue();
				if (entries.size() > 1 && entries.stream().anyMatch(response -> OPEN_E_PLATFORM.equals(response.getSystem()))) {
					return entries.stream()
						.filter(response -> !OPEN_E_PLATFORM.equals(response.getSystem()));
				}
				return entries.stream();
			});

		return Stream.concat(nullExternalCaseIdStream, filteredStream)
			.toList();
	}

	public List<CaseStatusResponse> getErrandStatuses(final String municipalityId, final String propertyDesignation, final String errandNumber) {
		if (hasText(propertyDesignation) && hasText(errandNumber)) {
			throw Problem.valueOf(BAD_REQUEST, "Both propertyDesignation and errandNumber cannot be provided at the same time");
		}
		if (propertyDesignation == null && errandNumber == null) {
			throw Problem.valueOf(BAD_REQUEST, "Either propertyDesignation or errandNumber must be provided");
		}

		if (hasText(propertyDesignation)) {
			return caseDataIntegration.getNamespaces().stream()
				.flatMap(namespace -> caseDataIntegration.getCaseDataCaseByPropertyDesignation(municipalityId, namespace, propertyDesignation).stream())
				.toList();
		}

		final var filterString = "errandNumber:'%s'".formatted(errandNumber);
		final var supportManagementCases = supportManagementService.getSupportManagementCases(municipalityId, filterString);
		final var caseStatusResponses = supportManagementCases.entrySet().stream()
			.flatMap(entry -> {
				final var namespace = entry.getKey();
				return entry.getValue().stream()
					.map(errand -> supportManagementMapper.toCaseStatusResponse(
						errand,
						namespace,
						statusVocabulary.lookupBySupportManagementStatus(errand.getStatus()),
						getSupportManagementClassificationName(municipalityId, namespace, errand)));
			})
			.toList();

		final var caseDataCases = caseDataIntegration.getNamespaces().stream()
			.flatMap(namespace -> caseDataIntegration.getCaseDataCaseByErrandNumber(municipalityId, namespace, errandNumber).stream())
			.toList();

		return Stream.of(caseStatusResponses, caseDataCases)
			.flatMap(List::stream)
			.toList();
	}

	private CompletableFuture<List<CaseStatusResponse>> getCaseManagementStatusesAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.toList(), mdcAwareExecutor);
	}

	private CompletableFuture<List<CaseStatusResponse>> getOepStatusesAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> oepIntegratorClient.getCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId, true).stream()
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList(), mdcAwareExecutor);
	}

	private CompletableFuture<List<CaseStatusResponse>> getOepMultisignStatusesAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> oepIntegratorClient.getMultisignCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId, true).stream()
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList(), mdcAwareExecutor);
	}

	/**
	 * Returns true when a regular OeP response should be kept, i.e. it is not shadowed by a multi-sign entry for the same
	 * flow instance. Multi-sign cases take precedence over the regular OeP listing because they represent an actionable
	 * state (awaiting signature) that the consumer must surface. Responses without an externalCaseId cannot be matched as
	 * duplicates and are always kept.
	 */
	private boolean isNotOverriddenByMultisign(final CaseStatusResponse response, final Set<String> multisignIds) {
		if (response.getExternalCaseId() == null) {
			return true;
		}
		return !multisignIds.contains(response.getExternalCaseId());
	}

	private CompletableFuture<List<CaseStatusResponse>> getSupportManagementStatusesAsync(final String externalIdOrOrgNo, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> supportManagementService
			.getSupportManagementCasesByExternalId(municipalityId, externalIdOrOrgNo)
			.entrySet()
			.stream()
			.flatMap(entry -> {
				final var namespace = entry.getKey();
				return entry.getValue().stream()
					.map(errand -> supportManagementMapper.toCaseStatusResponse(
						errand,
						namespace,
						statusVocabulary.lookupBySupportManagementStatus(errand.getStatus()),
						getSupportManagementClassificationName(municipalityId, namespace, errand)));
			})
			.toList(), mdcAwareExecutor);
	}

	private String getSupportManagementClassificationName(final String municipalityId, final String namespace, final Errand errand) {
		return supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand);
	}

	private Predicate<CaseStatusResponse> draftFilter(boolean includeDrafts) {
		return response -> includeDrafts || !DRAFT_STATUSES.contains(ofNullable(response.getStatus()).orElse("").toLowerCase());
	}
}
