package se.sundsvall.casestatus.integration.casemanagement;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;

@ExtendWith(MockitoExtension.class)
class CaseManagementIntegrationTests {
    
    @Mock
    private CaseManagementClient mockCaseManagementClient;
    
    @InjectMocks
    private CaseManagementIntegration caseManagementIntegration;
    
    @Test
    void getCaseStatusForExternalCaseId_ok() {
        var caseStatus = new CaseStatusDTO().externalCaseId("someExternalCaseId");
        
        when(mockCaseManagementClient.getCaseStatusForExternalCaseId(any(String.class))).thenReturn(caseStatus);
        
        var result = caseManagementIntegration.getCaseStatusForExternalId("someExternalCaseId");
        
        assertThat(result).isNotNull().isPresent();
        
        verify(mockCaseManagementClient).getCaseStatusForExternalCaseId(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementClient);
    }
    
    @Test
    void getCaseStatusForExternalCaseId_error() {
        when(mockCaseManagementClient.getCaseStatusForExternalCaseId(any(String.class)))
            .thenThrow(Problem.builder().build());
        
        var result = caseManagementIntegration.getCaseStatusForExternalId("someExternalCaseId");
        
        assertThat(result).isNotNull().isNotPresent();
        
        verify(mockCaseManagementClient).getCaseStatusForExternalCaseId(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementClient);
    }
    
    @Test
    void getCaseStatusForOrganizationNumber_ok() {
        when(mockCaseManagementClient.getCaseStatusForOrganizationNumber(any(String.class)))
            .thenReturn(List.of(new CaseStatusDTO(), new CaseStatusDTO(), new CaseStatusDTO()));
        
        var result = caseManagementIntegration.getCaseStatusForOrganizationNumber("someOrganizationNumber");
        
        assertThat(result).isNotNull().hasSize(3);
        
        verify(mockCaseManagementClient).getCaseStatusForOrganizationNumber(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementClient);
    }
    
    @Test
    void getCaseStatusForOrganizationNumber_error() {
        when(mockCaseManagementClient.getCaseStatusForOrganizationNumber(any(String.class)))
            .thenThrow(Problem.builder().build());
        
        var result = caseManagementIntegration.getCaseStatusForOrganizationNumber("someOrganizationNumber");
        
        assertThat(result).isNotNull().isEmpty();
        
        verify(mockCaseManagementClient).getCaseStatusForOrganizationNumber(any(String.class));
        verifyNoMoreInteractions(mockCaseManagementClient);
    }
}
