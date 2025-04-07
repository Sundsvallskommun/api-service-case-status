package se.sundsvall.casestatus.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.sundsvall.casestatus.integration.supportmanagement.SupportManagementClient;

@ExtendWith(MockitoExtension.class)
class SupportManagementServiceTest {

	@Mock
	private SupportManagementClient supportManagementClient;

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
		when(supportManagementClient.readAllNamespaceConfigs()).thenReturn(List.of(new NamespaceConfig().namespace(namespace)));
		when(supportManagementClient.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(errandsPage);

		// Act
		final var result = supportManagementService.getSupportManagementCases(municipalityId, filter);

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
		when(supportManagementClient.readAllNamespaceConfigs()).thenReturn(List.of(new NamespaceConfig().namespace(namespace)));
		when(supportManagementClient.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(errandsPage);

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
		final var namespace1 = "namespace-1";
		final var namespace2 = "namespace-2";
		final var errand = new Errand().id("errandId");
		final var errandsPage = new PageImpl<>(List.of(errand, errand));
		when(supportManagementClient.findErrands(any(), eq(namespace1), any(), any())).thenReturn(new PageImpl<>(emptyList()));
		when(supportManagementClient.findErrands(any(), eq(namespace2), any(), any())).thenReturn(errandsPage);

		// Act
		final var result = supportManagementService.getSupportManagementCaseById(municipalityId, List.of(namespace1, namespace2), errand.getId());

		// Verify
		assertThat(result).isSameAs(errand);
		verify(supportManagementClient).findErrands(municipalityId, namespace1, "id:'" + errand.getId() + "'", PageRequest.of(0, 20));
		verify(supportManagementClient).findErrands(municipalityId, namespace2, "id:'" + errand.getId() + "'", PageRequest.of(0, 20));
		verifyNoMoreInteractions(supportManagementClient);

	}
}
