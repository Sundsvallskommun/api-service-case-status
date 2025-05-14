package se.sundsvall.casestatus.service.scheduler.cache;

import static org.assertj.core.api.Assertions.assertThat;

import generated.client.oep_integrator.CaseStatus;
import generated.client.oep_integrator.ModelCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith({
	ResourceLoaderExtension.class
})
class MapperTest {

	@Test
	void toCompanyCaseEntity(
		@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML) {

		final var errandDoc = new ModelCase().payload(getErrandXML);
		final var errandStatusDoc = new CaseStatus().name("Inskickat");

		final var result = Mapper.toCompanyCaseEntity(errandStatusDoc, errandDoc, "someOrganisationNumber", "2281");

		assertThat(result)
			.isNotNull()
			.hasNoNullFieldsOrPropertiesExcept("personId", "sysStartTime", "sysEndtime", "contentType");
		assertThat(result.getStatus()).isEqualTo("Inskickat");
		assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getFlowInstanceId()).isEqualTo("2101");
		assertThat(result.getFamilyId()).isEqualTo("381");
		assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
		assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getOrganisationNumber()).isEqualTo("someOrganisationNumber");

	}

	@Test
	void toPrivateCaseEntity(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML) {

		final var errandDoc = new ModelCase().payload(getErrandXML);
		final var errandStatusDoc = new CaseStatus().name("Inskickat");

		final var result = Mapper.toPrivateCaseEntity(errandStatusDoc, errandDoc, "somePersonId", "2281");

		assertThat(result)
			.isNotNull()
			.hasNoNullFieldsOrPropertiesExcept("organisationNumber", "sysStartTime", "sysEndtime", "contentType");
		assertThat(result.getStatus()).isEqualTo("Inskickat");
		assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getFlowInstanceId()).isEqualTo("2101");
		assertThat(result.getFamilyId()).isEqualTo("381");
		assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
		assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getPersonId()).isEqualTo("somePersonId");
	}

	@Test
	void toCacheUnknowCaseStatus(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML) {

		final var errandDoc = new ModelCase().payload(getErrandXML);
		final var errandStatusDoc = new CaseStatus().name("Inskickat");

		final var result = Mapper.toUnknownCaseEntity(errandStatusDoc, errandDoc, "2281");

		assertThat(result)
			.isNotNull()
			.hasNoNullFieldsOrPropertiesExcept("sysStartTime", "sysEndtime", "organisationNumber", "personId", "contentType");
		assertThat(result.getStatus()).isEqualTo("Inskickat");
		assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getFlowInstanceId()).isEqualTo("2101");
		assertThat(result.getFamilyId()).isEqualTo("381");
		assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
		assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
	}

}
