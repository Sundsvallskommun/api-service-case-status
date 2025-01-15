package se.sundsvall.casestatus.integration.opene.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.service.scheduler.domain.FamilyId;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith({
	MockitoExtension.class, ResourceLoaderExtension.class
})
class OpenEIntegrationTests {

	@Mock
	private final FamilyId familyId = FamilyId.ROKKANALELDSTAD;
	@Mock
	private OpenEClient mockOpenEClient;
	@InjectMocks
	private OpenEIntegration openEIntegration;

	@Test
	void getPdf_ok() {
		when(mockOpenEClient.getPDF(any(String.class))).thenReturn("someAnswer".getBytes(StandardCharsets.UTF_8));

		final var response = openEIntegration.getPdf("someExternalCaseId");

		assertThat(response).isPresent();

		verify(mockOpenEClient).getPDF(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getPdf_error() {

		when(mockOpenEClient.getPDF(any(String.class))).thenThrow(new NullPointerException());

		final var response = openEIntegration.getPdf("someExternalCaseId");

		assertThat(response).isEmpty();

		verify(mockOpenEClient).getPDF(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getErrandIds_ok() {
		when(mockOpenEClient.getErrandIds(any(String.class))).thenReturn("someAnswer".getBytes());
		when(familyId.getValue()).thenReturn(123);
		final var response = openEIntegration.getErrandIds(familyId);

		assertThat(response).isNotNull();
		verify(mockOpenEClient).getErrandIds(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getErrandIds_error() {

		when(mockOpenEClient.getErrandIds(any(String.class))).thenThrow(new NullPointerException());

		final var response = openEIntegration.getErrandIds(familyId);

		assertThat(response).isEmpty();

		verify(mockOpenEClient).getErrandIds(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getErrand_ok() {
		when(mockOpenEClient.getErrand(any(String.class))).thenReturn("someAnswer".getBytes());
		final var response = openEIntegration.getErrand("someFlowInstanceId");

		assertThat(response).isNotNull();
		verify(mockOpenEClient).getErrand(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getErrand_error() {

		when(mockOpenEClient.getErrand(any(String.class))).thenThrow(new NullPointerException());

		final var response = openEIntegration.getErrand("someFlowInstanceId");

		assertThat(response).isEmpty();

		verify(mockOpenEClient).getErrand(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getErrandStatus_ok() {
		when(mockOpenEClient.getErrandStatus(any(String.class))).thenReturn("someAnswer".getBytes());

		final var response = openEIntegration.getErrandStatus("someFlowInstanceId");

		assertThat(response).isNotNull();

		verify(mockOpenEClient).getErrandStatus(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getErrandStatus_error() {

		when(mockOpenEClient.getErrandStatus(any(String.class))).thenThrow(new NullPointerException());

		final var response = openEIntegration.getErrandStatus("someFlowInstanceId");

		assertThat(response).isEmpty();

		verify(mockOpenEClient).getErrandStatus(any(String.class));
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getCaseStatuses(@Load(value = "/xml/getErrand_ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR.xml") final String getErrandXML,
		@Load(value = "/xml/getErrandStatus.xml") final String getErrandStatusXML) {
		final var legalId = "someLegalId";
		final var flowInstanceId = "123";
		final var municipalityId = "someMunicipalityId";
		final var response = "<FlowInstances><flowinstance><flowInstanceId>" + flowInstanceId + "</flowInstanceId></flowinstance></FlowInstances>";

		when(mockOpenEClient.getErrands(legalId)).thenReturn(response.getBytes(StandardCharsets.ISO_8859_1));
		when(mockOpenEClient.getErrand(flowInstanceId)).thenReturn(getErrandXML.getBytes(StandardCharsets.ISO_8859_1));
		when(mockOpenEClient.getErrandStatus(flowInstanceId)).thenReturn(getErrandStatusXML.getBytes(StandardCharsets.ISO_8859_1));

		final var result = openEIntegration.getCaseStatuses(municipalityId, legalId);

		assertThat(result).isNotEmpty();
		verify(mockOpenEClient).getErrands(legalId);
		verify(mockOpenEClient).getErrand(flowInstanceId);
		verify(mockOpenEClient).getErrandStatus(flowInstanceId);
		verifyNoMoreInteractions(mockOpenEClient);
	}

	@Test
	void getCaseStatusesError() {
		final var legalId = "someLegalId";
		final var municipalityId = "someMunicipalityId";

		when(mockOpenEClient.getErrands(legalId)).thenThrow(new NullPointerException());

		final var result = openEIntegration.getCaseStatuses(municipalityId, legalId);

		assertThat(result).isEmpty();
		verify(mockOpenEClient).getErrands(legalId);
		verifyNoMoreInteractions(mockOpenEClient);
	}
}
