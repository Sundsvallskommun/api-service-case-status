package se.sundsvall.casestatus.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.db.SupportManagementStatusRepository;
import se.sundsvall.casestatus.integration.db.model.SupportManagementStatusEntity;

@ExtendWith(MockitoExtension.class)
class SupportManagementMapperTest {

	@Mock
	private SupportManagementStatusRepository supportManagementStatusRepository;

	@InjectMocks
	private SupportManagementMapper supportManagementMapper;

	@Test
	void toCaseStatusResponse_withExternalCaseId() {
		// Arrange
		final var namespace = "namespace";
		final var genericStatus = "genericStatus";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));
		final var spy = Mockito.spy(supportManagementMapper);
		when(spy.getStatus(errand.getStatus())).thenReturn(genericStatus);

		// Act
		final var response = spy.toCaseStatusResponse(errand, namespace);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("title");
		assertThat(response.getStatus()).isEqualTo(genericStatus);
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponse_withoutExternalCaseId() {
		// Arrange
		final var namespace = "namespace";
		final var genericStatus = "genericStatus";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"));
		final var spy = Mockito.spy(supportManagementMapper);
		when(spy.getStatus(errand.getStatus())).thenReturn(genericStatus);

		// Act
		final var response = spy.toCaseStatusResponse(errand, namespace);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isNull();
		assertThat(response.getCaseType()).isEqualTo("title");
		assertThat(response.getStatus()).isEqualTo(genericStatus);
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponse_withNullModified() {
		// Arrange
		final var namespace = "namespace";
		final var genericStatus = "genericStatus";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));
		final var spy = Mockito.spy(supportManagementMapper);
		when(spy.getStatus(errand.getStatus())).thenReturn(genericStatus);

		// Act
		final var response = spy.toCaseStatusResponse(errand, namespace);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("title");
		assertThat(response.getStatus()).isEqualTo(genericStatus);
		assertThat(response.getLastStatusChange()).isNull();
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void getStatus() {
		final var statusEntity = new SupportManagementStatusEntity();
		final var genericStatus = "genericStatus";
		statusEntity.setGenericStatus(genericStatus);
		when(supportManagementStatusRepository.findBySystemStatus("someStatus")).thenReturn(Optional.of(statusEntity));

		final var status = supportManagementMapper.getStatus("someStatus");

		assertThat(status).isNotNull().isEqualTo(genericStatus);
	}

	@Test
	void getStatus_notFound() {
		when(supportManagementStatusRepository.findBySystemStatus("someStatus")).thenReturn(Optional.empty());

		final var status = supportManagementMapper.getStatus("someStatus");

		assertThat(status).isNotNull().isEqualTo("someStatus");
	}

}
