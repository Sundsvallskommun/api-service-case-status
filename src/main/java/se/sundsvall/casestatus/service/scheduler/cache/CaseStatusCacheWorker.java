package se.sundsvall.casestatus.service.scheduler.cache;

import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.party.PartyType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.scheduler.cache.domain.FamilyId;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import us.codecraft.xsoup.Xsoup;

@Component
public class CaseStatusCacheWorker {

	private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCacheWorker.class);
	private static final String PRIVATE = "private";
	private static final String ORG = "org";
	private final OepIntegratorClient oepIntegratorClient;
	private final PartyIntegration partyIntegration;
	private final CaseRepository caseRepository;
	private final Dept44HealthUtility dept44HealthUtility;
	@Value("${cache.scheduled.name}")
	private String jobName;

	public CaseStatusCacheWorker(final OepIntegratorClient oepIntegratorClient, final PartyIntegration partyIntegration,
		final CaseRepository caseRepository, final Dept44HealthUtility dept44HealthUtility) {
		this.oepIntegratorClient = oepIntegratorClient;
		this.partyIntegration = partyIntegration;
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

		response.forEach(caseEnvelope -> parseFlowInstance(caseEnvelope.getFlowInstanceId(), familyId));
	}

	void parseFlowInstance(final String flowInstanceID, final FamilyId familyId) {
		final var oepCase = oepIntegratorClient.getCase(familyId.getMunicipalityId(), InstanceType.EXTERNAL, flowInstanceID);

		if (oepCase == null || oepCase.getPayload() == null) {
			LOG.info("Unable to get errand with ID: {}, of family: {}", flowInstanceID, familyId);
			return;
		}

		final var statusDocument = oepIntegratorClient.getCaseStatus(familyId.getMunicipalityId(), InstanceType.EXTERNAL, flowInstanceID);

		final var privateOrOrganisation = parseOrganizationNumberOrPersonId(Xsoup.select(oepCase.getPayload(), "//values").getElements(), familyId);

		switch (privateOrOrganisation.getKey()) {
			case ORG -> {
				if ((privateOrOrganisation.getValue() == null) || privateOrOrganisation.getValue().isEmpty()) {
					LOG.info("Unable to get organisation number will not cache errand with ID: {}, of family: {}", flowInstanceID, familyId);
					return;
				}
				LOG.debug("Able to get orgNumber, will cache errand with Id: {}, of family: {} as Organization", flowInstanceID, familyId);
				caseRepository.save(Mapper.toCompanyCaseEntity(statusDocument, oepCase, privateOrOrganisation.getValue(), familyId.getMunicipalityId()));
			}
			case PRIVATE -> {
				final var personId = partyIntegration.getPartyIdByLegalId(familyId.getMunicipalityId(), privateOrOrganisation.getValue()).get(PartyType.PRIVATE);
				if ((personId == null) || personId.isEmpty()) {
					LOG.info("Unable to get personId, will not cache errand with Id: {}, of family: {}", flowInstanceID, familyId);
					return;
				}
				LOG.debug("Able to get personId, will cache errand with Id: {}, of family: {} as Private", flowInstanceID, familyId);
				caseRepository.save(Mapper.toPrivateCaseEntity(statusDocument, oepCase, personId, familyId.getMunicipalityId()));
			}
			default -> {
				LOG.debug("Unable to get personId or OrgNumber, will cache errand with Id: {}, of family: {} as Unknown", flowInstanceID, familyId);
				caseRepository.save(Mapper.toUnknownCaseEntity(statusDocument, oepCase, familyId.getMunicipalityId()));
			}

		}
	}

	private Pair<String, String> parseOrganizationNumberOrPersonId(final Elements flowInstance, final FamilyId familyID) {
		if (familyID.isApplicant() && !flowInstance.select("type").isEmpty()) {
			return parseApplicantInfo(flowInstance);
		}
		return switch (familyID) {
			case ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR, TILLSTANDFORSALJNINGTOBAKSVAROR,
				ANMALANFORSELJNINGSERVERINGFOLKOL, FORSALJNINGECIGGARETTER -> new ImmutablePair<>(ORG, Xsoup.select(flowInstance.first(), "company/organisationsnummer/text()")
					.get() != null ? Xsoup.select(flowInstance.first(), "company/organisationsnummer/text()").get()
						: Xsoup.select(flowInstance.first(), "chooseCompany/organizationNumber/text()").get());
			default -> new ImmutablePair<>("", "");
		};
	}

	private Pair<String, String> parseApplicantInfo(final Elements openEObj) {
		if ("Privat".equals(Xsoup.select(openEObj.first(), "type/value/text()").get()) || "Privatperson".equals(Xsoup.select(openEObj.first(), "Values/type/Value/text()").get())) {
			return new ImmutablePair<>(PRIVATE, Xsoup.select(openEObj.first(), "applicant/SocialSecurityNumber/text()").get().trim());

		}
		if (Xsoup.select(openEObj.first(), "applicant/applicantidentifier").get() != null) {
			return new ImmutablePair<>(ORG, Xsoup.select(openEObj.first(), "applicant/applicantidentifier/text()").get());
		}
		return new ImmutablePair<>(ORG, "Saknas");
	}

}
