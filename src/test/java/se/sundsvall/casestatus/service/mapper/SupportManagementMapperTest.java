package se.sundsvall.casestatus.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.util.Constants.DEFAULT_EXTERNAL_STATUS;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;

@ExtendWith(MockitoExtension.class)
class SupportManagementMapperTest {

	@InjectMocks
	private SupportManagementMapper supportManagementMapper;

	@Test
	void toCaseStatusResponseWithExternalCaseId() {

		// Arrange
		final var namespace = "namespace";
		final var smStatus = "someStatus";
		final var externalStatus = "externalStatus";
		final var statuses = new StatusesEntity().builder().withSupportManagementStatus(smStatus).withExternalStatus(externalStatus).build();
		final var classificationDisplayName = "classificationDisplayName";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.classification(new Classification().type("type"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = supportManagementMapper.toCaseStatusResponse(errand, namespace, statuses, classificationDisplayName);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("classificationDisplayName");
		assertThat(response.getStatus()).isEqualTo(smStatus);
		assertThat(response.getExternalStatus()).isEqualTo(externalStatus);
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponseWithoutExternalCaseId() {

		// Arrange
		final var namespace = "namespace";
		final var smStatus = "someStatus";
		final var externalStatus = "externalStatus";
		final var statuses = new StatusesEntity().builder().withSupportManagementStatus(smStatus).withExternalStatus(externalStatus).build();
		final var classificationDisplayName = "classificationDisplayName";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.classification(new Classification().type("type"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"));

		// Act
		final var response = supportManagementMapper.toCaseStatusResponse(errand, namespace, statuses, classificationDisplayName);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isNull();
		assertThat(response.getCaseType()).isEqualTo("classificationDisplayName");
		assertThat(response.getStatus()).isEqualTo(smStatus);
		assertThat(response.getExternalStatus()).isEqualTo(externalStatus);
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponseWithNullModified() {

		// Arrange
		final var namespace = "namespace";
		final var smStatus = "someStatus";
		final var externalStatus = "externalStatus";
		final var statuses = new StatusesEntity().builder().withSupportManagementStatus(smStatus).withExternalStatus(externalStatus).build();
		final var classificationDisplayName = "classificationDisplayName";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.classification(new Classification().type("type"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = supportManagementMapper.toCaseStatusResponse(errand, namespace, statuses, classificationDisplayName);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("classificationDisplayName");
		assertThat(response.getStatus()).isEqualTo(smStatus);
		assertThat(response.getExternalStatus()).isEqualTo(externalStatus);
		assertThat(response.getLastStatusChange()).isNull();
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponseWithNullInStatuses() {

		// Arrange
		final var namespace = "namespace";
		final var statuses = new StatusesEntity().builder().build();
		final var classificationDisplayName = "classificationDisplayName";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.classification(new Classification().type("type"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = supportManagementMapper.toCaseStatusResponse(errand, namespace, statuses, classificationDisplayName);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("classificationDisplayName");
		assertThat(response.getStatus()).isNull();
		assertThat(response.getExternalStatus()).isNull();
		assertThat(response.getLastStatusChange()).isNull();
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponseWithNullInExternalStatus() {

		// Arrange
		final var namespace = "namespace";
		final var smStatus = "someStatus";
		final var statuses = new StatusesEntity().builder().withSupportManagementStatus(smStatus).build();
		final var classificationDisplayName = "classificationDisplayName";
		final var errand = new Errand()
			.id("errandId")
			.title("title")
			.classification(new Classification().type("type"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = supportManagementMapper.toCaseStatusResponse(errand, namespace, statuses, classificationDisplayName);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("classificationDisplayName");
		assertThat(response.getStatus()).isEqualTo(smStatus);
		assertThat(response.getExternalStatus()).isEqualTo(DEFAULT_EXTERNAL_STATUS);
		assertThat(response.getLastStatusChange()).isNull();
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

}
