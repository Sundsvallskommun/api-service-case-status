package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toCasePdfResponse;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toOepStatusResponse;
import static se.sundsvall.casestatus.util.Constants.CASE_NOT_FOUND;
import static se.sundsvall.casestatus.util.Constants.DEFAULT_EXTERNAL_STATUS;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.FormattingUtil.getFormattedOrganizationNumber;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import generated.client.oep_integrator.CaseStatus;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.supportmanagement.Errand;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.integration.casedata.CaseDataIntegration;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.db.StatusesRepository;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.OpenEMapper;
import se.sundsvall.casestatus.service.mapper.SupportManagementMapper;

@Service
public class CaseStatusService {

	private final CaseManagementIntegration caseManagementIntegration;
	private final OepIntegratorClient oepIntegratorClient;
	private final CaseRepository caseRepository;
	private final PartyIntegration partyIntegration;
	private final SupportManagementService supportManagementService;

	private final CaseManagementMapper caseManagementMapper;
	private final CaseDataIntegration caseDataIntegration;
	private final SupportManagementMapper supportManagementMapper;
	private final StatusesRepository statusesRepository;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository,
		final PartyIntegration partyIntegration,
		final SupportManagementService supportManagementService,
		final CaseManagementMapper caseManagementMapper,
		final CaseDataIntegration caseDataIntegration,
		final SupportManagementMapper supportManagementMapper,
		final StatusesRepository statusesRepository) {

		this.caseManagementIntegration = caseManagementIntegration;
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseRepository = caseRepository;
		this.partyIntegration = partyIntegration;
		this.supportManagementService = supportManagementService;
		this.caseManagementMapper = caseManagementMapper;
		this.caseDataIntegration = caseDataIntegration;
		this.supportManagementMapper = supportManagementMapper;
		this.statusesRepository = statusesRepository;
	}

	public OepStatusResponse getOepStatus(final String externalCaseId, final String municipalityId) {
		final var caseStatus = caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));

		final var oepStatus = statusesRepository.findByCaseManagementStatus(caseStatus.getStatus())
			.map(StatusesEntity::getOepStatus)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Could not find matching open-E status for status %s".formatted(caseStatus.getStatus())));

		return toOepStatusResponse(oepStatus);
	}

	public CaseStatusResponse getCaseStatus(final String externalCaseId, final String municipalityId) {
		return caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.or(() -> caseRepository.findByFlowInstanceIdAndMunicipalityId(externalCaseId, municipalityId)
				.map(OpenEMapper::toCaseStatusResponse))
			.map(this::addExternalStatusByOepStatus)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));
	}

	public CasePdfResponse getCasePdf(final String municipalityId, final String externalCaseId) {

		final var response = oepIntegratorClient.getCasePdfByFlowInstanceId(municipalityId, InstanceType.EXTERNAL, externalCaseId);
		final var body = response.getBody();

		if (response.getStatusCode().is4xxClientError() || body == null) {
			throw Problem.valueOf(Status.NOT_FOUND, "Could not find PDF for case with externalCaseId %s".formatted(externalCaseId));
		}

		try {
			return toCasePdfResponse(externalCaseId, body);
		} catch (final IOException e) {
			throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Failed to read PDF data");
		}

	}

	public List<CaseStatusResponse> getCaseStatuses(final String organizationNumber, final String municipalityId) {
		final List<CaseStatusResponse> statuses = new ArrayList<>();

		caseManagementIntegration.getCaseStatusForOrganizationNumber(sanitizeForLogging(organizationNumber), municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		caseRepository.findByOrganisationNumberAndMunicipalityId(sanitizeForLogging(organizationNumber), municipalityId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.map(this::addExternalStatusByOepStatus)
			.forEach(statuses::add);

		getSupportManagementStatuses(getFormattedOrganizationNumber(organizationNumber), municipalityId, statuses);

		return statuses;
	}

	public List<CaseStatusResponse> getCaseStatusesForParty(final String partyId, final String municipalityId) {
		final var partyResult = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		final var cmFuture = getCaseManagementStatusesAsync(partyId, municipalityId);
		final var oepFuture = getOepStatusesAsync(partyId, municipalityId);

		// Support Management depends on the party type (PRIVATE -> partyId, ENTERPRISE -> formatted org no)
		final CompletableFuture<List<CaseStatusResponse>> supportFuture;
		if (partyResult.containsKey(PRIVATE)) {
			supportFuture = getSupportManagementStatusesAsync(partyId, municipalityId);
		} else if (partyResult.containsKey(ENTERPRISE)) {
			supportFuture = getSupportManagementStatusesAsync(getFormattedOrganizationNumber(partyResult.get(ENTERPRISE)), municipalityId);
		} else {
			return emptyList();
		}

		final var statuses = Stream.of(cmFuture, oepFuture, supportFuture)
			.map(CompletableFuture::join)
			.flatMap(List::stream)
			.toList();

		return filterResponses(statuses);
	}

	private void getSupportManagementStatuses(final String partyId, final String municipalityId, final List<CaseStatusResponse> statuses) {
		supportManagementService.getSupportManagementCasesByExternalId(municipalityId, partyId)
			.forEach((namespace, errands) -> errands.stream()
				.map(errand -> supportManagementMapper.toCaseStatusResponse(
					errand,
					namespace,
					getStatusesBySupportManagementStatus(errand.getStatus()),
					getSupportManagementClassificationName(municipalityId, namespace, errand)))
				.forEach(statuses::add));
	}

	/**
	 * CaseStatusResponses are processed, and if there are duplicate externalCaseId's, some filtering is done. Responses
	 * with null externalCaseId's are not filtered. If there are multiple responses with the same externalCaseId, the ones
	 * with system
	 * OPEN_E_PLATFORM are removed.
	 *
	 * @param  responses List of CaseStatusResponses
	 * @return           Filtered list of CaseStatusResponses
	 */
	List<CaseStatusResponse> filterResponses(final List<CaseStatusResponse> responses) {

		if (responses == null) {
			return emptyList();
		}
		final var nullExternalCaseIdStream = responses.stream()
			.filter(response -> response.getExternalCaseId() == null);

		final var filteredStream = responses.stream()
			.filter(response -> response.getExternalCaseId() != null)
			.collect(Collectors.groupingBy(CaseStatusResponse::getExternalCaseId))
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
		if (isNotBlank(propertyDesignation) && isNotBlank(errandNumber)) {
			throw Problem.valueOf(BAD_REQUEST, "Both propertyDesignation and errandNumber cannot be provided at the same time");
		}
		if (propertyDesignation == null && errandNumber == null) {
			throw Problem.valueOf(BAD_REQUEST, "Either propertyDesignation or errandNumber must be provided");
		}

		if (isNotBlank(propertyDesignation)) {
			return caseDataIntegration.getNamespaces().stream()
				.flatMap(namespace -> caseDataIntegration.getCaseDataCaseByPropertyDesignation(municipalityId, namespace, propertyDesignation).stream())
				.toList();
		}

		final var filterString = "errandNumber:'%s'".formatted(errandNumber);
		final var supportManagementCases = supportManagementService.getSupportManagementCases(municipalityId, filterString);
		final var caseStatusResponses = supportManagementCases.entrySet().stream()
			.flatMap(entry -> {
				var namespace = entry.getKey();
				return entry.getValue().stream()
					.map(errand -> supportManagementMapper.toCaseStatusResponse(
						errand,
						namespace,
						getStatusesBySupportManagementStatus(errand.getStatus()),
						getSupportManagementClassificationName(municipalityId, namespace, errand)));
			})
			.toList();

		final var caseDataCases = caseDataIntegration.getNamespaces().stream()
			.flatMap(namespace -> caseDataIntegration.getCaseDataCaseByErrandNumber(municipalityId, namespace, errandNumber).stream())
			.toList();

		return Stream.of(caseStatusResponses, addExternalStatusesByCaseManagementStatus(caseDataCases))
			.flatMap(List::stream)
			.toList();
	}

	private CompletableFuture<List<CaseStatusResponse>> getCaseManagementStatusesAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.toList());
	}

	private CompletableFuture<List<CaseStatusResponse>> getOepStatusesAsync(final String partyId, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> oepIntegratorClient.getCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId).stream()
			.map(caseEnvelope -> {
				final var casestatus = oepIntegratorClient.getCaseStatus(municipalityId, InstanceType.EXTERNAL, caseEnvelope.getFlowInstanceId());
				var caseStatusResponse = OpenEMapper.toCaseStatusResponse(caseEnvelope, casestatus);
				if (caseStatusResponse != null) {
					caseStatusResponse.setExternalStatus(getExternalStatusByOepStatus(casestatus));
				}
				return caseStatusResponse;

			})
			.toList());
	}

	private CompletableFuture<List<CaseStatusResponse>> getSupportManagementStatusesAsync(final String externalIdOrOrgNo, final String municipalityId) {
		return CompletableFuture.supplyAsync(() -> supportManagementService
			.getSupportManagementCasesByExternalId(municipalityId, externalIdOrOrgNo)
			.entrySet()
			.stream()
			.flatMap(entry -> {
				var namespace = entry.getKey();
				return entry.getValue().stream()
					.map(errand -> supportManagementMapper.toCaseStatusResponse(
						errand,
						namespace,
						getStatusesBySupportManagementStatus(errand.getStatus()),
						getSupportManagementClassificationName(municipalityId, namespace, errand)));
			})
			.toList());
	}

	private String getSupportManagementClassificationName(final String municipalityId, String namespace, final Errand errand) {
		return supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand);
	}

	private StatusesEntity getStatusesBySupportManagementStatus(final String supportManagementStatus) {
		if (isBlank(supportManagementStatus)) {
			return StatusesEntity.builder()
				.build();
		}
		return statusesRepository.findBySupportManagementStatus(supportManagementStatus).orElse(StatusesEntity.builder()
			.withSupportManagementStatus(supportManagementStatus)
			.withExternalStatus(DEFAULT_EXTERNAL_STATUS)
			.build());
	}

	private String getExternalStatusByOepStatus(final CaseStatus oepStatus) {
		if (oepStatus == null || isBlank(oepStatus.getName())) {
			return null;
		}
		return statusesRepository.findByOepStatus(oepStatus.getName()).stream()
			.map(StatusesEntity::getExternalStatus)
			.filter(Objects::nonNull)
			.filter(StringUtils::isNotBlank)
			.findFirst()
			.orElse(DEFAULT_EXTERNAL_STATUS);
	}

	private List<CaseStatusResponse> addExternalStatusesByCaseManagementStatus(List<CaseStatusResponse> statusResponses) {

		return statusResponses.stream()
			.map(response -> {
				if (isNotBlank(response.getStatus())) {
					final var externalStatus = statusesRepository.findByCaseManagementStatus(response.getStatus())
						.map(StatusesEntity::getExternalStatus)
						.orElse(DEFAULT_EXTERNAL_STATUS);
					response.setExternalStatus(externalStatus);
				}
				return response;
			}).toList();
	}

	private CaseStatusResponse addExternalStatusByOepStatus(CaseStatusResponse caseStatusResponse) {

		if (isNotBlank(caseStatusResponse.getStatus())) {
			final var externalStatus = statusesRepository.findByOepStatus(caseStatusResponse.getStatus()).stream()
				.map(StatusesEntity::getExternalStatus)
				.filter(Objects::nonNull)
				.filter(StringUtils::isNotBlank)
				.findFirst()
				.orElse(DEFAULT_EXTERNAL_STATUS);
			caseStatusResponse.setExternalStatus(externalStatus);
		}
		return caseStatusResponse;
	}
}
