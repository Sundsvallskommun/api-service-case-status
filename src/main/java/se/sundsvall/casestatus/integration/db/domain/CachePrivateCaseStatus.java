package se.sundsvall.casestatus.integration.db.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SuperBuilder(setterPrefix = "with")
@ToString(callSuper = true)
public class CachePrivateCaseStatus extends AbstractCacheCaseStatus {

    private String personId;
}
