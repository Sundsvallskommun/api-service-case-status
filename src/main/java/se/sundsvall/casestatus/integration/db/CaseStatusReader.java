package se.sundsvall.casestatus.integration.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.domain.CachedCaseStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
class CaseStatusReader {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    CaseStatusReader(@Qualifier("integrationDbCaseStatusJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Optional<String> getCaseManagementOpenEStatus(final String id) {
        return Optional.ofNullable(jdbcTemplate.query(
                "SELECT openeID FROM vStatusCaseManagementOpenE WHERE caseManagementID = :id",
                Map.of("id", id),
                this::mapOpenEId
        ));
    }

    Optional<String> getIncidentOpenEStatus(final Integer id) {
        return Optional.ofNullable(jdbcTemplate.query(
                "SELECT openeID FROM vStatusIncidentOpenE WHERE incidentID = :id",
                Map.of("id", String.valueOf(id)),
                this::mapOpenEId
        ));
    }

    Optional<String> getMapCaseTypeEnumText(final String enumValue) {
        return Optional.ofNullable(jdbcTemplate.query(
                "SELECT text FROM MapCaseTypeEnums WHERE ENUM = :enumValue",
                Map.of("enumValue", enumValue),
                this::mapCaseTypeEnumText
        ));
    }

    String mapOpenEId(final ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getString("openeID");
        }
        return null;
    }

    String mapCaseTypeEnumText(final ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getString("text");
        }
        return null;
    }


    Optional<CachedCaseStatus> getExternalCaseIdStatus(final String flowInstanceId) {
        return Optional.ofNullable(jdbcTemplate.query(
                "SELECT FlowInstanceID, ErrandType, Status, LastStatusChange, FirstSubmitted FROM Companies WHERE FlowInstanceID = :flowInstanceId",
                Map.of("flowInstanceId", flowInstanceId),
                this::mapToCachedCaseStatus
        ));
    }

    List<CachedCaseStatus> getOrganizationStatuses(final String organizationNumber) {
        return jdbcTemplate.query(
                "SELECT FlowInstanceID, ErrandType, Status, LastStatusChange, FirstSubmitted FROM Companies WHERE OrganisationNumber = :organizationNumber",
                Map.of("organizationNumber", organizationNumber),
                this::mapRow
        );
    }

    CachedCaseStatus mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        rs.absolute(rowNum);
        return mapToCachedCaseStatus(rs);
    }

    CachedCaseStatus mapToCachedCaseStatus(final ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        return CachedCaseStatus.builder()
                .withFlowInstanceId(rs.getString("FlowInstanceID"))
                .withErrandType(rs.getString("ErrandType"))
                .withStatus(rs.getString("Status"))
                .withFirstSubmitted(rs.getString("FirstSubmitted"))
                .withLastStatusChange(rs.getString("LastStatusChange"))
                .build();
    }
}
