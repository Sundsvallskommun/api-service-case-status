package se.sundsvall.casestatus.integration.casedata;

import static java.util.Collections.emptyList;
import static se.sundsvall.casestatus.integration.casedata.CaseDataMapper.toCaseStatusResponses;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.casedata.configuration.CaseDataProperties;

@Component
public class CaseDataIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaseDataIntegration.class);
	private final CaseDataClient client;
	private final CaseDataProperties properties;

	static final String PROPERTY_DESIGNATION_FILTER = "facilities.address.propertyDesignation~'%s'";
	static final String ERRAND_NUMBER_FILTER = "errandNumber:'%s'";

	public CaseDataIntegration(final CaseDataClient client, CaseDataProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	public List<String> getNamespaces() {
		return properties.namespaces();
	}

	public List<CaseStatusResponse> getCaseDataCaseByPropertyDesignation(final String municipalityId, final String namespace, final String propertyDesignation) {

		final var logMunicipalityId = sanitizeForLogging(municipalityId);
		final var logNamespace = sanitizeForLogging(namespace);
		final var logPropertyDesignation = sanitizeForLogging(propertyDesignation);

		try {
			LOGGER.info("Fetching errand(s) for municipalityId: {} and propertyDesignation: {}", logMunicipalityId, logPropertyDesignation);
			var filter = PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation);
			var errandPage = client.getErrands(municipalityId, namespace, filter, PageRequest.of(0, 100));
			var errands = errandPage.getContent();

			LOGGER.info("Successfully fetched {} errand(s) for municipalityId: {} and namespace: {} and propertyDesignation: {}", errands.size(), logMunicipalityId, logNamespace, logPropertyDesignation);
			return toCaseStatusResponses(errands);
		} catch (Exception e) {
			LOGGER.error("Error while fetching errands for municipalityId: {} and namespace: {} and propertyDesignation: {}, returning empty list.", logMunicipalityId, logNamespace, logPropertyDesignation, e);
			return emptyList();
		}
	}

	public List<CaseStatusResponse> getCaseDataCaseByErrandNumber(final String municipalityId, final String namespace, final String errandNumber) {

		final var logMunicipalityId = sanitizeForLogging(municipalityId);
		final var logNamespace = sanitizeForLogging(namespace);
		final var logErrandNumber = sanitizeForLogging(errandNumber);

		try {
			LOGGER.info("Fetching errand(s) for municipalityId: {} and errandNumber: {}", municipalityId, errandNumber);
			var filter = ERRAND_NUMBER_FILTER.formatted(errandNumber);
			var errandPage = client.getErrands(municipalityId, namespace, filter, PageRequest.of(0, 100));
			var errands = errandPage.getContent();

			LOGGER.info("Successfully fetched {} errand(s) for municipalityId: {} and namespace: {} and errandNumber: {}", errands.size(), logMunicipalityId, logNamespace, logErrandNumber);
			return toCaseStatusResponses(errands);
		} catch (Exception e) {
			LOGGER.error("Error while fetching errands for municipalityId: {} and namespace: {} and errandNumber: {}, returning empty list.", logMunicipalityId, logNamespace, logErrandNumber, e);
			return emptyList();
		}
	}
}
