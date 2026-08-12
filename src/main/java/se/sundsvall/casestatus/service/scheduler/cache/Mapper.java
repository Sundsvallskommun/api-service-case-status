package se.sundsvall.casestatus.service.scheduler.cache;

import generated.client.oep_integrator.CaseStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import us.codecraft.xsoup.Xsoup;

import static java.util.Optional.ofNullable;
import static se.sundsvall.casestatus.util.FormattingUtil.formatDateTime;

public final class Mapper {

	private static final String XPATH_FLOW_INSTANCE_ID_VALUE = "//header/FlowInstanceID/text()";
	private static final String XPATH_FAMILY_ID_VALUE = "//header/flow/familyId/text()";
	private static final String XPATH_ERRAND_TYPE_VALUE = "//header/flow/name/text()";
	private static final String XPATH_FIRST_SUBMITTED_VALUE = "//header/FirstSubmitted/text()";
	private static final String XPATH_LAST_STATUS_CHANGE_VALUE = "//header/LastSubmitted/text()";

	private Mapper() {}

	/**
	 * Parses an Open-E case payload. The payload is XML and must be parsed with the XML parser - jsoup's HTML parser
	 * discards the CDATA sections the payload wraps most of its text values in.
	 */
	public static Document parsePayload(final String payload) {
		return Jsoup.parse(payload, "", Parser.xmlParser());
	}

	public static CaseEntity toCompanyCaseEntity(final CaseStatus caseStatus, final Document payload, final String contentType, final String organisationNumber, final String municipalityId) {
		return CaseEntity.builder()
			.withStatus(caseStatus.getName())
			.withContentType(contentType)
			.withFlowInstanceId(select(payload, XPATH_FLOW_INSTANCE_ID_VALUE))
			.withFamilyId(select(payload, XPATH_FAMILY_ID_VALUE))
			.withErrandType(select(payload, XPATH_ERRAND_TYPE_VALUE))
			.withFirstSubmitted(formatDateTime(select(payload, XPATH_FIRST_SUBMITTED_VALUE)))
			.withLastStatusChange(formatDateTime(select(payload, XPATH_LAST_STATUS_CHANGE_VALUE)))
			.withMunicipalityId(municipalityId)
			.withOrganisationNumber(organisationNumber)
			.build();
	}

	/**
	 * Evaluates the given XPath against the case payload. CDATA wrapped values carry the surrounding whitespace of the
	 * enclosing element, so the result is trimmed. Returns null when the payload does not contain the element.
	 */
	private static String select(final Document payload, final String xPath) {
		return ofNullable(Xsoup.select(payload, xPath).get())
			.map(String::trim)
			.orElse(null);
	}
}
