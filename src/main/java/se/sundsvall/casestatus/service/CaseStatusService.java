package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.util.stream.Collectors.toList;
import static se.sundsvall.casestatus.service.Mapper.toCasePdfResponse;
import static se.sundsvall.casestatus.service.Mapper.toOepStatusResponse;
import static se.sundsvall.casestatus.utility.Constants.MISSING;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

@Service
public class CaseStatusService {

	static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final String CASE_NOT_FOUND = "Case with id %s not found";
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
				.map(Mapper::mapToCaseStatusResponse))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));
	}

	public CasePdfResponse getCasePdf(final String externalCaseId) {
		return openEIntegration.getPdf(externalCaseId)
			.map(pdf -> toCasePdfResponse(externalCaseId, pdf))
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));
	}

	public List<CaseStatusResponse> getCaseStatuses(final String organizationNumber, final String municipalityId) {
		final var result = caseManagementIntegration.getCaseStatusForOrganizationNumber(organizationNumber, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.collect(toList());

		final var cachedStatuses = caseRepository.findByOrganisationNumberAndMunicipalityId(organizationNumber, municipalityId).stream()
			.map(Mapper::mapToCaseStatusResponse)
			.toList();

		result.addAll(cachedStatuses);
		return result;
	}

	public List<CaseStatusResponse> getCaseStatusesForParty(final String partyId, final String municipalityId) {
		final var partyResult = partyIntegration.getLegalIdByPartyId(municipalityId, partyId);

		final var result = caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.collect(toList());

		if (partyResult.containsKey(PRIVATE)) {

			openEIntegration.getCaseStatuses(municipalityId, partyResult.get(PRIVATE)).stream()
				.map(Mapper::mapToCaseStatusResponse)
				.forEach(result::add);

			caseRepository.findByPersonIdAndMunicipalityId(partyId, municipalityId).stream()
				.map(Mapper::mapToCaseStatusResponse)
				.forEach(result::add);

			final var filterString = "stakeholders.externalId:'%s'".formatted(partyId);
			supportManagementService.getSupportManagementCases(municipalityId, filterString).stream()
				.map(Mapper::toCaseStatusResponse)
				.forEach(result::add);

			return filterResponse(result);

		} else if (partyResult.containsKey(ENTERPRISE)) {

			// Due to discrepancies in the organization number format in open-E, we need to check both the original and the
			// formatted number.
			final var legalId = getFormattedOrganizationNumber(partyResult.get(ENTERPRISE));

			final var cachedStatusesUnformatted = caseRepository.findByOrganisationNumberAndMunicipalityId(partyResult.get(ENTERPRISE), municipalityId).stream()
				.map(Mapper::mapToCaseStatusResponse)
				.toList();

			final var cachedStatusesFormatted = caseRepository.findByOrganisationNumberAndMunicipalityId(legalId, municipalityId).stream()
				.map(Mapper::mapToCaseStatusResponse)
				.toList();

			result.addAll(cachedStatusesUnformatted);
			result.addAll(cachedStatusesFormatted);
		}

		return result;
	}

	private List<CaseStatusResponse> filterResponse(final List<CaseStatusResponse> request) {
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

	private String getFormattedOrganizationNumber(final String organizationNumber) {

		// Control that the organizationNumber is not null and that it is a valid length
		if (IntStream.of(13, 12, 11, 10).anyMatch(i -> organizationNumber.length() == i)) {
			// Remove all non-digit characters
			final String cleanNumber = organizationNumber.replaceAll("\\D", "");

			if (cleanNumber.length() == 12) {
				// Insert the hyphen at the correct position
				return cleanNumber.substring(0, 8) + "-" + cleanNumber.substring(8);

			}
			if (cleanNumber.length() == 10) {
				return cleanNumber.substring(0, 6) + "-" + cleanNumber.substring(6);
			}
		}
		return organizationNumber;
	}
}
