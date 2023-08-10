package se.sundsvall.casestatus.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import se.sundsvall.casestatus.integration.db.domain.CachedCaseStatus;

@ExtendWith(MockitoExtension.class)
class CaseStatusReaderTests {

	@Mock
	private NamedParameterJdbcTemplate mockJdbcTemplate;

	@InjectMocks
	private CaseStatusReader caseStatusReader;

	@Test
    void getCaseManagementOpenEStatus() {
        when(mockJdbcTemplate.query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<String>>any()))
                .thenReturn("someValue");

        final var result = caseStatusReader.getCaseManagementOpenEStatus("someId");

        assertThat(result).isNotNull();

        verify(mockJdbcTemplate).query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<String>>any());
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void getIncidentOpenEStatus() {
        when(mockJdbcTemplate.query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<String>>any()))
                .thenReturn("someValue");

        final var result = caseStatusReader.getIncidentOpenEStatus(456);

        assertThat(result).isNotNull();

        verify(mockJdbcTemplate).query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<String>>any());
        verifyNoMoreInteractions(mockJdbcTemplate);

    }

	@Test
    void getMapCaseTypeEnumText() {
        when(mockJdbcTemplate.query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<String>>any()))
                .thenReturn("someValue");

        final var result = caseStatusReader.getMapCaseTypeEnumText("someEnumValue");

        assertThat(result).isNotNull();

        verify(mockJdbcTemplate).query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<String>>any());
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
	void mapOpenEId() throws SQLException {
		final var mockResultSet = mock(ResultSet.class);
		when(mockResultSet.getString("openeID")).thenReturn("someId");
		when(mockResultSet.next()).thenReturn(true);

		final var result = caseStatusReader.mapOpenEId(mockResultSet);

		assertThat(result)
			.isNotNull()
			.isEqualTo("someId");
	}

	@Test
	void mapCaseTypeEnumText() throws SQLException {
		final var mockResultSet = mock(ResultSet.class);
		when(mockResultSet.getString("text")).thenReturn("someText");
		when(mockResultSet.next()).thenReturn(true);

		final var result = caseStatusReader.mapCaseTypeEnumText(mockResultSet);

		assertThat(result)
			.isNotNull()
			.isEqualTo("someText");
	}

	@Test
    void getExternalCaseIdStatus() {
        when(mockJdbcTemplate.query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<CachedCaseStatus>>any()))
                .thenReturn(CachedCaseStatus.builder().build());

        final var result = caseStatusReader.getExternalCaseIdStatus("someExternalCaseId");

        assertThat(result).isNotNull();

        verify(mockJdbcTemplate).query(any(String.class), anyMap(), ArgumentMatchers.<ResultSetExtractor<CachedCaseStatus>>any());
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
    void getOrganizationStatuses() {
        when(mockJdbcTemplate.query(any(String.class), anyMap(), ArgumentMatchers.<RowMapper<CachedCaseStatus>>any()))
                .thenReturn(List.of(CachedCaseStatus.builder().build(), CachedCaseStatus.builder().build()));

        final var result = caseStatusReader.getOrganizationStatuses("someExternalCaseId");

        assertThat(result).isNotNull().hasSize(2);

        verify(mockJdbcTemplate).query(any(String.class), anyMap(), ArgumentMatchers.<RowMapper<CachedCaseStatus>>any());
        verifyNoMoreInteractions(mockJdbcTemplate);
    }

	@Test
	void mapRow() throws SQLException {
		final var result = caseStatusReader.mapRow(mockResultSet(), 0);

		assertCachedCaseStatusHasCorrectValues(result);
	}

	@Test
	void mapToCachedCaseStatus() throws SQLException {
		final var result = caseStatusReader.mapToCachedCaseStatus(mockResultSet());

		assertCachedCaseStatusHasCorrectValues(result);
	}

	private ResultSet mockResultSet() throws SQLException {
		final var mockResultSet = mock(ResultSet.class);
		when(mockResultSet.next()).thenReturn(true);
		when(mockResultSet.getString("FlowInstanceID")).thenReturn("someFlowInstanceId");
		when(mockResultSet.getString("ErrandType")).thenReturn("someErrandType");
		when(mockResultSet.getString("Status")).thenReturn("someStatus");
		when(mockResultSet.getString("FirstSubmitted")).thenReturn("someFirstSubmittedValue");
		when(mockResultSet.getString("LastStatusChange")).thenReturn("someLastStatusChangeValue");
		return mockResultSet;
	}

	private void assertCachedCaseStatusHasCorrectValues(final CachedCaseStatus cachedCaseStatus) {
		assertThat(cachedCaseStatus).isNotNull();
		assertThat(cachedCaseStatus.getFlowInstanceId()).isEqualTo("someFlowInstanceId");
		assertThat(cachedCaseStatus.getErrandType()).isEqualTo("someErrandType");
		assertThat(cachedCaseStatus.getStatus()).isEqualTo("someStatus");
		assertThat(cachedCaseStatus.getFirstSubmitted()).isEqualTo("someFirstSubmittedValue");
		assertThat(cachedCaseStatus.getLastStatusChange()).isEqualTo("someLastStatusChangeValue");
	}
}
