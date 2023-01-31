package se.sundsvall.casestatus.util;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({ResourceLoaderExtension.class})
class MapperTest {

    private Mapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Mapper();
    }

    @Test
    void toCacheCompanyCaseStatus(
            @Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") String getErrandXML,
            @Load(value = "/xml/getErrandStatus.xml") String getErrandStatusXML) {

        var errandDoc = Jsoup.parse(getErrandXML);
        var errandStatusDoc = Jsoup.parse(getErrandStatusXML);


        var result = mapper.toCacheCompanyCaseStatus(errandStatusDoc, errandDoc, "someOrganisationNumber");

        assertThat(result).isNotNull();
        assertThat(result).hasNoNullFieldsOrPropertiesExcept("sysStartTime", "sysEndtime");
        assertThat(result.getStatus()).isEqualTo("Inskickat");
        assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
        assertThat(result.getFlowInstanceID()).isEqualTo("2101");
        assertThat(result.getFamilyID()).isEqualTo("381");
        assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
        assertThat(result.getContentType()).isEqualTo("SUBMITTED");
        assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
        assertThat(result.getOrganisationNumber()).isEqualTo("someOrganisationNumber");


    }

    @Test
    void toCachePrivateCaseStatus(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") String getErrandXML,
                                  @Load(value = "/xml/getErrandStatus.xml") String getErrandStatusXML) {


        var errandDoc = Jsoup.parse(getErrandXML);
        var errandStatusDoc = Jsoup.parse(getErrandStatusXML);


        var result = mapper.toCachePrivateCaseStatus(errandStatusDoc, errandDoc, "somePersonId");

        assertThat(result).isNotNull();
        assertThat(result).hasNoNullFieldsOrPropertiesExcept("sysStartTime", "sysEndtime");
        assertThat(result.getStatus()).isEqualTo("Inskickat");
        assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
        assertThat(result.getFlowInstanceID()).isEqualTo("2101");
        assertThat(result.getFamilyID()).isEqualTo("381");
        assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
        assertThat(result.getContentType()).isEqualTo("SUBMITTED");
        assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
        assertThat(result.getPersonId()).isEqualTo("somePersonId");
    }

    @Test
    void toCacheUnknowCaseStatus(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") String getErrandXML,
                                 @Load(value = "/xml/getErrandStatus.xml") String getErrandStatusXML) {

        var errandDoc = Jsoup.parse(getErrandXML);
        var errandStatusDoc = Jsoup.parse(getErrandStatusXML);


        var result = mapper.toCacheUnknowCaseStatus(errandStatusDoc, errandDoc);

        assertThat(result).isNotNull();
        assertThat(result).hasNoNullFieldsOrPropertiesExcept("sysStartTime", "sysEndtime");
        assertThat(result.getStatus()).isEqualTo("Inskickat");
        assertThat(result.getFirstSubmitted()).isEqualTo("2022-01-20 11:29");
        assertThat(result.getFlowInstanceID()).isEqualTo("2101");
        assertThat(result.getFamilyID()).isEqualTo("381");
        assertThat(result.getErrandType()).isEqualTo("�ndring eller avslut av tillst�ndspliktig f�rs�ljning av tobaksvaror - anm�lan");
        assertThat(result.getContentType()).isEqualTo("SUBMITTED");
        assertThat(result.getLastStatusChange()).isEqualTo("2022-01-20 11:29");
    }
}