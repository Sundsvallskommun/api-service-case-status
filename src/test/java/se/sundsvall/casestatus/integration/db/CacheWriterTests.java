package se.sundsvall.casestatus.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import se.sundsvall.casestatus.integration.db.domain.CacheCompanyCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachePrivateCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CacheUnknownCaseStatus;

@ExtendWith(MockitoExtension.class)
class CacheWriterTests {

	@Mock
	private NamedParameterJdbcTemplate mockJdbcTemplate;

	@InjectMocks
	private CacheWriter cacheWriter;

	@Mock
	private JdbcTemplate jdbcTemplate;

	@Mock
	private DataSource dataSource;

	@Mock
	private Connection connection;

	@Mock
	private CallableStatement callableStatement;

	@Test
    void writeToCompanyTable_ok() {

        when(mockJdbcTemplate.update(any(String.class), any(SqlParameterSource.class))).thenReturn(0);

        cacheWriter.writeToCompanyTable(CacheCompanyCaseStatus.builder()
                .withStatus("someStatus")
                .withContentType("someContentType")
                .withFlowInstanceID("SomeFlowInstanceId")
                .withFamilyID("someFamilyId")
                .withErrandType("someErrandType")
                .withFirstSubmitted("someFirstSubmittedDate")
                .withLastStatusChange("someLastSubmittedStatusChange")
                .withOrganisationNumber("someOrganisationNumber")
                .build());

        verify(mockJdbcTemplate).update(any(String.class), any(SqlParameterSource.class));
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void writeToCompanyTable_error() {

        when(mockJdbcTemplate.update(any(String.class), any(SqlParameterSource.class))).thenThrow(new BadSqlGrammarException("", "", new SQLSyntaxErrorException()));

        cacheWriter.writeToCompanyTable(CacheCompanyCaseStatus.builder()
                .withStatus("someStatus")
                .withContentType("someContentType")
                .withFlowInstanceID("SomeFlowInstanceId")
                .withFamilyID("someFamilyId")
                .withErrandType("someErrandType")
                .withFirstSubmitted("someFirstSubmittedDate")
                .withLastStatusChange("someLastSubmittedStatusChange")
                .withOrganisationNumber("someOrganisationNumber")
                .build());

        verify(mockJdbcTemplate).update(any(String.class), any(SqlParameterSource.class));
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void writeToPrivateTable_ok() {

        when(mockJdbcTemplate.update(any(String.class), any(SqlParameterSource.class))).thenReturn(0);

        cacheWriter.writeToPrivateTable(CachePrivateCaseStatus.builder()
                .withStatus("someStatus")
                .withContentType("someContentType")
                .withFlowInstanceID("SomeFlowInstanceId")
                .withFamilyID("someFamilyId")
                .withErrandType("someErrandType")
                .withFirstSubmitted("someFirstSubmittedDate")
                .withLastStatusChange("someLastSubmittedStatusChange")
                .withPersonId("somePersonId")
                .build());

        verify(mockJdbcTemplate).update(any(String.class), any(SqlParameterSource.class));
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void writeToPrivateTable_error() {

        when(mockJdbcTemplate.update(any(String.class), any(SqlParameterSource.class))).thenThrow(new BadSqlGrammarException("", "", new SQLSyntaxErrorException()));

        cacheWriter.writeToPrivateTable(CachePrivateCaseStatus.builder()
                .withStatus("someStatus")
                .withContentType("someContentType")
                .withFlowInstanceID("SomeFlowInstanceId")
                .withFamilyID("someFamilyId")
                .withErrandType("someErrandType")
                .withFirstSubmitted("someFirstSubmittedDate")
                .withLastStatusChange("someLastSubmittedStatusChange")
                .withPersonId("somePersonId")
                .build());

        verify(mockJdbcTemplate).update(any(String.class), any(SqlParameterSource.class));
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void writeToUnknownTable_ok() {

        when(mockJdbcTemplate.update(any(String.class), any(SqlParameterSource.class))).thenReturn(0);

        cacheWriter.writeToUnknownTable(CacheUnknownCaseStatus.builder()
                .withStatus("someStatus")
                .withContentType("someContentType")
                .withFlowInstanceID("SomeFlowInstanceId")
                .withFamilyID("someFamilyId")
                .withErrandType("someErrandType")
                .withFirstSubmitted("someFirstSubmittedDate")
                .withLastStatusChange("someLastSubmittedStatusChange")
                .build());

        verify(mockJdbcTemplate).update(any(String.class), any(SqlParameterSource.class));
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void writeToUnknownTable_error() {

        when(mockJdbcTemplate.update(any(String.class), any(SqlParameterSource.class))).thenThrow(new BadSqlGrammarException("", "", new SQLSyntaxErrorException()));

        cacheWriter.writeToUnknownTable(CacheUnknownCaseStatus.builder()
                .withStatus("someStatus")
                .withContentType("someContentType")
                .withFlowInstanceID("SomeFlowInstanceId")
                .withFamilyID("someFamilyId")
                .withErrandType("someErrandType")
                .withFirstSubmitted("someFirstSubmittedDate")
                .withLastStatusChange("someLastSubmittedStatusChange")
                .build());

        verify(mockJdbcTemplate).update(any(String.class), any(SqlParameterSource.class));
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void mergeCaseStatusCache() throws SQLException {

        when(mockJdbcTemplate.getJdbcTemplate()).thenReturn(jdbcTemplate);
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareCall(any())).thenReturn(callableStatement);
        when(callableStatement.executeUpdate()).thenReturn(12);

        final var result = cacheWriter.mergeCaseStatusCache();

        assertThat(result).isEqualTo(12);
        verify(mockJdbcTemplate).getJdbcTemplate();
        verify(jdbcTemplate).getDataSource();
        verify(dataSource).getConnection();
        verify(connection).prepareCall(any());
        verify(callableStatement).executeUpdate();
        verify(callableStatement).close();
        verify(connection).close();
        verifyNoMoreInteractions(mockJdbcTemplate, jdbcTemplate, dataSource, connection, callableStatement);
    }

	@Test
	void testToString() {
		final var result = CachePrivateCaseStatus.builder()
			.withStatus("someStatus")
			.withContentType("someContentType")
			.withFlowInstanceID("SomeFlowInstanceId")
			.withFamilyID("someFamilyId")
			.withErrandType("someErrandType")
			.withFirstSubmitted("someFirstSubmittedDate")
			.withLastStatusChange("someLastSubmittedStatusChange")
			.withPersonId("somePersonId")
			.build();

		assertThat(result).hasToString(
			"CachePrivateCaseStatus(super=AbstractCacheCaseStatus(flowInstanceID=SomeFlowInstanceId, familyID=someFamilyId, status=someStatus, errandType=someErrandType, contentType=someContentType, firstSubmitted=someFirstSubmittedDate, lastStatusChange=someLastSubmittedStatusChange), personId=somePersonId)");

	}
}
