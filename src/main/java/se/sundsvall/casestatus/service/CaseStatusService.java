package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toCasePdfResponse;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toOepStatusResponse;
import static se.sundsvall.casestatus.util.Constants.CASE_NOT_FOUND;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.FormattingUtil.getFormattedOrganizationNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.opene.rest.OpenEIntegration;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.OpenEMapper;
import se.sundsvall.casestatus.service.mapper.SupportManagementMapper;

@Service
public class CaseStatusService {

	private final CaseManagementIntegration caseManagementIntegration;
	private final OpenEIntegration openEIntegration;
	private final CaseRepository caseRepository;
	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;
	private final PartyIntegration partyIntegration;
	private final SupportManagementService supportManagementService;

	private final CaseManagementMapper caseManagementMapper;
	private final SupportManagementMapper supportManagementMapper;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OpenEIntegration openEIntegration,
		final CaseRepository caseRepository,
		final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository,
		final PartyIntegration partyIntegration,
		final SupportManagementService supportManagementService,
		final CaseManagementMapper caseManagementMapper,
		final SupportManagementMapper supportManagementMapper) {
		this.caseManagementIntegration = caseManagementIntegration;
		this.openEIntegration = openEIntegration;
		this.caseRepository = caseRepository;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.partyIntegration = partyIntegration;
		this.supportManagementService = supportManagementService;
		this.caseManagementMapper = caseManagementMapper;
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

	public CasePdfResponse getCasePdf(final String externalCaseId) {
		return openEIntegration.getPdf(externalCaseId)
			.map(pdf -> toCasePdfResponse(externalCaseId, pdf))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));
	}

	public List<CaseStatusResponse> getCaseStatuses(final String organizationNumber, final String municipalityId) {
		List<CaseStatusResponse> statuses = new ArrayList<>();

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
			var legalId = partyResult.get(PRIVATE);
			var statuses = getPrivateCaseStatuses(partyId, legalId, municipalityId);
			return filterResponses(statuses);
		} else if (partyResult.containsKey(ENTERPRISE)) {
			var legalId = partyResult.get(ENTERPRISE);
			var statuses = getEnterpriseCaseStatuses(partyId, legalId, municipalityId);
			return filterResponses(statuses);
		}
		return emptyList();
	}

	List<CaseStatusResponse> getPrivateCaseStatuses(final String partyId, final String legalId, final String municipalityId) {
		List<CaseStatusResponse> statuses = new ArrayList<>();

		caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		openEIntegration.getCaseStatuses(municipalityId, legalId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		final var filterString = "stakeholders.externalId:'%s'".formatted(partyId);
		supportManagementService.getSupportManagementCases(municipalityId, filterString)
			.forEach((namespace, errands) -> errands.stream()
				.map(errand -> supportManagementMapper.toCaseStatusResponse(errand, namespace))
				.forEach(statuses::add));

		return statuses;
	}

	List<CaseStatusResponse> getEnterpriseCaseStatuses(final String partyId, final String legalId, final String municipalityId) {
		List<CaseStatusResponse> statuses = new ArrayList<>();

		// Fetching statuses from CaseManagement.
		caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		// Fetching cached statuses for the given organization number.
		openEIntegration.getCaseStatuses(municipalityId, legalId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		// Fetching cached statuses for the formatted organization number.
		openEIntegration.getCaseStatuses(municipalityId, getFormattedOrganizationNumber(legalId)).stream()
			.map(OpenEMapper::toCaseStatusResponse)
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
		var nullExternalCaseIdStream = responses.stream()
			.filter(response -> response.getExternalCaseId() == null);

		var filteredStream = responses.stream()
			.filter(response -> response.getExternalCaseId() != null)
			.collect(Collectors.groupingBy(CaseStatusResponse::getExternalCaseId))
			.entrySet().stream()
			.flatMap(entry -> {
				var entries = entry.getValue();
				if (entries.size() > 1 && entries.stream().anyMatch(response -> OPEN_E_PLATFORM.equals(response.getSystem()))) {
					return entries.stream()
						.filter(response -> !OPEN_E_PLATFORM.equals(response.getSystem()));
				}
				return entries.stream();
			});

		return Stream.concat(nullExternalCaseIdStream, filteredStream)
			.toList();
	}

}
