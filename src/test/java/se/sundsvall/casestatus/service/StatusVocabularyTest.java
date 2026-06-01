package se.sundsvall.casestatus.service;

import generated.client.oep_integrator.CaseStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.db.StatusesRepository;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casestatus.util.Constants.DEFAULT_EXTERNAL_STATUS;

@ExtendWith(MockitoExtension.class)
class StatusVocabularyTest {

	@Mock
	private StatusesRepository repository;

	@InjectMocks
	private StatusVocabulary statusVocabulary;

	private StatusesEntity entityWith(final String externalStatus) {
		return StatusesEntity.builder()
			.withExternalStatus(externalStatus)
			.build();
	}

	@BeforeEach
	void resetVocabulary() {
		// no-op; @InjectMocks gives us a fresh wrapper per test
	}

	@Test
	void findOepStatusForCaseManagementStatus_returnsMappedOepStatus() {
		final var entity = StatusesEntity.builder().withOepStatus("oep").build();
		when(repository.findByCaseManagementStatus("cm")).thenReturn(Optional.of(entity));

		final var result = statusVocabulary.findOepStatusForCaseManagementStatus("cm");

		assertThat(result).contains("oep");
		verify(repository).findByCaseManagementStatus("cm");
	}

	@Test
	void findOepStatusForCaseManagementStatus_returnsEmpty_whenInputBlank() {
		assertThat(statusVocabulary.findOepStatusForCaseManagementStatus("")).isEmpty();
		assertThat(statusVocabulary.findOepStatusForCaseManagementStatus(null)).isEmpty();
		verifyNoInteractions(repository);
	}

	@Test
	void findOepStatusForCaseManagementStatus_returnsEmpty_whenNotFound() {
		when(repository.findByCaseManagementStatus("cm")).thenReturn(Optional.empty());

		assertThat(statusVocabulary.findOepStatusForCaseManagementStatus("cm")).isEmpty();
	}

	@Test
	void translateOepStatus_caseStatus_returnsExternalStatus() {
		when(repository.findByOepStatus("Inskickat")).thenReturn(List.of(entityWith("Inskickat (visat)")));

		final var result = statusVocabulary.translateOepStatus(new CaseStatus().status("Inskickat"));

		assertThat(result).isEqualTo("Inskickat (visat)");
	}

	@Test
	void translateOepStatus_caseStatus_returnsNull_whenInputNull() {
		assertThat(statusVocabulary.translateOepStatus((CaseStatus) null)).isNull();
		verifyNoInteractions(repository);
	}

	@Test
	void translateOepStatus_caseStatus_returnsNull_whenStatusFieldBlank() {
		assertThat(statusVocabulary.translateOepStatus(new CaseStatus())).isNull();
		verifyNoInteractions(repository);
	}

	@Test
	void translateOepStatus_string_returnsExternalStatus() {
		when(repository.findByOepStatus("Klart")).thenReturn(List.of(entityWith("Avslutat")));

		assertThat(statusVocabulary.translateOepStatus("Klart")).isEqualTo("Avslutat");
	}

	@Test
	void translateOepStatus_string_fallsBackToDefault_whenNotFound() {
		when(repository.findByOepStatus("Unknown")).thenReturn(List.of());

		assertThat(statusVocabulary.translateOepStatus("Unknown")).isEqualTo(DEFAULT_EXTERNAL_STATUS);
	}

	@Test
	void translateOepStatus_string_fallsBackToDefault_whenAllRowsHaveBlankExternalStatus() {
		when(repository.findByOepStatus("Klart")).thenReturn(List.of(entityWith(null), entityWith("")));

		assertThat(statusVocabulary.translateOepStatus("Klart")).isEqualTo(DEFAULT_EXTERNAL_STATUS);
	}

	@Test
	void translateOepStatus_string_returnsNull_whenInputBlank() {
		assertThat(statusVocabulary.translateOepStatus("")).isNull();
		assertThat(statusVocabulary.translateOepStatus((String) null)).isNull();
		verifyNoInteractions(repository);
	}

	@Test
	void translateCaseManagementStatus_returnsExternalStatus() {
		when(repository.findByCaseManagementStatus("Pågående"))
			.thenReturn(Optional.of(entityWith("Handläggning pågår")));

		assertThat(statusVocabulary.translateCaseManagementStatus("Pågående")).isEqualTo("Handläggning pågår");
	}

	@Test
	void translateCaseManagementStatus_fallsBackToDefault_whenNotFound() {
		when(repository.findByCaseManagementStatus("Unmapped")).thenReturn(Optional.empty());

		assertThat(statusVocabulary.translateCaseManagementStatus("Unmapped")).isEqualTo(DEFAULT_EXTERNAL_STATUS);
	}

	@Test
	void translateCaseManagementStatus_returnsNull_whenInputBlank() {
		assertThat(statusVocabulary.translateCaseManagementStatus("")).isNull();
		assertThat(statusVocabulary.translateCaseManagementStatus(null)).isNull();
		verifyNoInteractions(repository);
	}

	@Test
	void lookupBySupportManagementStatus_returnsEntity_whenFound() {
		final var entity = StatusesEntity.builder()
			.withSupportManagementStatus("SM")
			.withExternalStatus("Pågår")
			.build();
		when(repository.findBySupportManagementStatus("SM")).thenReturn(Optional.of(entity));

		assertThat(statusVocabulary.lookupBySupportManagementStatus("SM")).isEqualTo(entity);
	}

	@Test
	void lookupBySupportManagementStatus_returnsSynthesizedEntity_whenNotFound() {
		when(repository.findBySupportManagementStatus("SM")).thenReturn(Optional.empty());

		final var result = statusVocabulary.lookupBySupportManagementStatus("SM");

		assertThat(result.getSupportManagementStatus()).isEqualTo("SM");
		assertThat(result.getExternalStatus()).isEqualTo(DEFAULT_EXTERNAL_STATUS);
	}

	@Test
	void lookupBySupportManagementStatus_returnsEmptyEntity_whenInputBlank() {
		final var result = statusVocabulary.lookupBySupportManagementStatus(null);

		assertThat(result).isNotNull();
		assertThat(result.getSupportManagementStatus()).isNull();
		assertThat(result.getExternalStatus()).isNull();
		verifyNoInteractions(repository);
	}
}
