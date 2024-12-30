package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static java.util.stream.Collectors.toList;
import static se.sundsvall.casestatus.service.Mapper.toCasePdfResponse;
import static se.sundsvall.casestatus.service.Mapper.toCaseStatusResponse;
import static se.sundsvall.casestatus.service.Mapper.toOepStatusResponse;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;
import se.sundsvall.casestatus.integration.party.PartyIntegration;

@Service
public class CaseStatusService {

	static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	static final String MISSING = "Saknas";
	private static final String CASE_NOT_FOUND = "Case with id %s not found";
	private final CaseManagementIntegration caseManagementIntegration;
	private final OpenEIntegration openEIntegration;
	private final CaseRepository caseRepository;
	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;
	private final CaseTypeRepository caseTypeRepository;
	private final PartyIntegration partyIntegration;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OpenEIntegration openEIntegration,
		final CaseRepository caseRepository,
		final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository,
		final CaseTypeRepository caseTypeRepository,
		final PartyIntegration partyIntegration) {
		this.caseManagementIntegration = caseManagementIntegration;
		this.openEIntegration = openEIntegration;
		this.caseRepository = caseRepository;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.caseTypeRepository = caseTypeRepository;
		this.partyIntegration = partyIntegration;
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
		return caseManagementIntegration
			.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(dto -> mapToCaseStatusResponse(dto, municipalityId))
			.or(() -> caseRepository
				.findByFlowInstanceIdAndMunicipalityId(externalCaseId, municipalityId)
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
			.map(dto -> mapToCaseStatusResponse(dto, municipalityId))
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
			.map(dto -> mapToCaseStatusResponse(dto, municipalityId))
			.collect(toList());

		if (partyResult.containsKey(PRIVATE)) {

			openEIntegration.getCaseStatuses(municipalityId, partyResult.get(PRIVATE)).stream()
				.map(Mapper::mapToCaseStatusResponse)
				.forEach(result::add);

			caseRepository.findByPersonIdAndMunicipalityId(partyId, municipalityId).stream()
				.map(Mapper::mapToCaseStatusResponse)
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

	CaseStatusResponse mapToCaseStatusResponse(final CaseStatusDTO caseStatus, final String municipalityId) {
		final var status = caseManagementOpeneViewRepository.findByCaseManagementId(caseStatus.getStatus())
			.map(CaseManagementOpeneView::getOpenEId)
			.orElse(caseStatus.getStatus());

		final var timestamp = Optional.ofNullable(caseStatus.getTimestamp())
			.map(DATE_TIME_FORMATTER::format)
			.orElse(MISSING);

		final var serviceName = Optional.ofNullable(caseStatus.getServiceName()).orElse(getCaseType(caseStatus, municipalityId));

		return toCaseStatusResponse(caseStatus, serviceName, status, timestamp);
	}

	private String getCaseType(final CaseStatusDTO caseStatus, final String municipalityId) {
		return Optional.ofNullable(caseStatus.getCaseType())
			.flatMap((String enumValue) -> caseTypeRepository.findByEnumValueAndMunicipalityId(enumValue, municipalityId)
				.map(CaseTypeEntity::getDescription))
			.orElse(MISSING);
	}

	private List<CaseStatusResponse> filterResponse(final List<CaseStatusResponse> result) {
		final var latestStatusById = result.stream()
			.filter(response -> response.getExternalCaseId() != null) // Exclude entries with null id
			.collect(Collectors.toMap(
				CaseStatusResponse::getExternalCaseId,
				response -> response,
				(existing, newEntry) -> {
					if (existing.getLastStatusChange() == null) {
						return newEntry;
					}
					if (newEntry.getLastStatusChange() == null) {
						return existing;
					}
					return existing.getLastStatusChange().compareTo(newEntry.getLastStatusChange()) > 0 ? existing : newEntry;
				}));

		return new ArrayList<>(latestStatusById.values());
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
