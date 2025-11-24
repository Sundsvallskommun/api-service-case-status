package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import se.sundsvall.casestatus.integration.supportmanagement.SupportManagementIntegration;
import se.sundsvall.casestatus.util.RoleSearchProperties;

@ExtendWith(MockitoExtension.class)
class SupportManagementServiceTest {

	@Mock
	private SupportManagementIntegration supportManagementIntegrationMock;

	@Mock
	private RoleSearchProperties roleSearchProperties;

	@InjectMocks
	private SupportManagementService supportManagementService;

	@Test
	void getSupportManagementCases() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var filter = "filter";
		final var namespace = "namespace";
		final var errand = new Errand().id("errandId");
		final var errandsPage = new PageImpl<>(List.of(errand));
		when(supportManagementIntegrationMock.readAllNamespaceConfigs(any())).thenReturn(List.of(new NamespaceConfig().namespace(namespace).municipalityId(municipalityId)));
		when(supportManagementIntegrationMock.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(errandsPage);

		// Act
		final var result = supportManagementService.getSupportManagementCases(municipalityId, filter);

		// Assert
		assertThat(result.get(namespace)).isNotNull().hasSize(1);
		assertThat(result.get(namespace).getFirst().getId()).isEqualTo("errandId");
	}

	@Test
	void getSupportManagementCasesByPartyId() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var role = "role";
		final var errand = new Errand().id("errandId");
		final var errandsPage = new PageImpl<>(List.of(errand));
		when(supportManagementIntegrationMock.readAllNamespaceConfigs(any())).thenReturn(List.of(new NamespaceConfig().namespace(namespace).municipalityId(municipalityId)));
		when(supportManagementIntegrationMock.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(errandsPage);
		when(roleSearchProperties.getRoles()).thenReturn(Map.of(municipalityId, Map.of(namespace, role)));

		// Act
		final var result = supportManagementService.getSupportManagementCasesByExternalId(municipalityId, namespace);

		// Assert
		assertThat(result.get(namespace)).isNotNull().hasSize(1);
		assertThat(result.get(namespace).getFirst().getId()).isEqualTo("errandId");
	}

	@Test
	void getSupportManagementCasesFilterDuplicates() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var filter = "filter";
		final var namespace = "namespace";
		final var errand = new Errand().id("errandId");
		final var errandsPage = new PageImpl<>(List.of(errand, errand));
		when(supportManagementIntegrationMock.readAllNamespaceConfigs(any())).thenReturn(List.of(new NamespaceConfig().namespace(namespace).municipalityId(municipalityId)));
		when(supportManagementIntegrationMock.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(errandsPage);

		// Act
		final var result = supportManagementService.getSupportManagementCases(municipalityId, filter);

		// Assert
		assertThat(result.get(namespace)).isNotNull().hasSize(2);
		assertThat(result.get(namespace).getFirst().getId()).isEqualTo("errandId");
	}

	@Test
	void getSupportManagementCaseById() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errand = new Errand().id("errandId");
		when(supportManagementIntegrationMock.findErrandById(any(), eq(namespace), any())).thenReturn(ResponseEntity.ok(errand));

		// Act
		final var result = supportManagementService.getSupportManagementCaseById(municipalityId, namespace, errand.getId());

		// Verify
		assertThat(result).isSameAs(errand);
		verify(supportManagementIntegrationMock).findErrandById(municipalityId, namespace, errand.getId());
		verifyNoMoreInteractions(supportManagementIntegrationMock);
	}
}
