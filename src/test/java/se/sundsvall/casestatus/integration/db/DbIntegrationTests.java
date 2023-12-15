package se.sundsvall.casestatus.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casestatus.integration.db.domain.CacheCompanyCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachePrivateCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CacheUnknownCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachedCaseStatus;

@ExtendWith(MockitoExtension.class)
class DbIntegrationTests {

	@Mock
	private CaseStatusReader mockCaseStatusReader;
	@Mock
	private CacheWriter mockCacheWriter;

	@InjectMocks
	private DbIntegration dbIntegration;

	@Test
	void getExternalCaseIdStatusFromCache() {
		when(mockCaseStatusReader.getExternalCaseIdStatus(any(String.class)))
			.thenReturn(Optional.ofNullable(CachedCaseStatus.builder().build()));

		final var result = dbIntegration.getExternalCaseIdStatusFromCache("someFlowInstanceId");

		assertThat(result).isNotNull();

		verify(mockCaseStatusReader).getExternalCaseIdStatus(any(String.class));
		verifyNoMoreInteractions(mockCaseStatusReader);
		verifyNoInteractions(mockCacheWriter);
	}

	@Test
	void getOrganizationStatusesFromCache() {
		when(mockCaseStatusReader.getOrganizationStatuses(any(String.class)))
			.thenReturn(List.of(CachedCaseStatus.builder().build(), CachedCaseStatus.builder().build()));

		final var result = dbIntegration.getOrganizationStatusesFromCache("someOrganizationId");

		assertThat(result).isNotNull().hasSize(2);

		verify(mockCaseStatusReader).getOrganizationStatuses(any(String.class));
		verifyNoMoreInteractions(mockCaseStatusReader);
		verifyNoInteractions(mockCacheWriter);
	}

	@Test
	void writeToCompanyTable() {
		doNothing().when(mockCacheWriter).writeToCompanyTable(any(CacheCompanyCaseStatus.class));

		dbIntegration.writeToCompanyTable(CacheCompanyCaseStatus.builder().build());

		verify(mockCacheWriter).writeToCompanyTable(any(CacheCompanyCaseStatus.class));
		verifyNoMoreInteractions(mockCacheWriter);
		verifyNoInteractions(mockCaseStatusReader);

	}

	@Test
	void writeToPrivateTable() {
		doNothing().when(mockCacheWriter).writeToPrivateTable(any(CachePrivateCaseStatus.class));

		dbIntegration.writeToPrivateTable(CachePrivateCaseStatus.builder().build());

		verify(mockCacheWriter).writeToPrivateTable(any(CachePrivateCaseStatus.class));
		verifyNoMoreInteractions(mockCacheWriter);
		verifyNoInteractions(mockCaseStatusReader);

	}

	@Test
	void writeToUnknownTable() {
		doNothing().when(mockCacheWriter).writeToUnknownTable(any(CacheUnknownCaseStatus.class));

		dbIntegration.writeToUnknownTable(CacheUnknownCaseStatus.builder().build());

		verify(mockCacheWriter).writeToUnknownTable(any(CacheUnknownCaseStatus.class));
		verifyNoMoreInteractions(mockCacheWriter);
		verifyNoInteractions(mockCaseStatusReader);

	}

	@Test
	void getCaseManagementOpenEStatus() {
		when(mockCaseStatusReader.getCaseManagementOpenEStatus(any(String.class))).thenReturn(Optional.of("someValue"));

		final var result = dbIntegration.getCaseManagementOpenEStatus("someId");

		assertThat(result).isNotNull().isPresent();

		verify(mockCaseStatusReader).getCaseManagementOpenEStatus(any(String.class));
		verifyNoMoreInteractions(mockCaseStatusReader);
		verifyNoInteractions(mockCacheWriter);
	}

	@Test
	void getIncidentOpenEStatus() {
		when(mockCaseStatusReader.getIncidentOpenEStatus(any(Integer.class))).thenReturn(Optional.of("someValue"));

		final var result = dbIntegration.getIncidentOpenEStatus(789);

		assertThat(result).isNotNull();

		verify(mockCaseStatusReader).getIncidentOpenEStatus(any(Integer.class));
		verifyNoMoreInteractions(mockCaseStatusReader);
		verifyNoInteractions(mockCacheWriter);
	}

	@Test
	void getMapCaseTypeEnumText() {
		when(mockCaseStatusReader.getMapCaseTypeEnumText(any(String.class))).thenReturn(Optional.of("someText"));

		final var result = dbIntegration.getMapCaseTypeEnumText("someEnumValue");

		assertThat(result).isNotNull();

		verify(mockCaseStatusReader).getMapCaseTypeEnumText(any(String.class));
		verifyNoMoreInteractions(mockCaseStatusReader);
		verifyNoInteractions(mockCacheWriter);
	}
}
