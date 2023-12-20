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
import se.sundsvall.casestatus.integration.db.DbIntegration;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;
import se.sundsvall.casestatus.util.Mapper;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;
import us.codecraft.xsoup.Xsoup;

@Component
public class CaseStatusCacheWorker {

	private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCacheWorker.class);

	private final OpenEIntegration openEIntegration;
	private final CitizenIntegration citizenIntegration;
	private final DbIntegration dbIntegration;
	private final Mapper mapper;

	private static final String PRIVATE = "private";
	private static final String ORG = "org";

	public CaseStatusCacheWorker(OpenEIntegration openEIntegration, CitizenIntegration citizenIntegration, DbIntegration dbIntegration, Mapper mapper) {
		this.openEIntegration = openEIntegration;
		this.citizenIntegration = citizenIntegration;
		this.dbIntegration = dbIntegration;
		this.mapper = mapper;
	}

	void cacheStatusesForFamilyID(FamilyId familyID) {
		final var response = new String(openEIntegration.getErrandIds(familyID), StandardCharsets.ISO_8859_1);

		final var flowInstances = Xsoup.select(Jsoup.parse(response), "//FlowInstances/flowinstance").getElements();

		if (!flowInstances.isEmpty()) {
			for (final Element flowInstance : flowInstances) {

				final var flowInstanceID = Xsoup.select(flowInstance, "flowInstanceID/text()").get();
				final var errandDocument = Jsoup.parse(new String(openEIntegration.getErrand(flowInstanceID), StandardCharsets.ISO_8859_1));
				final var statusDocument = Jsoup.parse(new String(openEIntegration.getErrandStatus(flowInstanceID), StandardCharsets.ISO_8859_1));

				final var privateOrOrganisation = parseOrganisationnumberOrPersonId(Xsoup.select(errandDocument, "//values").getElements(), familyID);

				switch (privateOrOrganisation.getKey()) {
					case ORG -> {
						if ((privateOrOrganisation.getValue() == null) || privateOrOrganisation.getValue().isEmpty()) {
							LOG.info("Unable to get organisation number will not cache errand with ID: {}, of family: {}", flowInstanceID, familyID);
							continue;
						}
						dbIntegration.writeToCompanyTable(mapper.toCacheCompanyCaseStatus(statusDocument, errandDocument, privateOrOrganisation.getValue()));

					}
					case PRIVATE -> {
						final var personId = citizenIntegration.getPersonId(privateOrOrganisation.getValue());
						if ((personId == null) || personId.isEmpty()) {
							LOG.info("Unable to get personId, will not cache errand with Id: {}, of family: {}", flowInstanceID, familyID);
							continue;
						}
						dbIntegration.writeToPrivateTable(mapper.toCachePrivateCaseStatus(statusDocument, errandDocument, personId));

					}
					default -> dbIntegration.writeToUnknownTable(mapper.toCacheUnknowCaseStatus(statusDocument, errandDocument));
				}
			}
		}
	}

	private Pair<String, String> parseOrganisationnumberOrPersonId(Elements flowInstance, FamilyId familyID) {
		if (familyID.isApplicant() && !flowInstance.select("type").isEmpty()) {
			return parseApplicantInfo(flowInstance);
		}
		return switch (familyID) {
			case NYBYGGNADSKARTA -> Xsoup.select(flowInstance.first(), "clientEstablishment/text()").get() != null ? new ImmutablePair<>(ORG, Xsoup.select(flowInstance.first(), "company/OrganizationNumber/text()").get())
				: new ImmutablePair<>(PRIVATE, Xsoup.select(flowInstance.first(), "clientPrivate/SocialSecurityNumber/text()").get());
			case ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR, TILLSTANDFORSALJNINGTOBAKSVAROR, ANMALANFORSELJNINGSERVERINGFOLKOL, FORSALJNINGECIGGARETTER -> new ImmutablePair<>(ORG, Xsoup.select(flowInstance.first(), "company/organisationsnummer/text()")
				.get() != null ? Xsoup.select(flowInstance.first(), "company/organisationsnummer/text()").get()
					: Xsoup.select(flowInstance.first(), "chooseCompany/organizationNumber/text()").get());
			default -> new ImmutablePair<>("", "");
		};
	}

	private Pair<String, String> parseApplicantInfo(Elements openEObj) {
		if ("Privat".equals(Xsoup.select(openEObj.first(), "type/value/text()").get()) || "Privatperson".equals(Xsoup.select(openEObj.first(), "Values/type/Value/text()").get())) {
			return new ImmutablePair<>(PRIVATE, Xsoup.select(openEObj.first(), "applicant/SocialSecurityNumber/text()").get().trim());

		}
		if (Xsoup.select(openEObj.first(), "applicant/applicantidentifier").get() != null) {
			return new ImmutablePair<>(ORG, Xsoup.select(openEObj.first(), "applicant/applicantidentifier/text()").get());
		}
		return new ImmutablePair<>(ORG, "Saknas");
	}

	public int mergeCaseStatusCache() {
		return dbIntegration.mergeCaseStatusCache();
	}
}
