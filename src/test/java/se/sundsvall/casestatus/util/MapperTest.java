package se.sundsvall.casestatus.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith({
	ResourceLoaderExtension.class
})
class MapperTest {

	private Mapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new Mapper();
	}

	@Test
	void toCacheCompanyCaseStatus(
		@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML,
		@Load(value = "/xml/getErrandStatus.xml") final String getErrandStatusXML) {

		final var errandDoc = Jsoup.parse(getErrandXML);
		final var errandStatusDoc = Jsoup.parse(getErrandStatusXML);

		final var result = mapper.toCacheCompanyCaseStatus(errandStatusDoc, errandDoc, "someOrganisationNumber", "2281");

		assertThat(result)
			.isNotNull()
			.hasNoNullFieldsOrPropertiesExcept("sysStartTime", "sysEndtime");
		assertThat(result.getStatus()).isEqualTo("Inskickat");
		assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getFlowInstanceId()).isEqualTo("2101");
		assertThat(result.getFamilyId()).isEqualTo("381");
		assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
		assertThat(result.getContentType()).isEqualTo("SUBMITTED");
		assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getOrganisationNumber()).isEqualTo("someOrganisationNumber");

	}

	@Test
	void toCachePrivateCaseStatus(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML,
		@Load(value = "/xml/getErrandStatus.xml") final String getErrandStatusXML) {

		final var errandDoc = Jsoup.parse(getErrandXML);
		final var errandStatusDoc = Jsoup.parse(getErrandStatusXML);

		final var result = mapper.toCachePrivateCaseStatus(errandStatusDoc, errandDoc, "somePersonId", "2281");

		assertThat(result)
			.isNotNull()
			.hasNoNullFieldsOrPropertiesExcept("sysStartTime", "sysEndtime");
		assertThat(result.getStatus()).isEqualTo("Inskickat");
		assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getFlowInstanceId()).isEqualTo("2101");
		assertThat(result.getFamilyId()).isEqualTo("381");
		assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
		assertThat(result.getContentType()).isEqualTo("SUBMITTED");
		assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getPersonId()).isEqualTo("somePersonId");
	}

	@Test
	void toCacheUnknowCaseStatus(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML,
		@Load(value = "/xml/getErrandStatus.xml") final String getErrandStatusXML) {

		final var errandDoc = Jsoup.parse(getErrandXML);
		final var errandStatusDoc = Jsoup.parse(getErrandStatusXML);

		final var result = mapper.toCacheUnknownCaseStatus(errandStatusDoc, errandDoc, "2281");

		assertThat(result)
			.isNotNull()
			.hasNoNullFieldsOrPropertiesExcept("sysStartTime", "sysEndtime");
		assertThat(result.getStatus()).isEqualTo("Inskickat");
		assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
		assertThat(result.getFlowInstanceId()).isEqualTo("2101");
		assertThat(result.getFamilyId()).isEqualTo("381");
		assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
		assertThat(result.getContentType()).isEqualTo("SUBMITTED");
		assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
	}

}
