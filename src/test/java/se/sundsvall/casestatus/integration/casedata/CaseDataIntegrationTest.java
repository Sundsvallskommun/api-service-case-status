package se.sundsvall.casestatus.integration.casedata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.TestDataFactory.createCaseDataErrand;
import static se.sundsvall.casestatus.integration.casedata.CaseDataIntegration.ERRAND_NUMBER_FILTER;
import static se.sundsvall.casestatus.integration.casedata.CaseDataIntegration.PROPERTY_DESIGNATION_FILTER;

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
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class CaseDataIntegrationTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private CaseDataClient clientMock;

	@Captor
	private ArgumentCaptor<String> filterCaptor;

	@InjectMocks
	private CaseDataIntegration caseDataIntegration;

	@Test
	void getCaseDataCaseByPropertyDesignation() {
		var propertyDesignation = "Körsbärsdalen 123";
		var caseDataErrand = createCaseDataErrand();
		var errandPage = new PageImpl<>(List.of(caseDataErrand));
		when(clientMock.getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), any(String.class), any(PageRequest.class))).thenReturn(errandPage);

		var result = caseDataIntegration.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, propertyDesignation);

		verify(clientMock).getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), filterCaptor.capture(), any(PageRequest.class));

		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation));
		assertThat(result).hasSize(1).allSatisfy(response -> {
			assertThat(response).usingRecursiveAssertion().isEqualTo(CaseDataMapper.toCaseStatusResponse(caseDataErrand));
		});

		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByPropertyDesignation_throws() {
		var propertyDesignation = "Körsbärsdalen 123";

		doThrow(Problem.valueOf(NOT_FOUND, "No errand was found")).when(clientMock).getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), any(String.class), any(PageRequest.class));

		var result = caseDataIntegration.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, propertyDesignation);

		assertThat(result).isEmpty();
		verify(clientMock).getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), filterCaptor.capture(), any(PageRequest.class));
		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation));
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber() {
		var errandNumber = "Star Fighter 2000";
		var caseDataErrand = createCaseDataErrand();
		var errandPage = new PageImpl<>(List.of(caseDataErrand));
		when(clientMock.getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), any(String.class), any(PageRequest.class))).thenReturn(errandPage);

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, errandNumber);

		verify(clientMock).getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), filterCaptor.capture(), any(PageRequest.class));
		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(ERRAND_NUMBER_FILTER.formatted(errandNumber));
		assertThat(result).hasSize(1).allSatisfy(response -> {
			assertThat(response).usingRecursiveAssertion().isEqualTo(CaseDataMapper.toCaseStatusResponse(caseDataErrand));
		});
		verifyNoMoreInteractions(clientMock);
	}

	@Test
	void getCaseDataCaseByErrandNumber_throws() {
		var errandNumber = "Star Fighter 2000";

		doThrow(Problem.valueOf(NOT_FOUND, "No errand was found")).when(clientMock).getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), any(String.class), any(PageRequest.class));

		var result = caseDataIntegration.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, errandNumber);

		assertThat(result).isEmpty();
		verify(clientMock).getErrandsWithoutNamespace(eq(MUNICIPALITY_ID), filterCaptor.capture(), any(PageRequest.class));
		var filter = filterCaptor.getValue();
		assertThat(filter).isEqualTo(ERRAND_NUMBER_FILTER.formatted(errandNumber));
		verifyNoMoreInteractions(clientMock);
	}
}
