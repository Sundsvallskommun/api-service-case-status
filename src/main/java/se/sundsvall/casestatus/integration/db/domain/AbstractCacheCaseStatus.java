package se.sundsvall.casestatus.integration.db.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SuperBuilder(setterPrefix = "with")
@ToString
public abstract class AbstractCacheCaseStatus {
    private String flowInstanceID;
    private String familyID;
    private String status;
    private String errandType;
    private String contentType;
    private String firstSubmitted;
    private String lastStatusChange;

}
