package se.sundsvall.casestatus.integration.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.domain.AbstractCacheCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CacheCompanyCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachePrivateCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CacheUnknownCaseStatus;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

@Component
class CacheWriter {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(CacheWriter.class);

    CacheWriter(@Qualifier("integration.db.case-status.jdbc-template") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    void writeToCompanyTable(CacheCompanyCaseStatus companyCaseStatus) {

        SqlParameterSource parameters = mapParameters(companyCaseStatus)
                .addValue("organisationNumber", companyCaseStatus.getOrganisationNumber());
        try {
            jdbcTemplate.update("""
                    INSERT INTO StageCompanies(flowInstanceID,organisationNumber, familyId,status,errandType,contentType,firstSubmitted,lastStatusChange)
                    values(:flowInstanceID,:organisationNumber,:familyId,:status,:errandType,:contentType,:firstSubmitted,:lastStatusChange)""", parameters);
        } catch (Exception e) {
            LOG.info("Could not stage company row: {}", companyCaseStatus, e);
        }
    }


    void writeToPrivateTable(CachePrivateCaseStatus privateCaseStatus) {

        SqlParameterSource parameters = mapParameters(privateCaseStatus).addValue("personId", privateCaseStatus.getPersonId());
        try {
            jdbcTemplate.update("""
                    INSERT INTO StagePrivate(flowInstanceID,personId, familyId,status,errandType,contentType,firstSubmitted,lastStatusChange)
                    values(:flowInstanceID,:personId,:familyId,:status,:errandType,:contentType,:firstSubmitted,:lastStatusChange)""", parameters);

        } catch (Exception e) {
            LOG.info("Could not stage private row: {}", privateCaseStatus, e);
        }
    }

    void writeToUnknownTable(CacheUnknownCaseStatus unknownCaseStatus) {
        SqlParameterSource parameters = mapParameters(unknownCaseStatus);
        try {
            jdbcTemplate.update("""
                    INSERT INTO StageUnknown(flowInstanceID, familyId,status,errandType,contentType,firstSubmitted,lastStatusChange)
                    values(:flowInstanceID,:familyId,:status,:errandType,:contentType,:firstSubmitted,:lastStatusChange)""", parameters);
        } catch (Exception e) {
            LOG.info("Could not stage unknown row: {}", unknownCaseStatus, e);
        }
    }

    public int mergeCaseStatusCache() {
        try {
            try (var connection = Objects.requireNonNull(jdbcTemplate.getJdbcTemplate().getDataSource())
                    .getConnection()) {
                return connection.prepareCall("CALL MergeCaseStatusCache()")
                        .executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    MapSqlParameterSource mapParameters(AbstractCacheCaseStatus cacheCaseStatus) {
        return new MapSqlParameterSource(Map.of(
                "flowInstanceID", cacheCaseStatus.getFlowInstanceID(),
                "familyId", cacheCaseStatus.getFamilyID(),
                "status", cacheCaseStatus.getStatus(),
                "errandType", cacheCaseStatus.getErrandType(),
                "contentType", cacheCaseStatus.getContentType(),
                "firstSubmitted", cacheCaseStatus.getFirstSubmitted(),
                "lastStatusChange", cacheCaseStatus.getLastStatusChange()));
    }
}
