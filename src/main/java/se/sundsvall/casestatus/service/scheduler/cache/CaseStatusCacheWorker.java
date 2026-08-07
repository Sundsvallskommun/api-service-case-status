package se.sundsvall.casestatus.service.scheduler.cache;

import generated.client.oep_integrator.InstanceType;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.service.scheduler.cache.domain.FamilyId;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import us.codecraft.xsoup.Xsoup;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class CaseStatusCacheWorker {

	private static final String XPATH_ORGANISATION_NUMBER = "company/organisationsnummer/text()";
	private static final String XPATH_CHOOSE_COMPANY_ORGANIZATION_NUMBER = "chooseCompany/organizationNumber/text()";

	private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCacheWorker.class);
	private final OepIntegratorClient oepIntegratorClient;
	private final CaseRepository caseRepository;
	private final Dept44HealthUtility dept44HealthUtility;
	@Value("${cache.scheduled.name}")
	private String jobName;

	public CaseStatusCacheWorker(final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository, final Dept44HealthUtility dept44HealthUtility) {
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseRepository = caseRepository;
		this.dept44HealthUtility = dept44HealthUtility;
	}

	public void cacheStatusesForFamilyId(final FamilyId familyId) {

		LOG.debug("Running for familyId: {}", familyId);
		final var response = oepIntegratorClient.getCases(familyId.getMunicipalityId(), InstanceType.EXTERNAL, familyId.getValue());

		if (response == null || response.isEmpty()) {
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Unable to get errandIds for familyId: " + familyId);
			return;
		}

		final var cachedCount = response.stream()
			.filter(caseEnvelope -> parseFlowInstance(caseEnvelope.getFlowInstanceId(), familyId))
			.count();

		// Skipping a single errand is expected, but caching none of them means the errands or their statuses cannot be
		// reached at all. Staying silent there would let the cache go stale behind a green health indicator.
		if (cachedCount == 0) {
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Unable to cache any errand for familyId: " + familyId);
		}
	}

	/**
	 * @return true if the errand was cached, false if it had to be skipped
	 */
	boolean parseFlowInstance(final String flowInstanceID, final FamilyId familyId) {
		final var oepCase = oepIntegratorClient.getCase(familyId.getMunicipalityId(), InstanceType.EXTERNAL, flowInstanceID);

		if (oepCase == null || oepCase.getPayload() == null) {
			LOG.info("Unable to get errand with ID: {}, of family: {}", flowInstanceID, familyId);
			return false;
		}

		final var statusDocument = oepIntegratorClient.getCaseStatus(familyId.getMunicipalityId(), InstanceType.EXTERNAL, flowInstanceID);

		if (statusDocument == null) {
			LOG.info("Unable to get status for errand with ID: {}, of family: {}, will not cache errand", flowInstanceID, familyId);
			return false;
		}

		final var payload = Mapper.parsePayload(oepCase.getPayload());
		final var organisationNumber = parseOrganizationNumber(Xsoup.select(payload, "//values").getElements().first());

		if (isBlank(organisationNumber)) {
			LOG.info("Unable to get organisation number will not cache errand with ID: {}, of family: {}", flowInstanceID, familyId);
			return false;
		}

		LOG.debug("Able to get orgNumber, will cache errand with Id: {}, of family: {} as Organization", flowInstanceID, familyId);
		caseRepository.save(Mapper.toCompanyCaseEntity(statusDocument, payload, oepCase.getTitle(), organisationNumber, familyId.getMunicipalityId()));
		return true;
	}

	/**
	 * All currently cached family ids belong to company e-services, so the organisation number is read from either of the
	 * two form layouts in use. Returns null when the payload holds neither.
	 */
	private String parseOrganizationNumber(final Element values) {
		return ofNullable(values)
			.map(element -> ofNullable(Xsoup.select(element, XPATH_ORGANISATION_NUMBER).get())
				.orElseGet(() -> Xsoup.select(element, XPATH_CHOOSE_COMPANY_ORGANIZATION_NUMBER).get()))
			.map(String::trim)
			.orElse(null);
	}
}
