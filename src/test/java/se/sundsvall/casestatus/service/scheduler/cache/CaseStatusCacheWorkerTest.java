package se.sundsvall.casestatus.service.scheduler.cache;

import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.client.oep_integrator.CaseEnvelope;
import generated.client.oep_integrator.CaseStatus;
import generated.client.oep_integrator.InstanceType;
import generated.client.oep_integrator.ModelCase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.scheduler.cache.domain.FamilyId;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class CaseStatusCacheWorkerTest {

	@Mock
	private Dept44HealthUtility dept44HealthUtility;

	@Mock
	private OepIntegratorClient oepIntegratorClientMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private CaseRepository caseRepositoryMock;

	@InjectMocks
	private CaseStatusCacheWorker caseStatusCacheWorker;

	@Test
	void cacheStatusesForFamilyID_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML) {

		final var familyId = FamilyId.ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR;
		final var municipalityId = familyId.getMunicipalityId();
		final var instanceType = InstanceType.EXTERNAL;

		// Mock CaseStatusCache
		final var caseStatusCacheMock = Mockito.mock(CaseStatusCache.class);
		when(caseStatusCacheMock.isProduction()).thenReturn(false);

		try (final var mockedContextUtil = Mockito.mockStatic(ContextUtil.class)) {
			mockedContextUtil.when(() -> ContextUtil.getBean(CaseStatusCache.class)).thenReturn(caseStatusCacheMock);

			when(oepIntegratorClientMock.getCases(municipalityId, instanceType, familyId.getValue())).thenReturn(List.of(new CaseEnvelope().flowInstanceId("someFlowInstanceId")));
			when(oepIntegratorClientMock.getCase(municipalityId, instanceType, "someFlowInstanceId")).thenReturn(new ModelCase().payload(getErrandXML));
			when(oepIntegratorClientMock.getCaseStatus(any(), any(), any())).thenReturn(new CaseStatus());

			caseStatusCacheWorker.cacheStatusesForFamilyId(familyId);

			verify(oepIntegratorClientMock).getCase(any(), any(), any());
			verify(oepIntegratorClientMock).getCaseStatus(any(), any(), any());
			verify(caseRepositoryMock).save(any());
		}
	}

	@Test
	void cacheStatusesForFamilyID(
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD1.xml") final String getErrandXML1,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD2.xml") final String getErrandXML2,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD3.xml") final String getErrandXML3,
		@Load(value = "/xml/getErrand_ROKKANALELDSTAD4.xml") final String getErrandXML4) {

		final var familyId = FamilyId.ROKKANALELDSTAD;
		final var municipalityId = familyId.getMunicipalityId();
		final var instanceType = InstanceType.EXTERNAL;

		// Mock CaseStatusCache
		final var caseStatusCacheMock = Mockito.mock(CaseStatusCache.class);
		when(caseStatusCacheMock.isProduction()).thenReturn(false);

		try (final var mockedContextUtil = Mockito.mockStatic(ContextUtil.class)) {
			mockedContextUtil.when(() -> ContextUtil.getBean(CaseStatusCache.class)).thenReturn(caseStatusCacheMock);

			when(oepIntegratorClientMock.getCases(municipalityId, instanceType, FamilyId.ROKKANALELDSTAD.getValue())).thenReturn(List.of(
				new CaseEnvelope().flowInstanceId("flowInstanceId1"),
				new CaseEnvelope().flowInstanceId("flowInstanceId2"),
				new CaseEnvelope().flowInstanceId("flowInstanceId3"),
				new CaseEnvelope().flowInstanceId("flowInstanceId4")

			));

			when(oepIntegratorClientMock.getCase(municipalityId, instanceType, "flowInstanceId1")).thenReturn(new ModelCase().payload(getErrandXML1));
			when(oepIntegratorClientMock.getCase(municipalityId, instanceType, "flowInstanceId2")).thenReturn(new ModelCase().payload(getErrandXML2));
			when(oepIntegratorClientMock.getCase(municipalityId, instanceType, "flowInstanceId3")).thenReturn(new ModelCase().payload(getErrandXML3));
			when(oepIntegratorClientMock.getCase(municipalityId, instanceType, "flowInstanceId4")).thenReturn(new ModelCase().payload(getErrandXML4));

			when(oepIntegratorClientMock.getCaseStatus(any(), any(), any())).thenReturn(new CaseStatus());
			when(partyIntegrationMock.getPartyIdByLegalId(any(), any())).thenReturn(Map.of(PRIVATE, "somePersonId"));

			caseStatusCacheWorker.cacheStatusesForFamilyId(familyId);

			verify(oepIntegratorClientMock, times(4)).getCase(any(), any(), any());
			verify(oepIntegratorClientMock, times(4)).getCaseStatus(any(), any(), any());
			verifyNoMoreInteractions(oepIntegratorClientMock);
		}
	}

	@Test
	void cacheStatusesForFamilyID_EmptyResponse() {
		final var familyId = FamilyId.ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR;
		final var municipalityId = familyId.getMunicipalityId();
		final var instanceType = InstanceType.EXTERNAL;
		ReflectionTestUtils.setField(caseStatusCacheWorker, "jobName", "cache_job");

		// Mock CaseStatusCache
		final var caseStatusCacheMock = Mockito.mock(CaseStatusCache.class);
		when(caseStatusCacheMock.isProduction()).thenReturn(false);

		try (final var mockedContextUtil = Mockito.mockStatic(ContextUtil.class)) {
			mockedContextUtil.when(() -> ContextUtil.getBean(CaseStatusCache.class)).thenReturn(caseStatusCacheMock);
			when(oepIntegratorClientMock.getCases(municipalityId, instanceType, familyId.getValue())).thenReturn(List.of());

			caseStatusCacheWorker.cacheStatusesForFamilyId(familyId);

			verify(oepIntegratorClientMock).getCases(municipalityId, instanceType, familyId.getValue());
			verify(dept44HealthUtility).setHealthIndicatorUnhealthy("cache_job", "Unable to get errandIds for familyId: " + familyId);
			verifyNoMoreInteractions(oepIntegratorClientMock, partyIntegrationMock, caseRepositoryMock);
		}
	}
}
