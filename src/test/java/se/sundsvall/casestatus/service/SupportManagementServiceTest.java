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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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
		final var namespace = "namespace";
		final var errand = new Errand().id("errandId");
		when(supportManagementClient.findErrandById(any(), eq(namespace), any())).thenReturn(ResponseEntity.ok(errand));

		// Act
		final var result = supportManagementService.getSupportManagementCaseById(municipalityId, namespace, errand.getId());

		// Verify
		assertThat(result).isSameAs(errand);
		verify(supportManagementClient).findErrandById(municipalityId, namespace, errand.getId());
		verifyNoMoreInteractions(supportManagementClient);

	}
}
