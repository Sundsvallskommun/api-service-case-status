package se.sundsvall.casestatus.service;

import static java.util.stream.Collectors.toList;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casestatus.api.domain.CasePdfResponse;
import se.sundsvall.casestatus.api.domain.CaseStatusResponse;
import se.sundsvall.casestatus.api.domain.OepStatusResponse;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.DbIntegration;
import se.sundsvall.casestatus.integration.db.domain.CachedCaseStatus;
import se.sundsvall.casestatus.integration.incident.IncidentIntegration;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;

@Service
public class CaseStatusService {
    
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    static final String MISSING = "Saknas";
    
    private final CaseManagementIntegration caseManagementIntegration;
    private final IncidentIntegration incidentIntegration;
    private final OpenEIntegration openEIntegration;
    private final DbIntegration dbIntegration;
    
    public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
        final IncidentIntegration incidentIntegration, final OpenEIntegration openEIntegration,
        final DbIntegration dbIntegration) {
        this.caseManagementIntegration = caseManagementIntegration;
        this.incidentIntegration = incidentIntegration;
        this.openEIntegration = openEIntegration;
        this.dbIntegration = dbIntegration;
    }
    
    public OepStatusResponse getOepStatus(final String externalCaseId) {
        var status = caseManagementIntegration.getCaseStatusForExternalId(externalCaseId)
            .flatMap(caseStatus -> dbIntegration.getCaseManagementOpenEStatus(caseStatus.getStatus()))
            .orElseGet(() -> incidentIntegration.getIncidentStatus(externalCaseId)
                .flatMap(incidentStatus -> dbIntegration.getIncidentOpenEStatus(incidentStatus.getStatusId()))
                .orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND)));
        
        return OepStatusResponse.builder()
            .withKey("status")
            .withValue(status)
            .build();
    }
    
    public CaseStatusResponse getCaseStatus(final String externalCaseId) {
        return caseManagementIntegration.getCaseStatusForExternalId(externalCaseId)
            .map(this::mapToCaseStatusResponse)
            .orElseGet(() -> dbIntegration.getExternalCaseIdStatusFromCache(externalCaseId)
                .map(this::mapToCaseStatusResponse)
                .orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND)));
    }
    
    public CasePdfResponse getCasePdf(final String externalCaseId) {
        return openEIntegration.getPdf(externalCaseId)
            .map(pdf -> CasePdfResponse.builder()
                .withExternalCaseId(externalCaseId)
                .withBase64(pdf)
                .build())
            .orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND));
    }
    
    public List<CaseStatusResponse> getCaseStatuses(final String organizationNumber) {
        var result = caseManagementIntegration.getCaseStatusForOrganizationNumber(organizationNumber).stream()
            .map(this::mapToCaseStatusResponse)
            .collect(toList());
        
        var cachedStatuses = dbIntegration.getOrganizationStatusesFromCache(organizationNumber).stream()
            .map(this::mapToCaseStatusResponse)
            .toList();
        result.addAll(cachedStatuses);
        return result;
    }
    
    CaseStatusResponse mapToCaseStatusResponse(final CaseStatusDTO caseStatus) {
        var status = dbIntegration.getCaseManagementOpenEStatus(caseStatus.getStatus());
        
        
        var timestamp = Optional.ofNullable(caseStatus.getTimestamp())
            .map(DATE_TIME_FORMATTER::format)
            .orElse(MISSING);
        
        var serviceName = Optional.ofNullable(caseStatus.getServiceName()).orElse(getCaseType(caseStatus));
        
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
    
    CaseStatusResponse mapToCaseStatusResponse(final CachedCaseStatus cachedCaseStatus) {
        return CaseStatusResponse.builder()
            .withId(cachedCaseStatus.getFlowInstanceId())
            .withCaseType(cachedCaseStatus.getErrandType())
            .withStatus(cachedCaseStatus.getStatus())
            .withFirstSubmitted(cachedCaseStatus.getFirstSubmitted())
            .withLastStatusChange(cachedCaseStatus.getLastStatusChange())
            .withIsOpenEErrand(true)
            .build();
    }
    
    private String getCaseType(CaseStatusDTO caseStatus) {
        return Optional.ofNullable(caseStatus.getCaseType())
            .flatMap(dbIntegration::getMapCaseTypeEnumText)
            .orElse(MISSING);
    }
}
