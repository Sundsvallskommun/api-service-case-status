package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.DbIntegration;
import se.sundsvall.casestatus.integration.db.domain.CachedCaseStatus;
import se.sundsvall.casestatus.integration.incident.IncidentIntegration;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.incident.IncidentOepResponse;

@ExtendWith(MockitoExtension.class)
class CaseStatusServiceTests {
    
    @Mock
    private CaseManagementIntegration mockCaseManagementIntegration;
    @Mock
    private IncidentIntegration mockIncidentIntegration;
    @Mock
    private OpenEIntegration mockOpenEIntegration;
    @Mock
    private DbIntegration mockDbIntegration;
    
    @InjectMocks
    private CaseStatusService caseStatusService;
    
    @Test
    void getOepStatus_caseStatusFoundInCaseManagement() {
        when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class)))
            .thenReturn(Optional.of(new CaseStatusDTO().status("someStatus")));
        when(mockDbIntegration.getCaseManagementOpenEStatus(eq("someStatus")))
            .thenReturn(Optional.of("someStatus"));
        
        var status = caseStatusService.getOepStatus("someExternalCaseId");
        
        assertThat(status).isNotNull().satisfies(oepStatus -> {
            assertThat(oepStatus.getKey()).isEqualTo("status");
            assertThat(oepStatus.getValue()).isEqualTo("someStatus");
        });
        
        verify(mockCaseManagementIntegration, times(1)).getCaseStatusForExternalId(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementIntegration);
        verify(mockDbIntegration, times(1)).getCaseManagementOpenEStatus(any(String.class));
        verifyNoMoreInteractions(mockDbIntegration);
    }
    
    @Test
    void getOepStatus_caseStatusNotFoundInCaseManagement() {
        when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class)))
            .thenReturn(Optional.empty());
        when(mockIncidentIntegration.getIncidentStatus(any(String.class)))
            .thenReturn(Optional.of(new IncidentOepResponse().statusId(678)));
        when(mockDbIntegration.getIncidentOpenEStatus(eq(678))).thenReturn(Optional.of("someStatus"));
        
        var status = caseStatusService.getOepStatus("someExternalCaseId");
        
        assertThat(status).isNotNull().satisfies(oepStatus -> {
            assertThat(oepStatus.getKey()).isEqualTo("status");
            assertThat(oepStatus.getValue()).isEqualTo("someStatus");
        });
        
        verify(mockCaseManagementIntegration, times(1)).getCaseStatusForExternalId(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementIntegration);
        verify(mockIncidentIntegration, times(1)).getIncidentStatus(any(String.class));
        verifyNoMoreInteractions(mockIncidentIntegration);
        verify(mockDbIntegration, times(1)).getIncidentOpenEStatus(any(Integer.class));
        verifyNoMoreInteractions(mockDbIntegration);
    }
    
    @Test
    void getCaseStatus_caseStatusFoundInCaseManagement() {
        var caseStatus = new CaseStatusDTO()
            .caseId("someCaseId")
            .externalCaseId("someExternalCaseId")
            .caseType(CaseStatusDTO.CaseTypeEnum.PARKING_PERMIT)
            .timestamp(LocalDateTime.now())
            .status("someStatus");
        
        when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class)))
            .thenReturn(Optional.of(caseStatus));
        when(mockDbIntegration.getCaseManagementOpenEStatus(any(String.class))).thenReturn(Optional.of("someStatus"));
        when(mockDbIntegration.getMapCaseTypeEnumText(any(String.class))).thenReturn(Optional.of("someText"));
        
        var result = caseStatusService.getCaseStatus("someExternalCaseId");
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("someCaseId");
        assertThat(result.getExternalCaseId()).isEqualTo("someExternalCaseId");
        assertThat(result.getCaseType()).isEqualTo("someText");
        assertThat(result.getLastStatusChange()).isEqualTo(CaseStatusService.DATE_TIME_FORMATTER.format(caseStatus.getTimestamp()));
        assertThat(result.getFirstSubmitted()).isEqualTo(CaseStatusService.MISSING);
        assertThat(result.isOpenEErrand()).isFalse();
        
        verify(mockCaseManagementIntegration, times(1)).getCaseStatusForExternalId(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementIntegration);
        verify(mockDbIntegration, times(1)).getCaseManagementOpenEStatus(any(String.class));
        verify(mockDbIntegration, times(1)).getMapCaseTypeEnumText(any(String.class));
        verifyNoMoreInteractions(mockDbIntegration);
    }
    
    @Test
    void getCaseStatus_caseStatusNotFoundInCaseManagement() {
        var cachedCaseStatus = CachedCaseStatus.builder()
            .withFlowInstanceId("someFlowInstanceId")
            .withErrandType("someErrandType")
            .withStatus("someStatus")
            .withFirstSubmitted("someFirstSubmittedValue")
            .withLastStatusChange("someLastStatusChangeValue")
            .build();
        
        when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class)))
            .thenReturn(Optional.empty());
        when(mockDbIntegration.getExternalCaseIdStatusFromCache(any(String.class))).thenReturn(Optional.ofNullable(cachedCaseStatus));
        
        var result = caseStatusService.getCaseStatus("someExternalCaseId");
        
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("someFlowInstanceId");
        assertThat(result.getExternalCaseId()).isNull();
        assertThat(result.getCaseType()).isEqualTo("someErrandType");
        assertThat(result.getFirstSubmitted()).isEqualTo("someFirstSubmittedValue");
        assertThat(result.getLastStatusChange()).isEqualTo("someLastStatusChangeValue");
        assertThat(result.isOpenEErrand()).isTrue();
        
        verify(mockCaseManagementIntegration, times(1)).getCaseStatusForExternalId(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementIntegration);
        verify(mockDbIntegration, times(1)).getExternalCaseIdStatusFromCache(any(String.class));
        verifyNoMoreInteractions(mockDbIntegration);
    }
    
    @Test
    void getCasePdf() {
        when(mockOpenEIntegration.getPdf(any(String.class))).thenReturn(Optional.of("someBase64String"));
        
        var result = caseStatusService.getCasePdf("someExternalCaseID");
        
        assertThat(result).isNotNull();
        
        verify(mockOpenEIntegration, times(1)).getPdf(any(String.class));
        verifyNoMoreInteractions(mockOpenEIntegration);
    }
    
    @Test
    void getCaseStatuses() {
        when(mockCaseManagementIntegration.getCaseStatusForOrganizationNumber(any(String.class)))
            .thenReturn(List.of(new CaseStatusDTO().status("someStatus"),
                new CaseStatusDTO().status("someOtherStatus")));
        when(mockDbIntegration.getCaseManagementOpenEStatus(any(String.class)))
            .thenReturn(Optional.of("someResolvedStatus"))
            .thenReturn(Optional.of("someOtherResolvedStatus"));
        when(mockDbIntegration.getOrganizationStatusesFromCache(any(String.class)))
            .thenReturn(List.of(CachedCaseStatus.builder().build()));
        
        var result = caseStatusService.getCaseStatuses("someOrganizationId");
        
        assertThat(result).isNotNull().hasSize(3);
        
        verify(mockCaseManagementIntegration, times(1)).getCaseStatusForOrganizationNumber(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementIntegration);
        verify(mockDbIntegration, times(1)).getOrganizationStatusesFromCache(any(String.class));
        verify(mockDbIntegration, times(2)).getCaseManagementOpenEStatus(any(String.class));
        verifyNoMoreInteractions(mockDbIntegration);
    }
}
