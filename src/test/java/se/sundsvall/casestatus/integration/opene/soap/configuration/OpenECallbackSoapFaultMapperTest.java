package se.sundsvall.casestatus.integration.opene.soap.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.xml.soap.Detail;
import jakarta.xml.soap.DetailEntry;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.exception.ServerProblem;

class OpenECallbackSoapFaultMapperTest {

	@Test
	void testConvertToThrowableProblem_AccessDeniedFault() {
		// Arrange
		final var soapFault = mock(SOAPFault.class);
		final var detail = mock(Detail.class);
		final var detailEntry = mock(DetailEntry.class);
		when(detailEntry.getLocalName()).thenReturn("AccessDeniedFault");
		when(detail.getDetailEntries()).thenReturn(Collections.singletonList(detailEntry).iterator());
		when(soapFault.getDetail()).thenReturn(detail);
		when(soapFault.getFaultString()).thenReturn("Access denied");
		final var soapFaultException = new SOAPFaultException(soapFault);

		// Act
		final var result = OpenECallbackSoapFaultMapper.convertToThrowableProblem(soapFaultException);

		// Assert
		assertThat(result).isInstanceOf(ServerProblem.class);
		assertThat(result.getStatus()).isEqualTo(org.zalando.problem.Status.UNAUTHORIZED);
		assertThat(result.getDetail()).isEqualTo("Access denied");
	}

	@Test
	void testConvertToThrowableProblem_FlowInstanceNotFoundFault() {
		// Arrange
		final var soapFault = mock(SOAPFault.class);
		final var detail = mock(Detail.class);
		final var detailEntry = mock(DetailEntry.class);
		when(detailEntry.getLocalName()).thenReturn("FlowInstanceNotFoundFault");
		when(detail.getDetailEntries()).thenReturn(Collections.singletonList(detailEntry).iterator());
		when(soapFault.getDetail()).thenReturn(detail);
		when(soapFault.getFaultString()).thenReturn("Flow instance not found");
		final var soapFaultException = new SOAPFaultException(soapFault);

		// Act
		final var result = OpenECallbackSoapFaultMapper.convertToThrowableProblem(soapFaultException);

		// Assert
		assertThat(result).isInstanceOf(ClientProblem.class);
		assertThat(result.getStatus()).isEqualTo(org.zalando.problem.Status.NOT_FOUND);
		assertThat(result.getDetail()).isEqualTo("Flow instance not found");
	}

	@Test
	void testConvertToThrowableProblem_UnknownFault() {
		// Arrange
		final var soapFault = mock(SOAPFault.class);
		final var detail = mock(Detail.class);
		final var detailEntry = mock(DetailEntry.class);
		when(detailEntry.getLocalName()).thenReturn("UnknownFault");
		when(detail.getDetailEntries()).thenReturn(Collections.singletonList(detailEntry).iterator());
		when(soapFault.getDetail()).thenReturn(detail);
		when(soapFault.getFaultString()).thenReturn("Unknown fault");
		final var soapFaultException = new SOAPFaultException(soapFault);

		// Act
		final var result = OpenECallbackSoapFaultMapper.convertToThrowableProblem(soapFaultException);

		// Assert
		assertThat(result).isInstanceOf(ThrowableProblem.class);
		assertThat(result.getStatus()).isEqualTo(org.zalando.problem.Status.INTERNAL_SERVER_ERROR);
		assertThat(result.getDetail()).isEqualTo("Unknown fault");
	}
}
