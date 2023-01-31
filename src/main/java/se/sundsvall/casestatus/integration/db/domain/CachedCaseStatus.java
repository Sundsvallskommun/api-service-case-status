package se.sundsvall.casestatus.integration.db.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(setterPrefix = "with")
@ToString
public class CachedCaseStatus {

    private String flowInstanceId;
    private String errandType;
    private String status;
    private String firstSubmitted;
    private String lastStatusChange;
}
