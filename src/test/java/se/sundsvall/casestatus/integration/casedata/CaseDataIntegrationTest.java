package se.sundsvall.casestatus.integration.casedata;

import generated.se.sundsvall.casedata.Errand;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.casedata.configuration.CaseDataProperties;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.TestDataFactory.createCaseDataErrand;
import static se.sundsvall.casestatus.integration.casedata.CaseDataIntegration.ERRAND_NUMBER_FILTER;
import static se.sundsvall.casestatus.integration.casedata.CaseDataIntegration.PROPERTY_DESIGNATION_FILTER;

@ExtendWith(MockitoExtension.class)
class CaseDataIntegrationTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String NAMESPACE = "VIVA_LA_NAMESPACE";

	@Mock
	private CaseDataClient clientMock;

	@Mock
	private CaseDataMapper caseDataMapperMock;

	@Captor
	private ArgumentCaptor<String> filterCaptor;

	@InjectMocks
	private CaseDataIntegration caseDataIntegration;

	@Test
	void getCaseDataCaseByPropertyDesignation() {
		var propertyDesignation = "Körsbärsdalen 123";
		var caseDataErrand = createCaseDataErrand();
		var errandPage = new PageImpl<>(List.of(caseDataErrand));
		var mappedResponse = CaseStatusResponse.builder().withCaseId("1").build();
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(errandPage);
		when(caseDataMapperMock.toCaseStatusResponses(List.of(caseDataErrand))).thenReturn(List.of(mappedResponse));

		var result = caseDataIntegration.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE, propertyDesignation);

		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), filterCaptor.capture(), any(PageRequest.class));

		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation));
		assertThat(result).containsExactly(mappedResponse);

		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByPropertyDesignation_throws() {
		var propertyDesignation = "Körsbärsdalen 123";

		doThrow(Problem.valueOf(NOT_FOUND, "No errand was found")).when(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class));
		when(caseDataMapperMock.toCaseStatusResponses(List.of())).thenReturn(List.of());

		var result = caseDataIntegration.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE, propertyDesignation);

		assertThat(result).isEmpty();
		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), filterCaptor.capture(), any(PageRequest.class));
		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber() {
		var errandNumber = "Star Fighter 2000";
		var caseDataErrand = createCaseDataErrand();
		var errandPage = new PageImpl<>(List.of(caseDataErrand));
		var mappedResponse = CaseStatusResponse.builder().withCaseId("1").build();
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(errandPage);
		when(caseDataMapperMock.toCaseStatusResponses(List.of(caseDataErrand))).thenReturn(List.of(mappedResponse));

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE, errandNumber);

		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), filterCaptor.capture(), any(PageRequest.class));
		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(ERRAND_NUMBER_FILTER.formatted(errandNumber));
		assertThat(result).containsExactly(mappedResponse);
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber_throws() {
		var errandNumber = "Star Fighter 2000";

		doThrow(Problem.valueOf(NOT_FOUND, "No errand was found")).when(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class));
		when(caseDataMapperMock.toCaseStatusResponses(List.of())).thenReturn(List.of());

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE, errandNumber);

		assertThat(result).isEmpty();
		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), filterCaptor.capture(), any(PageRequest.class));
		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(ERRAND_NUMBER_FILTER.formatted(errandNumber));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByPropertyDesignation_readsAllPages() {
		var propertyDesignation = "Körsbärsdalen 123";
		var firstErrand = createCaseDataErrand();
		var secondErrand = createCaseDataErrand();
		var firstPage = new PageImpl<>(List.of(firstErrand), PageRequest.of(0, 1), 2);
		var secondPage = new PageImpl<>(List.of(secondErrand), PageRequest.of(1, 1), 2);
		var mappedResponse = CaseStatusResponse.builder().withCaseId("1").build();
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(firstPage, secondPage);
		when(caseDataMapperMock.toCaseStatusResponses(List.of(firstErrand, secondErrand))).thenReturn(List.of(mappedResponse));

		var result = caseDataIntegration.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE, propertyDesignation);

		assertThat(result).containsExactly(mappedResponse);
		verify(clientMock).getErrands(MUNICIPALITY_ID, NAMESPACE, PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation), PageRequest.of(0, 100));
		verify(clientMock).getErrands(MUNICIPALITY_ID, NAMESPACE, PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation), PageRequest.of(1, 100));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber_nullPage() {
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(null);
		when(caseDataMapperMock.toCaseStatusResponses(List.of())).thenReturn(List.of());

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE, "Star Fighter 2000");

		assertThat(result).isEmpty();
		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), eq(PageRequest.of(0, 100)));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getNamespacesWhenUnconfigured() {
		assertThat(new CaseDataIntegration(clientMock, new CaseDataProperties(5, 30, null), caseDataMapperMock).getNamespaces()).isEmpty();
	}

	@Test
	void getCaseDataCaseByErrandNumber_stopsOnEmptyPage() {
		// A page claiming more pages but holding nothing - continuing would never add anything
		var emptyPage = new PageImpl<>(List.<Errand>of(), PageRequest.of(0, 1), 10);
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(emptyPage);
		when(caseDataMapperMock.toCaseStatusResponses(List.of())).thenReturn(List.of());

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE, "Star Fighter 2000");

		assertThat(result).isEmpty();
		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), eq(PageRequest.of(0, 100)));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByPropertyDesignation_keepsPagesFetchedBeforeAFailure() {
		// A late page failing must not void the pages already read - the caller would see it as "no errands exist"
		var propertyDesignation = "Körsbärsdalen 123";
		var firstErrand = createCaseDataErrand();
		var firstPage = new PageImpl<>(List.of(firstErrand), PageRequest.of(0, 1), 2);
		var mappedResponse = CaseStatusResponse.builder().withCaseId("1").build();
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), eq(PageRequest.of(0, 100)))).thenReturn(firstPage);
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), eq(PageRequest.of(1, 100))))
			.thenThrow(Problem.valueOf(NOT_FOUND, "No errand was found"));
		when(caseDataMapperMock.toCaseStatusResponses(List.of(firstErrand))).thenReturn(List.of(mappedResponse));

		var result = caseDataIntegration.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE, propertyDesignation);

		assertThat(result).containsExactly(mappedResponse);
		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), eq(PageRequest.of(0, 100)));
		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), eq(PageRequest.of(1, 100)));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByPropertyDesignation_mapperThrows() {
		// Page failures are handled per page now, so the outer catch only guards the mapping step
		var propertyDesignation = "Körsbärsdalen 123";
		var caseDataErrand = createCaseDataErrand();
		var errandPage = new PageImpl<>(List.of(caseDataErrand));
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(errandPage);
		when(caseDataMapperMock.toCaseStatusResponses(List.of(caseDataErrand))).thenThrow(new IllegalStateException("Mapping blew up"));

		var result = caseDataIntegration.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE, propertyDesignation);

		assertThat(result).isEmpty();
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber_mapperThrows() {
		var errandNumber = "Star Fighter 2000";
		var caseDataErrand = createCaseDataErrand();
		var errandPage = new PageImpl<>(List.of(caseDataErrand));
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(errandPage);
		when(caseDataMapperMock.toCaseStatusResponses(List.of(caseDataErrand))).thenThrow(new IllegalStateException("Mapping blew up"));

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE, errandNumber);

		assertThat(result).isEmpty();
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber_escapesQuoteInFilterValue() {
		var errandNumber = "O'Brien";
		var errandPage = new PageImpl<>(List.<Errand>of(), PageRequest.of(0, 1), 0);
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(errandPage);
		when(caseDataMapperMock.toCaseStatusResponses(List.of())).thenReturn(List.of());

		caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE, errandNumber);

		verify(clientMock).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), filterCaptor.capture(), any(PageRequest.class));
		assertThat(filterCaptor.getValue()).isEqualTo("errandNumber:'O\\'Brien'");
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber_stopsAtPageLimit() {
		// Paging metadata comes from the remote service - a page always reporting a successor must not loop forever
		var runawayPage = new PageImpl<>(List.of(createCaseDataErrand()), PageRequest.of(0, 1), 1000);
		when(clientMock.getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class))).thenReturn(runawayPage);
		when(caseDataMapperMock.toCaseStatusResponses(anyList())).thenReturn(List.of());

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE, "Star Fighter 2000");

		assertThat(result).isEmpty();
		verify(clientMock, times(100)).getErrands(eq(MUNICIPALITY_ID), eq(NAMESPACE), any(String.class), any(PageRequest.class));
		verifyNoMoreInteractions(clientMock);
	}
}
