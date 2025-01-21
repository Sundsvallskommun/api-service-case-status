package se.sundsvall.casestatus.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

import java.time.OffsetDateTime;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ExecutionInformationEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		MatcherAssert.assertThat(ExecutionInformationEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var lastSuccessfulExecution = now();
		final var municipalityId = "municipalityId";

		// Act
		final var result = ExecutionInformationEntity.builder()
			.withLastSuccessfulExecution(lastSuccessfulExecution)
			.withMunicipalityId(municipalityId)
			.build();
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getLastSuccessfulExecution()).isEqualTo(lastSuccessfulExecution);
		assertThat(result.getMunicipalityId()).isEqualTo(municipalityId);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(ExecutionInformationEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new ExecutionInformationEntity()).hasAllNullFieldsOrProperties();
	}

}
