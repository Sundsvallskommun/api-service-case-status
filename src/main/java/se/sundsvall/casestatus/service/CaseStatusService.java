package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toCasePdfResponse;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toOepStatusResponse;
import static se.sundsvall.casestatus.util.Constants.CASE_NOT_FOUND;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;

import generated.client.oep_integrator.InstanceType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
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
	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;
	private final PartyIntegration partyIntegration;
	private final SupportManagementService supportManagementService;

	private final CaseManagementMapper caseManagementMapper;
	private final CaseDataIntegration caseDataIntegration;
	private final SupportManagementMapper supportManagementMapper;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository,
		final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository,
		final PartyIntegration partyIntegration,
		final SupportManagementService supportManagementService,
		final CaseManagementMapper caseManagementMapper,
		final CaseDataIntegration caseDataIntegration,
		final SupportManagementMapper supportManagementMapper) {
		this.caseManagementIntegration = caseManagementIntegration;
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseRepository = caseRepository;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.partyIntegration = partyIntegration;
		this.supportManagementService = supportManagementService;
		this.caseManagementMapper = caseManagementMapper;
		this.caseDataIntegration = caseDataIntegration;
		this.supportManagementMapper = supportManagementMapper;
	}

	public OepStatusResponse getOepStatus(final String externalCaseId, final String municipalityId) {
		final var caseStatus = caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));

		final var openEId = caseManagementOpeneViewRepository.findByCaseManagementId(caseStatus.getStatus())
			.map(CaseManagementOpeneView::getOpenEId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Could not find matching open-E status for status %s".formatted(caseStatus.getStatus())));

		return toOepStatusResponse(openEId);
	}

	public CaseStatusResponse getCaseStatus(final String externalCaseId, final String municipalityId) {
		return caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.or(() -> caseRepository.findByFlowInstanceIdAndMunicipalityId(externalCaseId, municipalityId)
				.map(OpenEMapper::toCaseStatusResponse))
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

		caseManagementIntegration.getCaseStatusForOrganizationNumber(organizationNumber, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		caseRepository.findByOrganisationNumberAndMunicipalityId(organizationNumber, municipalityId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		return statuses;
	}

	public List<CaseStatusResponse> getCaseStatusesForParty(final String partyId, final String municipalityId) {
		final var partyResult = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		if (partyResult.containsKey(PRIVATE)) {
			final var statuses = getPrivateCaseStatuses(partyId, municipalityId);
			return filterResponses(statuses);
		} else if (partyResult.containsKey(ENTERPRISE)) {
			final var statuses = getEnterpriseCaseStatuses(partyId, municipalityId);
			return filterResponses(statuses);
		}
		return emptyList();
	}

	List<CaseStatusResponse> getPrivateCaseStatuses(final String partyId, final String municipalityId) {
		final List<CaseStatusResponse> statuses = new ArrayList<>();

		caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		oepIntegratorClient.getCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId).stream()
			.map(caseEnvelope -> {
				final var casestatus = oepIntegratorClient.getCaseStatus(municipalityId, InstanceType.EXTERNAL, caseEnvelope.getFlowInstanceId());
				return OpenEMapper.toCaseStatusResponse(caseEnvelope, casestatus);
			})
			.forEach(statuses::add);

		final var filterString = "stakeholders.externalId:'%s'".formatted(partyId);
		supportManagementService.getSupportManagementCases(municipalityId, filterString)
			.forEach((namespace, errands) -> errands.stream()
				.map(errand -> supportManagementMapper.toCaseStatusResponse(errand, namespace))
				.forEach(statuses::add));

		return statuses;
	}

	List<CaseStatusResponse> getEnterpriseCaseStatuses(final String partyId, final String municipalityId) {
		final List<CaseStatusResponse> statuses = new ArrayList<>();

		// Fetching statuses from CaseManagement.
		caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		// Fetching cached statuses for the given organization number.
		oepIntegratorClient.getCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId).stream()
			.map(caseEnvelope -> {
				final var casestatus = oepIntegratorClient.getCaseStatus(municipalityId, InstanceType.EXTERNAL, caseEnvelope.getFlowInstanceId());
				return OpenEMapper.toCaseStatusResponse(caseEnvelope, casestatus);
			})
			.forEach(statuses::add);

		return statuses;
	}

	/**
	 * CaseStatusResponses are processed and if there are duplicate externalCaseId's some filtering is done. Responses with
	 * null externalCaseId's are not filtered. If there are multiple responses with the same externalCaseId, the ones with
	 * system
	 * OPEN_E_PLATFORM are removed.
	 *
	 * @param  responses List of CaseStatusResponses
	 * @return           Filtered list of CaseStatusResponses
	 */
	List<CaseStatusResponse> filterResponses(final List<CaseStatusResponse> responses) {
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
		if (StringUtils.isNotBlank(propertyDesignation) && StringUtils.isNotBlank(errandNumber)) {
			throw Problem.valueOf(BAD_REQUEST, "Both propertyDesignation and errandNumber cannot be provided at the same time");
		}
		if (propertyDesignation == null && errandNumber == null) {
			throw Problem.valueOf(BAD_REQUEST, "Either propertyDesignation or errandNumber must be provided");
		}

		if (StringUtils.isNotBlank(propertyDesignation)) {
			return caseDataIntegration.getCaseDataCaseByPropertyDesignation(municipalityId, propertyDesignation);
		}

		final var filterString = "errandNumber:'%s'".formatted(errandNumber);
		var supportManagementCases = supportManagementService.getSupportManagementCases(municipalityId, filterString);
		var caseStatusResponses = supportManagementCases.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(errand -> supportManagementMapper.toCaseStatusResponse(errand, entry.getKey())))
			.toList();

		var caseDataCases = caseDataIntegration.getCaseDataCaseByErrandNumber(municipalityId, errandNumber);

		return Stream.of(caseStatusResponses, caseDataCases)
			.flatMap(List::stream)
			.toList();
	}
}
