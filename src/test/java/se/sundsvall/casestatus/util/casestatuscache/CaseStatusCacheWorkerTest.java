package se.sundsvall.casestatus.util.casestatuscache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casestatus.integration.citizen.CitizenIntegration;
import se.sundsvall.casestatus.integration.db.DbIntegration;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;
import se.sundsvall.casestatus.util.Mapper;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith({ MockitoExtension.class, ResourceLoaderExtension.class })
class CaseStatusCacheWorkerTest {

	@Mock
	private OpenEIntegration openEIntegration;

	@Mock
	private DbIntegration dbIntegration;

	@Mock
	private CitizenIntegration citizenIntegration;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private Mapper mapper;

	private CaseStatusCacheWorker caseStatusCacheWorker;

	@BeforeEach
	void setUp() {
		caseStatusCacheWorker = new CaseStatusCacheWorker(openEIntegration, citizenIntegration, dbIntegration, mapper);
	}

	@Test
	void cacheStatusesForFamilyID_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR(@Load(value = "/xml/getErrandList_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") String getErrandIdsXML,
		@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") String getErrandXML,
		@Load(value = "/xml/getErrandStatus.xml") String getErrandStatusXML) {

		final FamilyId familyId = FamilyId.ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR;

		when(openEIntegration.getErrandIds(any(FamilyId.class))).thenReturn(getErrandIdsXML.getBytes());
		when(openEIntegration.getErrand(any())).thenReturn(getErrandXML.getBytes());
		when(openEIntegration.getErrandStatus(any())).thenReturn(getErrandStatusXML.getBytes());

		caseStatusCacheWorker.cacheStatusesForFamilyID(familyId);

		verify(openEIntegration).getErrandIds(any());
		verify(openEIntegration, times(2)).getErrand(any());
		verify(openEIntegration, times(2)).getErrandStatus(any());
		verify(dbIntegration, times(2)).writeToCompanyTable(any());
		verify(mapper, times(2)).toCacheCompanyCaseStatus(any(), any(), any());
	}

	@Test
	void cacheStatusesForFamilyID_NYBYGGNADSKARTA(@Load(value = "/xml/getErrandList_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") String getErrandIdsXML,
		@Load(value = "/xml/getErrand_nybyggnad.xml") String getErrandXML,
		@Load(value = "/xml/getErrandStatus.xml") String getErrandStatusXML) {

		final FamilyId familyId = FamilyId.NYBYGGNADSKARTA;

		when(openEIntegration.getErrandIds(any(FamilyId.class))).thenReturn(getErrandIdsXML.getBytes());
		when(openEIntegration.getErrand(any())).thenReturn(getErrandXML.getBytes());
		when(openEIntegration.getErrandStatus(any())).thenReturn(getErrandStatusXML.getBytes());
		when(citizenIntegration.getPersonId(any())).thenReturn("somePersonId");

		caseStatusCacheWorker.cacheStatusesForFamilyID(familyId);

		verify(openEIntegration).getErrandIds(any());
		verify(openEIntegration, times(2)).getErrand(any());
		verify(openEIntegration, times(2)).getErrandStatus(any());
		verify(dbIntegration, times(2)).writeToPrivateTable(any());
		verify(mapper, times(2)).toCachePrivateCaseStatus(any(), any(), any());
	}

	@Test
	void cacheStatusesForFamilyID(@Load(value = "/xml/getErrandList_ROKKANALELDSTAD.xml") String getErrandIdsXML,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD1.xml") String getErrandXML1,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD2.xml") String getErrandXML2,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD3.xml") String getErrandXML3,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD4.xml") String getErrandXML4,
		@Load(value = "/xml/getErrandStatus.xml") String getErrandStatusXML) {

		final FamilyId familyId = FamilyId.ROKKANALELDSTAD;

		when(openEIntegration.getErrandIds(any(FamilyId.class))).thenReturn(getErrandIdsXML.getBytes());
		when(openEIntegration.getErrand(any())).thenReturn(getErrandXML1.getBytes()).thenReturn(getErrandXML2.getBytes()).thenReturn(getErrandXML3.getBytes()).thenReturn(getErrandXML4.getBytes());
		when(openEIntegration.getErrandStatus(any())).thenReturn(getErrandStatusXML.getBytes());
		when(citizenIntegration.getPersonId(any())).thenReturn("somePersonId");

		caseStatusCacheWorker.cacheStatusesForFamilyID(familyId);

		verify(openEIntegration).getErrandIds(any());
		verify(openEIntegration, times(4)).getErrand(any());
		verify(openEIntegration, times(4)).getErrandStatus(any());
		verify(dbIntegration, times(2)).writeToCompanyTable(any());
		verify(dbIntegration).writeToPrivateTable(any());
		verify(dbIntegration).writeToUnknownTable(any());
		verify(mapper, times(2)).toCacheCompanyCaseStatus(any(), any(), any());
		verify(mapper).toCachePrivateCaseStatus(any(), any(), any());
		verify(mapper).toCacheUnknowCaseStatus(any(), any());
		verifyNoMoreInteractions(openEIntegration);
		verifyNoMoreInteractions(mapper);
	}
}
