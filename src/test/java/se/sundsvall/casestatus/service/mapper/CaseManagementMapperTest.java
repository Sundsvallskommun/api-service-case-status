package se.sundsvall.casestatus.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createCaseStatusDTO;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;

@ExtendWith(MockitoExtension.class)
class CaseManagementMapperTest {

	@Mock
	private CaseManagementOpeneViewRepository caseManagementOpeneViewRepositoryMock;

	@Mock
	private CaseTypeRepository caseTypeRepositoryMock;

	@InjectMocks
	private CaseManagementMapper caseManagementMapper;

	@ParameterizedTest
	@EnumSource(CaseStatusDTO.SystemEnum.class)
	void toCaseStatusResponse(final CaseStatusDTO.SystemEnum system) {
		final var caseStatus = createCaseStatusDTO(system);
		final var municipalityId = "2281";
		final var spy = Mockito.spy(caseManagementMapper);
		when(spy.getStatus(caseStatus.getStatus())).thenReturn("status");
		when(spy.getTimestamp(caseStatus.getTimestamp())).thenReturn("2025-04-01 12:30");
		when(spy.getServiceName(caseStatus.getServiceName(), caseStatus.getCaseType(), municipalityId)).thenReturn("serviceName");

		final var result = spy.toCaseStatusResponse(caseStatus, municipalityId);

		assertThat(result).isNotNull().satisfies(response -> {
			assertThat(response.getSystem()).isEqualTo(system.getValue());
			assertThat(response.getCaseId()).isEqualTo(caseStatus.getCaseId());
			assertThat(response.getExternalCaseId()).isEqualTo(caseStatus.getExternalCaseId());
			assertThat(response.getCaseType()).isEqualTo("serviceName");
			assertThat(response.getStatus()).isEqualTo("status");
			assertThat(response.getLastStatusChange()).isEqualTo("2025-04-01 12:30");
			assertThat(response.getFirstSubmitted()).isEqualTo("2025-04-01 12:30");
			assertThat(response.getErrandNumber()).isEqualTo("errandNumber");
			assertThat(response.getNamespace()).isEqualTo("namespace");
		});
	}

	/**
	 * Test scenario where there is not corresponding OpenE ID for the original status.
	 */
	@Test
	void getStatus_1() {
		final var originalStatus = "originalStatus";

		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(originalStatus)).thenReturn(Optional.empty());

		final var result = caseManagementMapper.getStatus(originalStatus);

		assertThat(result).isEqualTo(originalStatus);
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(originalStatus);

		verifyNoMoreInteractions(caseManagementOpeneViewRepositoryMock);
		verifyNoInteractions(caseTypeRepositoryMock);
	}

	/**
	 * Test scenario where there is a corresponding OpenE ID for the original status.
	 */
	@Test
	void getStatus_2() {
		final var originalStatus = "originalStatus";
		final var view = new CaseManagementOpeneView();
		view.setOpenEId("openEId");

		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(originalStatus)).thenReturn(Optional.of(view));

		final var result = caseManagementMapper.getStatus(originalStatus);

		assertThat(result).isEqualTo("openEId");
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(originalStatus);

		verifyNoMoreInteractions(caseManagementOpeneViewRepositoryMock);
		verifyNoInteractions(caseTypeRepositoryMock);
	}

	/**
	 * Test scenario where a timestamp is provided.
	 */
	@Test
	void getTimestamp_1() {
		final var timestamp = LocalDateTime.of(2025, Month.APRIL, 1, 12, 30, 45, 555);

		final var result = caseManagementMapper.getTimestamp(timestamp);

		assertThat(result).isEqualTo("2025-04-01 12:30");
	}

	/**
	 * Test scenario where a timestamp is not provided.
	 */
	@Test
	void getTimestamp_2() {
		final var result = caseManagementMapper.getTimestamp(null);

		assertThat(result).isNull();
	}

	/**
	 * Test scenario where the service name is provided.
	 */
	@Test
	void getServiceName_1() {
		final var serviceName = "serviceName";
		final var caseType = "caseType";
		final var municipalityId = "2281";

		when(caseTypeRepositoryMock.findByEnumValueAndMunicipalityId(caseType, municipalityId)).thenReturn(Optional.empty());

		final var result = caseManagementMapper.getServiceName(serviceName, caseType, municipalityId);

		assertThat(result).isEqualTo("serviceName");
		verify(caseTypeRepositoryMock).findByEnumValueAndMunicipalityId(caseType, municipalityId);
		verifyNoMoreInteractions(caseTypeRepositoryMock);
		verifyNoInteractions(caseManagementOpeneViewRepositoryMock);
	}

	/**
	 * Test scenario where the service name is not provided and a description is found in the database.
	 */
	@Test
	void getServiceName_2() {
		final var caseType = "caseType";
		final var municipalityId = "2281";
		final var caseTypeEntity = new CaseTypeEntity();
		caseTypeEntity.setDescription("description");

		when(caseTypeRepositoryMock.findByEnumValueAndMunicipalityId(caseType, municipalityId)).thenReturn(Optional.of(caseTypeEntity));

		final var result = caseManagementMapper.getServiceName(null, caseType, municipalityId);

		assertThat(result).isEqualTo("description");
		verify(caseTypeRepositoryMock).findByEnumValueAndMunicipalityId(caseType, municipalityId);
		verifyNoMoreInteractions(caseTypeRepositoryMock);
		verifyNoInteractions(caseManagementOpeneViewRepositoryMock);
	}

	/**
	 * Test scenario where the service name is not provided and a description is not found in the database.
	 */
	@Test
	void getServiceName_3() {
		final var caseType = "caseType";
		final var municipalityId = "2281";

		when(caseTypeRepositoryMock.findByEnumValueAndMunicipalityId(caseType, municipalityId)).thenReturn(Optional.empty());

		final var result = caseManagementMapper.getServiceName(null, caseType, municipalityId);

		assertThat(result).isEqualTo(caseType);
		verify(caseTypeRepositoryMock).findByEnumValueAndMunicipalityId(caseType, municipalityId);
		verifyNoMoreInteractions(caseTypeRepositoryMock);
		verifyNoInteractions(caseManagementOpeneViewRepositoryMock);
	}

}
