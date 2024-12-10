package se.sundsvall.casestatus.util.casestatuscache;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.citizen.CitizenIntegration;
import se.sundsvall.casestatus.integration.db.CompanyRepository;
import se.sundsvall.casestatus.integration.db.PrivateRepository;
import se.sundsvall.casestatus.integration.db.UnknownRepository;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;
import se.sundsvall.casestatus.util.Mapper;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;
import us.codecraft.xsoup.Xsoup;

@Component
public class CaseStatusCacheWorker {

	private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCacheWorker.class);

	private static final String PRIVATE = "private";

	private static final String ORG = "org";

	private final OpenEIntegration openEIntegration;

	private final CitizenIntegration citizenIntegration;

	private final Mapper mapper;

	private final UnknownRepository unknownRepository;

	private final PrivateRepository privateRepository;

	private final CompanyRepository companyRepository;

	public CaseStatusCacheWorker(final OpenEIntegration openEIntegration, final CitizenIntegration citizenIntegration, final Mapper mapper, final UnknownRepository unknownRepository, final PrivateRepository privateRepository,
		final CompanyRepository companyRepository) {
		this.openEIntegration = openEIntegration;
		this.citizenIntegration = citizenIntegration;

		this.mapper = mapper;
		this.unknownRepository = unknownRepository;
		this.privateRepository = privateRepository;
		this.companyRepository = companyRepository;
	}

	void cacheStatusesForFamilyID(final FamilyId familyID) {

		LOG.debug("Running for family: {}", familyID);
		final var response = new String(openEIntegration.getErrandIds(familyID), StandardCharsets.ISO_8859_1);

		final var flowInstances = Xsoup.select(Jsoup.parse(response), "//FlowInstances/flowinstance").getElements();

		if (!flowInstances.isEmpty()) {
			flowInstances.forEach(flowInstance -> parseFlowInstance(flowInstance, familyID));
		}
	}

	void parseFlowInstance(final Element flowInstance, final FamilyId familyId) {
		final var flowInstanceID = Xsoup.select(flowInstance, "flowInstanceId/text()").get();
		final var errandDocument = Jsoup.parse(new String(openEIntegration.getErrand(flowInstanceID), StandardCharsets.ISO_8859_1));
		final var statusDocument = Jsoup.parse(new String(openEIntegration.getErrandStatus(flowInstanceID), StandardCharsets.ISO_8859_1));

		final var privateOrOrganisation = parseOrganizationNumberOrPersonId(Xsoup.select(errandDocument, "//values").getElements(), familyId);

		switch (privateOrOrganisation.getKey()) {
			case ORG -> {
				if ((privateOrOrganisation.getValue() == null) || privateOrOrganisation.getValue().isEmpty()) {
					LOG.info("Unable to get organisation number will not cache errand with ID: {}, of family: {}", flowInstanceID, familyId);
					return;
				}
				LOG.debug("Able to get orgNumber, will cache errand with Id: {}, of family: {} as Organization", flowInstanceID, familyId);
				companyRepository.save(mapper.toCacheCompanyCaseStatus(statusDocument, errandDocument, privateOrOrganisation.getValue(), familyId.getMunicipalityId()));
			}
			case PRIVATE -> {
				final var personId = citizenIntegration.getPersonId(privateOrOrganisation.getValue());
				if ((personId == null) || personId.isEmpty()) {
					LOG.info("Unable to get personId, will not cache errand with Id: {}, of family: {}", flowInstanceID, familyId);
					return;
				}
				LOG.debug("Able to get personId, will cache errand with Id: {}, of family: {} as Private", flowInstanceID, familyId);
				privateRepository.save(mapper.toCachePrivateCaseStatus(statusDocument, errandDocument, personId, familyId.getMunicipalityId()));
			}
			default -> {
				LOG.debug("Unable to get personId or OrgNumber, will cache errand with Id: {}, of family: {} as Unknown", flowInstanceID, familyId);
				unknownRepository.save(mapper.toCacheUnknownCaseStatus(statusDocument, errandDocument, familyId.getMunicipalityId()));
			}

		}
	}

	private Pair<String, String> parseOrganizationNumberOrPersonId(final Elements flowInstance, final FamilyId familyID) {
		if (familyID.isApplicant() && !flowInstance.select("type").isEmpty()) {
			return parseApplicantInfo(flowInstance);
		}
		return switch (familyID) {
			case NYBYGGNADSKARTA -> Xsoup.select(flowInstance.first(), "clientEstablishment/text()").get() != null ? new ImmutablePair<>(ORG, Xsoup.select(flowInstance.first(), "company/OrganizationNumber/text()").get())
				: new ImmutablePair<>(PRIVATE, Xsoup.select(flowInstance.first(), "clientPrivate/SocialSecurityNumber/text()").get());
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
