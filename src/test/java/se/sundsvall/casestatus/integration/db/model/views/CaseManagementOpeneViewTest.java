package se.sundsvall.casestatus.integration.db.model.views;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class CaseManagementOpeneViewTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseManagementOpeneView.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {

		// Arrange
		final var caseManagementId = "caseManagementId";
		final var openEId = "openEId";

		// Act
		final var result = CaseManagementOpeneView.builder()
			.withCaseManagementId(caseManagementId)
			.withOpenEId(openEId)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getCaseManagementId()).isEqualTo(caseManagementId);
		assertThat(result.getOpenEId()).isEqualTo(openEId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseManagementOpeneView.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new CaseManagementOpeneView()).hasAllNullFieldsOrProperties();
	}

}
