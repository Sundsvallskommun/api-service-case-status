package se.sundsvall.casestatus.service.scheduler.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.citizen.CitizenIntegration;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.opene.rest.OpenEIntegration;
import se.sundsvall.casestatus.service.scheduler.cache.domain.FamilyId;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class CaseStatusCacheWorkerTest {

	@Mock
	private OpenEIntegration openEIntegrationMock;

	@Mock
	private CitizenIntegration citizenIntegrationMock;

	@Mock
	private CaseRepository caseRepositoryMock;

	@InjectMocks
	private CaseStatusCacheWorker caseStatusCacheWorker;

	@Test
	void cacheStatusesForFamilyID_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR(@Load(value = "/xml/getErrandList_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandIdsXML,
		@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML,
		@Load(value = "/xml/getErrandStatus.xml") final String getErrandStatusXML) {

		final FamilyId familyId = FamilyId.ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR;

		when(openEIntegrationMock.getErrandIds(any(FamilyId.class))).thenReturn(getErrandIdsXML.getBytes());
		when(openEIntegrationMock.getErrand(any())).thenReturn(getErrandXML.getBytes());
		when(openEIntegrationMock.getErrandStatus(any())).thenReturn(getErrandStatusXML.getBytes());

		caseStatusCacheWorker.cacheStatusesForFamilyId(familyId);

		verify(openEIntegrationMock).getErrandIds(any());
		verify(openEIntegrationMock, times(2)).getErrand(any());
		verify(openEIntegrationMock, times(2)).getErrandStatus(any());
		verify(caseRepositoryMock, times(2)).save(any());
	}

	@Test
	void cacheStatusesForFamilyID(@Load(value = "/xml/getErrandList_ROKKANALELDSTAD.xml") final String getErrandIdsXML,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD1.xml") final String getErrandXML1,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD2.xml") final String getErrandXML2,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD3.xml") final String getErrandXML3,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD4.xml") final String getErrandXML4,
		@Load(value = "/xml/getErrandStatus.xml") final String getErrandStatusXML) {

		final FamilyId familyId = FamilyId.ROKKANALELDSTAD;

		when(openEIntegrationMock.getErrandIds(any(FamilyId.class))).thenReturn(getErrandIdsXML.getBytes());
		when(openEIntegrationMock.getErrand(any())).thenReturn(getErrandXML1.getBytes()).thenReturn(getErrandXML2.getBytes()).thenReturn(getErrandXML3.getBytes()).thenReturn(getErrandXML4.getBytes());
		when(openEIntegrationMock.getErrandStatus(any())).thenReturn(getErrandStatusXML.getBytes());
		when(citizenIntegrationMock.getPersonId(any())).thenReturn("somePersonId");

		caseStatusCacheWorker.cacheStatusesForFamilyId(familyId);

		verify(openEIntegrationMock).getErrandIds(any());
		verify(openEIntegrationMock, times(4)).getErrand(any());
		verify(openEIntegrationMock, times(4)).getErrandStatus(any());
		verify(caseRepositoryMock, times(4)).save(any());
		verifyNoMoreInteractions(openEIntegrationMock);
	}

}
