package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toCasePdfResponse;
import static se.sundsvall.casestatus.service.mapper.OpenEMapper.toOepStatusResponse;
import static se.sundsvall.casestatus.service.mapper.SupportManagementMapper.toCaseStatusResponse;
import static se.sundsvall.casestatus.util.Constants.CASE_NOT_FOUND;
import static se.sundsvall.casestatus.util.Constants.MISSING;
import static se.sundsvall.casestatus.util.FormattingUtil.getFormattedOrganizationNumber;

import generated.se.sundsvall.party.PartyType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

@Service
public class CaseStatusService {

	private final CaseManagementIntegration caseManagementIntegration;
	private final OpenEIntegration openEIntegration;
	private final CaseRepository caseRepository;
	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;
	private final PartyIntegration partyIntegration;
	private final SupportManagementService supportManagementService;

	private final CaseManagementMapper caseManagementMapper;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OpenEIntegration openEIntegration,
		final CaseRepository caseRepository,
		final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository,
		final PartyIntegration partyIntegration,
		final SupportManagementService supportManagementService,
		final CaseManagementMapper caseManagementMapper) {
		this.caseManagementIntegration = caseManagementIntegration;
		this.openEIntegration = openEIntegration;
		this.caseRepository = caseRepository;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.partyIntegration = partyIntegration;
		this.supportManagementService = supportManagementService;
		this.caseManagementMapper = caseManagementMapper;
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
			var statuses = getPrivateCaseStatuses(partyId, partyResult, municipalityId);
			return filterResponse(statuses);
		} else if (partyResult.containsKey(ENTERPRISE)) {
			var statuses = getEnterpriseCaseStatuses(partyId, partyResult, municipalityId);
			return filterResponse(statuses);
		}
		return emptyList();
	}

	List<CaseStatusResponse> getPrivateCaseStatuses(final String partyId, final Map<PartyType, String> partyMap, final String municipalityId) {
		List<CaseStatusResponse> statuses = new ArrayList<>();
		var legalId = partyMap.get(PRIVATE);

		caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		openEIntegration.getCaseStatuses(municipalityId, legalId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		caseRepository.findByPersonIdAndMunicipalityId(partyId, municipalityId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		final var filterString = "stakeholders.externalId:'%s'".formatted(partyId);
		supportManagementService.getSupportManagementCases(municipalityId, filterString)
			.forEach((namespace, errands) -> errands.stream()
				.map(errand -> toCaseStatusResponse(errand, namespace))
				.forEach(statuses::add));

		return statuses;
	}

	List<CaseStatusResponse> getEnterpriseCaseStatuses(final String partyId, final Map<PartyType, String> partyMap, final String municipalityId) {
		List<CaseStatusResponse> statuses = new ArrayList<>();
		var legalId = partyMap.get(ENTERPRISE);

		// Fetching statuses from CaseManagement.
		caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.forEach(statuses::add);

		// Fetching cached statuses for the given organization number.
		caseRepository.findByOrganisationNumberAndMunicipalityId(legalId, municipalityId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		// Fetching cached statuses for the formatted organization number.
		caseRepository.findByOrganisationNumberAndMunicipalityId(getFormattedOrganizationNumber(legalId), municipalityId).stream()
			.map(OpenEMapper::toCaseStatusResponse)
			.forEach(statuses::add);

		return statuses;
	}

	List<CaseStatusResponse> filterResponse(final List<CaseStatusResponse> request) {
		// Filter out cases without external case id as duplicates will only be appearing in the list with external case id
		final var casesWithoutExternalCaseId = request.stream()
			.filter(response -> response.getExternalCaseId() == null)
			.toList();

		final var latestStatusById = request.stream()
			.filter(response -> response.getExternalCaseId() != null)
			.collect(Collectors.toMap(
				CaseStatusResponse::getExternalCaseId,
				response -> response,
				(existing, newEntry) -> {
					if (existing.getLastStatusChange() == null || existing.getLastStatusChange().equals(MISSING)) {
						return newEntry;
					}
					if (newEntry.getLastStatusChange() == null || newEntry.getLastStatusChange().equals(MISSING)) {
						return existing;
					}
					return existing.getLastStatusChange().compareTo(newEntry.getLastStatusChange()) > 0 ? existing : newEntry;
				}));

		final var result = new ArrayList<CaseStatusResponse>();
		result.addAll(casesWithoutExternalCaseId);
		result.addAll(latestStatusById.values());

		return result;
	}

}
