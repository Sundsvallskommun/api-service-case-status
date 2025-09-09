package se.sundsvall.casestatus.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class StatusesEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(StatusesEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {

		// Arrange
		final var id = 12;
		final var supportManagementStatus = "supportManagementStatus";
		final var supportManagementDisplayName = "supportManagementDisplayName";
		final var caseManagementStatus = "caseManagementStatus";
		final var caseManagementDisplayName = "caseManagementDisplayName";
		final var oepStatus = "oepStatus";
		final var oepDisplayName = "oepDisplayName";
		final var externalStatus = "externalStatus";
		final var externalDisplayName = "externalDisplayName";
		// Act
		final var result = StatusesEntity.builder()
			.withId(id)
			.withSupportManagementStatus(supportManagementStatus)
			.withSupportManagementDisplayName(supportManagementDisplayName)
			.withCaseManagementStatus(caseManagementStatus)
			.withCaseManagementDisplayName(caseManagementDisplayName)
			.withOepStatus(oepStatus)
			.withOepDisplayName(oepDisplayName)
			.withExternalStatus(externalStatus)
			.withExternalDisplayName(externalDisplayName)
			.build();
		// Assert
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getSupportManagementStatus()).isEqualTo(supportManagementStatus);
		assertThat(result.getSupportManagementDisplayName()).isEqualTo(supportManagementDisplayName);
		assertThat(result.getCaseManagementStatus()).isEqualTo(caseManagementStatus);
		assertThat(result.getCaseManagementDisplayName()).isEqualTo(caseManagementDisplayName);
		assertThat(result.getOepStatus()).isEqualTo(oepStatus);
		assertThat(result.getOepDisplayName()).isEqualTo(oepDisplayName);
		assertThat(result.getExternalStatus()).isEqualTo(externalStatus);
		assertThat(result.getExternalDisplayName()).isEqualTo(externalDisplayName);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(StatusesEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new StatusesEntity()).hasAllNullFieldsOrProperties();
	}
}
