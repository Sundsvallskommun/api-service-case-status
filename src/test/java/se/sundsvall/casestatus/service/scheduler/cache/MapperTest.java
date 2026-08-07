package se.sundsvall.casestatus.service.scheduler.cache;

import generated.client.oep_integrator.CaseStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({
	ResourceLoaderExtension.class
})
class MapperTest {

	@Test
	void toCompanyCaseEntity(
		@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML) {

		final var payload = Mapper.parsePayload(getErrandXML);
		final var errandStatusDoc = new CaseStatus().name("Inskickat");

		final var result = Mapper.toCompanyCaseEntity(errandStatusDoc, payload, null, "someOrganisationNumber", "2281");

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
	void toCompanyCaseEntityWhenPayloadIsEmpty() {

		final var payload = Mapper.parsePayload("<FlowInstance></FlowInstance>");
		final var errandStatusDoc = new CaseStatus().name("Inskickat");

		final var result = Mapper.toCompanyCaseEntity(errandStatusDoc, payload, "contentType", "someOrganisationNumber", "2281");

		assertThat(result).isNotNull();
		assertThat(result.getStatus()).isEqualTo("Inskickat");
		assertThat(result.getFlowInstanceId()).isNull();
		assertThat(result.getFamilyId()).isNull();
		assertThat(result.getErrandType()).isNull();
		assertThat(result.getFirstSubmitted()).isNull();
		assertThat(result.getLastStatusChange()).isNull();
		assertThat(result.getOrganisationNumber()).isEqualTo("someOrganisationNumber");
	}
}
