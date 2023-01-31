package se.sundsvall.casestatus.api.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseStatusResponse {

    private String id;
    private String externalCaseId;
    private String caseType;
    private String status;
    private String firstSubmitted;
    private String lastStatusChange;
    private boolean isOpenEErrand;
}
