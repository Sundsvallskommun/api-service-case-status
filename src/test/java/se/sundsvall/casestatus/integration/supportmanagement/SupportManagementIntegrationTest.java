package se.sundsvall.casestatus.integration.supportmanagement;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.supportmanagement.Category;
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

@ExtendWith(MockitoExtension.class)
class SupportManagementIntegrationTest {

	@Mock
	private SupportManagementClient supportManagementClientMock;

	@InjectMocks
	private SupportManagementIntegration supportManagementIntegration;

	@Test
	void readAllNamespaceConfigs() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var mockedResult = List.of(new NamespaceConfig());
		when(supportManagementClientMock.readAllNamespaceConfigs(any())).thenReturn(mockedResult);

		// Act
		final var result = supportManagementIntegration.readAllNamespaceConfigs(municipalityId);

		// Assert
		assertThat(result)
			.hasSize(1)
			.isEqualTo(mockedResult);
		verify(supportManagementClientMock).readAllNamespaceConfigs(municipalityId);
		verifyNoMoreInteractions(supportManagementClientMock);
	}

	@Test
	void findErrands() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var filter = "filter";
		final var pageRequest = PageRequest.ofSize(1);
		final var mockedResult = new PageImpl<Errand>(emptyList());
		when(supportManagementClientMock.findErrands(any(), any(), any(), any())).thenReturn(mockedResult);

		// Act
		final var result = supportManagementIntegration.findErrands(municipalityId, namespace, filter, pageRequest);

		// Assert
		assertThat(result).isEqualTo(mockedResult);
		verify(supportManagementClientMock).findErrands(municipalityId, namespace, filter, pageRequest);
		verifyNoMoreInteractions(supportManagementClientMock);
	}

	@Test
	void findErrandById() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var id = "id";
		final var mockedResult = ResponseEntity.ofNullable(new Errand());
		when(supportManagementClientMock.findErrandById(any(), any(), any())).thenReturn(mockedResult);

		// Act
		final var result = supportManagementIntegration.findErrandById(municipalityId, namespace, id);

		// Assert
		assertThat(result).isEqualTo(mockedResult);
		verify(supportManagementClientMock).findErrandById(municipalityId, namespace, id);
		verifyNoMoreInteractions(supportManagementClientMock);
	}

	@Test
	void findCategoriesForNamespace() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var mockedResult = List.of(new Category());
		when(supportManagementClientMock.findCategoriesForNamespace(any(), any())).thenReturn(mockedResult);

		// Act
		final var result = supportManagementIntegration.findCategoriesForNamespace(municipalityId, namespace);

		// Assert
		assertThat(result).isEqualTo(mockedResult);
		verify(supportManagementClientMock).findCategoriesForNamespace(municipalityId, namespace);
		verifyNoMoreInteractions(supportManagementClientMock);
	}
}
