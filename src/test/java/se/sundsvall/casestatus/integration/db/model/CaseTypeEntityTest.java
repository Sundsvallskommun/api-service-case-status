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

class CaseTypeEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(CaseTypeEntity.class, allOf(
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
		final var enumValue = "enumValue";
		final var description = "description";
		final var municipalityId = "municipalityId";

		// Act
		final var result = CaseTypeEntity.builder()
			.withId(id)
			.withEnumValue(enumValue)
			.withDescription(description)
			.withMunicipalityId(municipalityId)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getEnumValue()).isEqualTo(enumValue);
		assertThat(result.getDescription()).isEqualTo(description);
		assertThat(result.getMunicipalityId()).isEqualTo(municipalityId);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(CaseTypeEntity.builder().build()).hasAllNullFieldsOrPropertiesExcept("id").satisfies(entity -> {
			assertThat(entity.getId()).isZero();
		});
		assertThat(new CaseTypeEntity()).hasAllNullFieldsOrPropertiesExcept("id").satisfies(entity -> {
			assertThat(entity.getId()).isZero();
		});
	}

}
