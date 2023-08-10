package se.sundsvall.casestatus.integration.db;

import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.Map;

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
import se.sundsvall.casestatus.integration.db.exception.DatabaseException;

@Component
class CacheWriter {
	private final NamedParameterJdbcTemplate jdbcTemplate;
	private static final Logger LOG = LoggerFactory.getLogger(CacheWriter.class);

	CacheWriter(@Qualifier("integration.db.case-status.jdbc-template") final NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	void writeToCompanyTable(CacheCompanyCaseStatus companyCaseStatus) {

		final SqlParameterSource parameters = mapParameters(companyCaseStatus)
			.addValue("organisationNumber", companyCaseStatus.getOrganisationNumber());
		try {
			jdbcTemplate.update("""
				INSERT INTO StageCompanies(flowInstanceID,organisationNumber, familyId,status,errandType,contentType,firstSubmitted,lastStatusChange)
				values(:flowInstanceID,:organisationNumber,:familyId,:status,:errandType,:contentType,:firstSubmitted,:lastStatusChange)""", parameters);
		} catch (final Exception e) {
			LOG.info("Could not stage company row: {}", companyCaseStatus, e);
		}
	}

	void writeToPrivateTable(CachePrivateCaseStatus privateCaseStatus) {

		final SqlParameterSource parameters = mapParameters(privateCaseStatus).addValue("personId", privateCaseStatus.getPersonId());
		try {
			jdbcTemplate.update("""
				INSERT INTO StagePrivate(flowInstanceID,personId, familyId,status,errandType,contentType,firstSubmitted,lastStatusChange)
				values(:flowInstanceID,:personId,:familyId,:status,:errandType,:contentType,:firstSubmitted,:lastStatusChange)""", parameters);

		} catch (final Exception e) {
			LOG.info("Could not stage private row: {}", privateCaseStatus, e);
		}
	}

	void writeToUnknownTable(CacheUnknownCaseStatus unknownCaseStatus) {
		final SqlParameterSource parameters = mapParameters(unknownCaseStatus);
		try {
			jdbcTemplate.update("""
				INSERT INTO StageUnknown(flowInstanceID, familyId,status,errandType,contentType,firstSubmitted,lastStatusChange)
				values(:flowInstanceID,:familyId,:status,:errandType,:contentType,:firstSubmitted,:lastStatusChange)""", parameters);
		} catch (final Exception e) {
			LOG.info("Could not stage unknown row: {}", unknownCaseStatus, e);
		}
	}

	public int mergeCaseStatusCache() {
		try (var connection = requireNonNull(jdbcTemplate.getJdbcTemplate().getDataSource()).getConnection();
			var callableStatement = connection.prepareCall("CALL MergeCaseStatusCache()")) {

			return callableStatement.executeUpdate();

		} catch (final SQLException e) {
			throw new DatabaseException("Exception in method mergeCaseStatusCache()", e);
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
