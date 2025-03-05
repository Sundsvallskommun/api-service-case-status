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
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getId()).isEqualTo("errandId");
	}

	@Test
	void getSupportManagementCase() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var errandId = "errandId";
		final var namespace = "namespace";
		final var errand = new Errand().id("errandId");
		when(supportManagementClient.readAllNamespaceConfigs()).thenReturn(List.of(new NamespaceConfig().namespace(namespace), new NamespaceConfig().namespace("otherNamespace")));
		when(supportManagementClient.findErrand(municipalityId, namespace, errandId)).thenReturn(java.util.Optional.of(errand));
		// Act
		final var result = supportManagementService.getSupportManagementCase(municipalityId, errandId);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("errandId");
		verify(supportManagementClient).readAllNamespaceConfigs();
		verify(supportManagementClient).findErrand(eq(municipalityId), any(), eq(errandId));
		verifyNoMoreInteractions(supportManagementClient);

	}

}
