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

class UnknownEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(UnknownEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void builder() {
		// Arrange
		final var flowInstanceId = "flowInstanceId";
		final var familyId = "familyId";
		final var status = "status";
		final var errandType = "errandType";
		final var contentType = "contentType";
		final var firstSubmitted = "firstSubmitted";
		final var lastStatusChange = "lastStatusChange";
		final var municipalityId = "municipalityId";

		// Act
		final var result = UnknownEntity.builder()
			.withFlowInstanceId(flowInstanceId)
			.withFamilyId(familyId)
			.withStatus(status)
			.withErrandType(errandType)
			.withContentType(contentType)
			.withFirstSubmitted(firstSubmitted)
			.withLastStatusChange(lastStatusChange)
			.withMunicipalityId(municipalityId)
			.build();

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getFlowInstanceId()).isEqualTo(flowInstanceId);
		assertThat(result.getFamilyId()).isEqualTo(familyId);
		assertThat(result.getStatus()).isEqualTo(status);
		assertThat(result.getErrandType()).isEqualTo(errandType);
		assertThat(result.getContentType()).isEqualTo(contentType);
		assertThat(result.getFirstSubmitted()).isEqualTo(firstSubmitted);
		assertThat(result.getLastStatusChange()).isEqualTo(lastStatusChange);
		assertThat(result.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(UnknownEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new UnknownEntity()).hasAllNullFieldsOrProperties();
	}

}
