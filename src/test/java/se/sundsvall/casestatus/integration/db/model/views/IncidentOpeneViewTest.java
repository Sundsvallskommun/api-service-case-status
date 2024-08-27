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

class IncidentOpeneViewTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(IncidentOpeneView.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {

		// Arrange
		final var incidentId = 12;
		final var openEId = "openEId";

		// Act
		final var result = IncidentOpeneView.builder()
			.withIncidentId(incidentId)
			.withOpenEId(openEId)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getIncidentId()).isEqualTo(incidentId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(IncidentOpeneView.builder().build()).hasAllNullFieldsOrPropertiesExcept("incidentId").satisfies(entity -> {
			assertThat(entity.getIncidentId()).isZero();
		});
		assertThat(new IncidentOpeneView()).hasAllNullFieldsOrPropertiesExcept("incidentId").satisfies(entity -> {
			assertThat(entity.getIncidentId()).isZero();
		});
	}

}
