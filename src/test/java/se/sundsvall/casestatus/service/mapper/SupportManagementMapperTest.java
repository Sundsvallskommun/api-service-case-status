package se.sundsvall.casestatus.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class SupportManagementMapperTest {

	@Test
	void toCaseStatusResponse_withExternalCaseId() {
		// Arrange
		final var namespace = "namespace";
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = SupportManagementMapper.toCaseStatusResponse(errand, namespace);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("someType");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponse_withoutExternalCaseId() {
		// Arrange
		final var namespace = "namespace";
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"));

		// Act
		final var response = SupportManagementMapper.toCaseStatusResponse(errand, namespace);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isNull();
		assertThat(response.getCaseType()).isEqualTo("someType");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}

	@Test
	void toCaseStatusResponse_withNullModified() {
		// Arrange
		final var namespace = "namespace";
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = SupportManagementMapper.toCaseStatusResponse(errand, namespace);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("someType");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getLastStatusChange()).isEqualTo("Saknas");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
	}
}
