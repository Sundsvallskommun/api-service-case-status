package se.sundsvall.casestatus.integration.casedata;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;

@Component
public class CaseDataIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaseDataIntegration.class);
	private final CaseDataClient client;

	static final String PROPERTY_DESIGNATION_FILTER = "facilities.address.propertyDesignation:'%s'";
	static final String ERRAND_NUMBER_FILTER = "errandNumber:'%s'";

	public CaseDataIntegration(final CaseDataClient client) {
		this.client = client;
	}

	public List<CaseStatusResponse> getCaseDataCaseByPropertyDesignation(final String municipalityId, final String propertyDesignation) {
		try {
			LOGGER.info("Fetching errand(s) for municipalityId: {} and propertyDesignation: {}", municipalityId, propertyDesignation);
			var filter = PROPERTY_DESIGNATION_FILTER.formatted(propertyDesignation);
			var errandPage = client.getErrandsWithoutNamespace(municipalityId, filter, PageRequest.of(0, 100));
			var errands = errandPage.getContent();

			LOGGER.info("Successfully fetched {} errand(s) for municipalityId: {} and propertyDesignation: {}", errands.size(), municipalityId, propertyDesignation);
			return CaseDataMapper.toCaseStatusResponses(errands);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			LOGGER.info("Error while fetching errands for municipalityId: {} and propertyDesignation: {}, returning empty list.", municipalityId, propertyDesignation);
			return List.of();
		}
	}

	public List<CaseStatusResponse> getCaseDataCaseByErrandNumber(final String municipalityId, final String errandNumber) {
		try {
			LOGGER.info("Fetching errand(s) for municipalityId: {} and errandNumber: {}", municipalityId, errandNumber);
			var filter = ERRAND_NUMBER_FILTER.formatted(errandNumber);
			var errandPage = client.getErrandsWithoutNamespace(municipalityId, filter, PageRequest.of(0, 100));
			var errands = errandPage.getContent();

			LOGGER.info("Successfully fetched {} errand(s) for municipalityId: {} and errandNumber: {}", errands.size(), municipalityId, errandNumber);
			return CaseDataMapper.toCaseStatusResponses(errands);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			LOGGER.info("Error while fetching errands for municipalityId: {} and errandNumber: {}, returning empty list.", municipalityId, errandNumber);
			return List.of();
		}
	}
}
