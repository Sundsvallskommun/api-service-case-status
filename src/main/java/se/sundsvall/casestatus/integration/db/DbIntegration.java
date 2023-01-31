package se.sundsvall.casestatus.integration.db;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.domain.CacheCompanyCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachePrivateCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CacheUnknownCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachedCaseStatus;

import java.util.List;
import java.util.Optional;

@Component
public class DbIntegration {

    private final CaseStatusReader caseManagementStatusReader;

    private final CacheWriter cacheWriter;

    public DbIntegration(final CaseStatusReader caseManagementStatusReader,
                         CacheWriter cacheWriter) {
        this.caseManagementStatusReader = caseManagementStatusReader;
        this.cacheWriter = cacheWriter;
    }

    public void writeToCompanyTable(CacheCompanyCaseStatus companyCaseStatus) {
        cacheWriter.writeToCompanyTable(companyCaseStatus);
    }

    public void writeToPrivateTable(CachePrivateCaseStatus privateCaseStatus) {
        cacheWriter.writeToPrivateTable(privateCaseStatus);
    }

    public void writeToUnknownTable(CacheUnknownCaseStatus unknowCaseStatus) {
        cacheWriter.writeToUnknownTable(unknowCaseStatus);
    }

    public int mergeCaseStatusCache() {
        return cacheWriter.mergeCaseStatusCache();
    }

    public Optional<CachedCaseStatus> getExternalCaseIdStatusFromCache(final String flowInstanceId) {
        return caseManagementStatusReader.getExternalCaseIdStatus(flowInstanceId);
    }

    public List<CachedCaseStatus> getOrganizationStatusesFromCache(final String organizationNumber) {
        return caseManagementStatusReader.getOrganizationStatuses(organizationNumber);
    }

    public Optional<String> getCaseManagementOpenEStatus(final String id) {
        return caseManagementStatusReader.getCaseManagementOpenEStatus(id);
    }

    public Optional<String> getIncidentOpenEStatus(final Integer id) {
        return caseManagementStatusReader.getIncidentOpenEStatus(id);
    }

    @Cacheable("mapCaseTypeEnum")
    public Optional<String> getMapCaseTypeEnumText(final String value) {
        return caseManagementStatusReader.getMapCaseTypeEnumText(value);
    }
}
