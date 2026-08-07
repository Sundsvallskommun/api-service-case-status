package se.sundsvall.casestatus.integration.casedata;

import generated.se.sundsvall.casedata.Errand;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.casedata.configuration.CaseDataProperties;
import se.sundsvall.casestatus.util.PaginationUtil;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casestatus.util.FilterUtil.escapeFilterValue;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Component
public class CaseDataIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaseDataIntegration.class);

	private final CaseDataClient client;
	private final CaseDataProperties properties;
	private final CaseDataMapper caseDataMapper;

	static final String PROPERTY_DESIGNATION_FILTER = "facilities.address.propertyDesignation~'%s'";
	static final String ERRAND_NUMBER_FILTER = "errandNumber:'%s'";

	public CaseDataIntegration(final CaseDataClient client, final CaseDataProperties properties, final CaseDataMapper caseDataMapper) {
		this.client = client;
		this.properties = properties;
		this.caseDataMapper = caseDataMapper;
	}

	public List<String> getNamespaces() {
		return ofNullable(properties.namespaces()).orElse(emptyList());
	}

	public List<CaseStatusResponse> getCaseDataCaseByPropertyDesignation(final String municipalityId, final String namespace, final String propertyDesignation) {

		final var logMunicipalityId = sanitizeForLogging(municipalityId);
		final var logNamespace = sanitizeForLogging(namespace);
		final var logPropertyDesignation = sanitizeForLogging(propertyDesignation);

		try {
			LOGGER.info("Fetching errand(s) for municipalityId: {} and propertyDesignation: {}", logMunicipalityId, logPropertyDesignation);
			final var errands = findAllErrands(municipalityId, namespace, PROPERTY_DESIGNATION_FILTER.formatted(escapeFilterValue(propertyDesignation)));

			LOGGER.info("Successfully fetched {} errand(s) for municipalityId: {} and namespace: {} and propertyDesignation: {}", errands.size(), logMunicipalityId, logNamespace, logPropertyDesignation);
			return caseDataMapper.toCaseStatusResponses(errands);
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
			LOGGER.info("Fetching errand(s) for municipalityId: {} and errandNumber: {}", logMunicipalityId, logErrandNumber);
			final var errands = findAllErrands(municipalityId, namespace, ERRAND_NUMBER_FILTER.formatted(escapeFilterValue(errandNumber)));

			LOGGER.info("Successfully fetched {} errand(s) for municipalityId: {} and namespace: {} and errandNumber: {}", errands.size(), logMunicipalityId, logNamespace, logErrandNumber);
			return caseDataMapper.toCaseStatusResponses(errands);
		} catch (Exception e) {
			LOGGER.error("Error while fetching errands for municipalityId: {} and namespace: {} and errandNumber: {}, returning empty list.", logMunicipalityId, logNamespace, logErrandNumber, e);
			return emptyList();
		}
	}

	/**
	 * Walks through every page of the search result. Fetching only the first page silently dropped matches for searches
	 * yielding more than one page of errands.
	 * <p>
	 * A page that fails to load ends the walk instead of failing the whole search, so the errands already fetched are
	 * kept rather than discarded. With up to {@link PaginationUtil#PAGE_SIZE} requests per search, letting one late
	 * failure void every preceding page would turn a partial outage into an empty result.
	 */
	private List<Errand> findAllErrands(final String municipalityId, final String namespace, final String filter) {
		final var logMunicipalityId = sanitizeForLogging(municipalityId);
		final var logNamespace = sanitizeForLogging(namespace);

		return PaginationUtil.fetchAllPages(pageNumber -> {
			try {
				return client.getErrands(municipalityId, namespace, filter, PageRequest.of(pageNumber, PaginationUtil.PAGE_SIZE));
			} catch (Exception e) {
				LOGGER.warn("Failed to fetch page {} of errands for municipalityId: {} and namespace: {}, returning the errands fetched so far",
					pageNumber, logMunicipalityId, logNamespace, e);
				return null;
			}
		}, "errands for municipalityId: %s and namespace: %s".formatted(logMunicipalityId, logNamespace));
	}
}
