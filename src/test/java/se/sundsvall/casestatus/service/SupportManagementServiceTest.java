package se.sundsvall.casestatus.service;

import generated.se.sundsvall.supportmanagement.Category;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.Label;
import generated.se.sundsvall.supportmanagement.Labels;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import generated.se.sundsvall.supportmanagement.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.sundsvall.casestatus.integration.supportmanagement.SupportManagementIntegration;
import se.sundsvall.casestatus.util.RoleSearchProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
	void getSupportManagementCasesReadsAllPages() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var filter = "filter";
		final var namespace = "namespace";
		final var firstPage = new PageImpl<>(List.of(new Errand().id("errandId1")), PageRequest.of(0, 1), 2);
		final var secondPage = new PageImpl<>(List.of(new Errand().id("errandId2")), PageRequest.of(1, 1), 2);
		when(supportManagementIntegrationMock.readAllNamespaceConfigs(any())).thenReturn(List.of(new NamespaceConfig().namespace(namespace).municipalityId(municipalityId)));
		when(supportManagementIntegrationMock.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class)))
			.thenReturn(firstPage, secondPage);

		// Act
		final var result = supportManagementService.getSupportManagementCases(municipalityId, filter);

		// Assert
		assertThat(result.get(namespace)).extracting(Errand::getId).containsExactly("errandId1", "errandId2");
		verify(supportManagementIntegrationMock).findErrands(municipalityId, namespace, filter, PageRequest.of(0, 100));
		verify(supportManagementIntegrationMock).findErrands(municipalityId, namespace, filter, PageRequest.of(1, 100));
	}

	@Test
	void getSupportManagementCasesStopsAtPageLimit() {
		// Arrange - paging metadata comes from the remote service, a page always reporting a successor must not loop forever
		final var municipalityId = "municipalityId";
		final var filter = "filter";
		final var namespace = "namespace";
		final var runawayPage = new PageImpl<>(List.of(new Errand().id("errandId")), PageRequest.of(0, 1), 1000);
		when(supportManagementIntegrationMock.readAllNamespaceConfigs(any())).thenReturn(List.of(new NamespaceConfig().namespace(namespace).municipalityId(municipalityId)));
		when(supportManagementIntegrationMock.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(runawayPage);

		// Act
		final var result = supportManagementService.getSupportManagementCases(municipalityId, filter);

		// Assert
		assertThat(result.get(namespace)).hasSize(100);
		verify(supportManagementIntegrationMock, times(100)).findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class));
	}

	@Test
	void getSupportManagementCasesStopsOnEmptyPage() {
		// Arrange - a page claiming more pages but holding nothing, continuing would never add anything
		final var municipalityId = "municipalityId";
		final var filter = "filter";
		final var namespace = "namespace";
		final var emptyPage = new PageImpl<>(List.<Errand>of(), PageRequest.of(0, 1), 10);
		when(supportManagementIntegrationMock.readAllNamespaceConfigs(any())).thenReturn(List.of(new NamespaceConfig().namespace(namespace).municipalityId(municipalityId)));
		when(supportManagementIntegrationMock.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(emptyPage);

		// Act
		final var result = supportManagementService.getSupportManagementCases(municipalityId, filter);

		// Assert
		assertThat(result.get(namespace)).isEmpty();
		verify(supportManagementIntegrationMock).findErrands(municipalityId, namespace, filter, PageRequest.of(0, 100));
	}

	@Test
	void getSupportManagementCasesByExternalIdFallsBackToPrimaryRoleWhenUnconfigured() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandsPage = new PageImpl<>(List.of(new Errand().id("errandId")));
		when(supportManagementIntegrationMock.readAllNamespaceConfigs(any())).thenReturn(List.of(new NamespaceConfig().namespace(namespace).municipalityId(municipalityId)));
		when(supportManagementIntegrationMock.findErrands(eq(municipalityId), eq(namespace), any(String.class), any(PageRequest.class))).thenReturn(errandsPage);
		when(roleSearchProperties.getRoles()).thenReturn(null);

		// Act
		final var result = supportManagementService.getSupportManagementCasesByExternalId(municipalityId, "externalId");

		// Assert
		assertThat(result.get(namespace)).hasSize(1);
		verify(supportManagementIntegrationMock).findErrands(municipalityId, namespace,
			"stakeholders.externalId:'externalId' and stakeholders.role:'PRIMARY'", PageRequest.of(0, 100));
	}

	@Test
	void getClassificationDisplayNameWhenErrandHasNoClassification() {
		assertThat(supportManagementService.getClassificationDisplayName("municipalityId", "namespace", new Errand())).isNull();
		verifyNoMoreInteractions(supportManagementIntegrationMock);
	}

	@Test
	void getClassificationDisplayNameFromCategoryMetadata() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errand = new Errand().classification(new Classification().category("SUPPORT-CASE").type("OPEN-HOURS"));
		when(supportManagementIntegrationMock.findCategoriesForNamespace(municipalityId, namespace))
			.thenReturn(List.of(new Category().name("SUPPORT-CASE").types(Set.of(new Type().name("OPEN-HOURS").displayName("Questions on opening hours")))));

		// Act & Assert
		assertThat(supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand)).isEqualTo("Questions on opening hours");
		verify(supportManagementIntegrationMock).findCategoriesForNamespace(municipalityId, namespace);
		verifyNoMoreInteractions(supportManagementIntegrationMock);
	}

	@Test
	void getClassificationDisplayNameFromDeepestErrandLabel() {
		// Arrange - a label based namespace, where the classification holds the technical resource path
		final var municipalityId = "municipalityId";
		final var namespace = "BOU";
		final var errand = new Errand()
			.classification(new Classification().category("BOU").type("BOU/FEE_CONTROL_CHILDCARE"))
			.labels(List.of(
				new ErrandLabel().resourcePath("BOU").displayName("Barn och utbildning"),
				new ErrandLabel().resourcePath("BOU/FEE_CONTROL_CHILDCARE").displayName("Avgiftskontroll barnomsorg")));
		when(supportManagementIntegrationMock.findCategoriesForNamespace(municipalityId, namespace)).thenReturn(List.of());

		// Act & Assert
		assertThat(supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand)).isEqualTo("Avgiftskontroll barnomsorg");
		verify(supportManagementIntegrationMock).findCategoriesForNamespace(municipalityId, namespace);
		verifyNoMoreInteractions(supportManagementIntegrationMock);
	}

	@Test
	void getClassificationDisplayNameFromErrandLabelWhenClassificationIsMissing() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "BOU";
		final var errand = new Errand().labels(List.of(new ErrandLabel().resourcePath("BOU/FEE_CONTROL_CHILDCARE").displayName("Avgiftskontroll barnomsorg")));

		// Act & Assert
		assertThat(supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand)).isEqualTo("Avgiftskontroll barnomsorg");
		verifyNoMoreInteractions(supportManagementIntegrationMock);
	}

	@Test
	void getClassificationDisplayNameFromNamespaceLabelStructure() {
		// Arrange - errand without labels, leaving the label structure as the only place to translate the resource path
		final var municipalityId = "municipalityId";
		final var namespace = "BOU";
		final var errand = new Errand().classification(new Classification().category("BOU").type("BOU/FEE_CONTROL_CHILDCARE"));
		when(supportManagementIntegrationMock.findCategoriesForNamespace(municipalityId, namespace)).thenReturn(List.of());
		when(supportManagementIntegrationMock.findLabelsForNamespace(municipalityId, namespace)).thenReturn(new Labels().labelStructure(List.of(
			new Label().resourcePath("BOU").displayName("Barn och utbildning").labels(List.of(
				new Label().resourcePath("BOU/FEE_CONTROL_CHILDCARE").displayName("Avgiftskontroll barnomsorg"))))));

		// Act & Assert
		assertThat(supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand)).isEqualTo("Avgiftskontroll barnomsorg");
		verify(supportManagementIntegrationMock).findCategoriesForNamespace(municipalityId, namespace);
		verify(supportManagementIntegrationMock).findLabelsForNamespace(municipalityId, namespace);
		verifyNoMoreInteractions(supportManagementIntegrationMock);
	}

	@Test
	void getClassificationDisplayNameFallsBackToClassificationTypeWhenNothingMatches() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errand = new Errand().classification(new Classification().category("UNKNOWN").type("UNKNOWN_TYPE"));
		when(supportManagementIntegrationMock.findCategoriesForNamespace(municipalityId, namespace)).thenReturn(null);
		when(supportManagementIntegrationMock.findLabelsForNamespace(municipalityId, namespace)).thenReturn(null);

		// Act & Assert
		assertThat(supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand)).isEqualTo("UNKNOWN_TYPE");
	}

	@Test
	void getClassificationDisplayNameFromNamespaceLabelStructureWithLeadingSeparator() {
		// Arrange - the label structure spells the resource path with a leading separator, the classification without one
		final var municipalityId = "municipalityId";
		final var namespace = "BOU";
		final var errand = new Errand().classification(new Classification().category("BOU").type("BOU/FEE_CONTROL_CHILDCARE"));
		when(supportManagementIntegrationMock.findCategoriesForNamespace(municipalityId, namespace)).thenReturn(List.of());
		when(supportManagementIntegrationMock.findLabelsForNamespace(municipalityId, namespace)).thenReturn(new Labels().labelStructure(List.of(
			new Label().resourcePath("/BOU").displayName("Barn och utbildning").labels(List.of(
				new Label().resourcePath("/BOU/FEE_CONTROL_CHILDCARE").displayName("Avgiftskontroll barnomsorg"))))));

		// Act & Assert
		assertThat(supportManagementService.getClassificationDisplayName(municipalityId, namespace, errand)).isEqualTo("Avgiftskontroll barnomsorg");
		verify(supportManagementIntegrationMock).findCategoriesForNamespace(municipalityId, namespace);
		verify(supportManagementIntegrationMock).findLabelsForNamespace(municipalityId, namespace);
		verifyNoMoreInteractions(supportManagementIntegrationMock);
	}
}
