package se.sundsvall.casestatus.service;

import static java.util.stream.Collectors.toList;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.CompanyRepository;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;
import se.sundsvall.casestatus.integration.db.model.CompanyEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;

@Service
public class CaseStatusService {

	static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	static final String MISSING = "Saknas";
	private static final String CASE_NOT_FOUND = "Case with id %s not found";
	private final CaseManagementIntegration caseManagementIntegration;

	private final OpenEIntegration openEIntegration;

	private final CompanyRepository companyRepository;

	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;

	private final CaseTypeRepository caseTypeRepository;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OpenEIntegration openEIntegration,
		final CompanyRepository companyRepository, final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository, final CaseTypeRepository caseTypeRepository) {
		this.caseManagementIntegration = caseManagementIntegration;
		this.openEIntegration = openEIntegration;
		this.companyRepository = companyRepository;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.caseTypeRepository = caseTypeRepository;
	}

	public OepStatusResponse getOepStatus(final String externalCaseId, final String municipalityId) {
		final var caseStatus = caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));

		final var openEId = caseManagementOpeneViewRepository.findByCaseManagementId(caseStatus.getStatus())
			.map(CaseManagementOpeneView::getOpenEId)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, "Could not find matching open-E status for status %s".formatted(caseStatus.getStatus())));

		return OepStatusResponse.builder()
			.withKey("status")
			.withValue(openEId)
			.build();
	}

	public CaseStatusResponse getCaseStatus(final String externalCaseId, final String municipalityId) {
		return caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(dto -> mapToCaseStatusResponse(dto, municipalityId))
			.orElseGet(() -> companyRepository.findByFlowInstanceIdAndMunicipalityId(externalCaseId, municipalityId)
				.map(this::mapToCaseStatusResponse)
				.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId))));
	}

	public CasePdfResponse getCasePdf(final String externalCaseId) {
		return openEIntegration.getPdf(externalCaseId)
			.map(pdf -> CasePdfResponse.builder()
				.withExternalCaseId(externalCaseId)
				.withBase64(pdf)
				.build())
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));
	}

	public List<CaseStatusResponse> getCaseStatuses(final String organizationNumber, final String municipalityId) {
		final var result = caseManagementIntegration.getCaseStatusForOrganizationNumber(organizationNumber, municipalityId).stream()
			.map(dto -> mapToCaseStatusResponse(dto, municipalityId))
			.collect(toList());

		final var cachedStatuses = companyRepository.findByOrganisationNumberAndMunicipalityId(organizationNumber, municipalityId).stream()
			.map(this::mapToCaseStatusResponse)
			.toList();
		result.addAll(cachedStatuses);
		return result;
	}

	CaseStatusResponse mapToCaseStatusResponse(final CaseStatusDTO caseStatus, final String municipalityId) {
		final var status = caseManagementOpeneViewRepository.findByCaseManagementId(caseStatus.getStatus())
			.map(CaseManagementOpeneView::getOpenEId);

		final var timestamp = Optional.ofNullable(caseStatus.getTimestamp())
			.map(DATE_TIME_FORMATTER::format)
			.orElse(MISSING);

		final var serviceName = Optional.ofNullable(caseStatus.getServiceName()).orElse(getCaseType(caseStatus, municipalityId));

		return CaseStatusResponse.builder()
			.withId(caseStatus.getCaseId())
			.withExternalCaseId(caseStatus.getExternalCaseId())
			.withCaseType(serviceName)
			.withStatus(status.orElse(caseStatus.getStatus()))
			.withLastStatusChange(timestamp)
			.withFirstSubmitted(MISSING)
			.withIsOpenEErrand(false)
			.build();
	}

	CaseStatusResponse mapToCaseStatusResponse(final CompanyEntity cachedCaseStatus) {
		return CaseStatusResponse.builder()
			.withId(cachedCaseStatus.getFlowInstanceId())
			.withCaseType(cachedCaseStatus.getErrandType())
			.withStatus(cachedCaseStatus.getStatus())
			.withFirstSubmitted(cachedCaseStatus.getFirstSubmitted())
			.withLastStatusChange(cachedCaseStatus.getLastStatusChange())
			.withIsOpenEErrand(true)
			.build();
	}

	private String getCaseType(final CaseStatusDTO caseStatus, final String municipalityId) {
		return Optional.ofNullable(caseStatus.getCaseType())
			.flatMap((String enumValue) -> caseTypeRepository.findByEnumValueAndMunicipalityId(enumValue, municipalityId)
				.map(CaseTypeEntity::getDescription))
			.orElse(MISSING);
	}

}
