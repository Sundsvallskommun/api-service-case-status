package se.sundsvall.casestatus.integration.opene.rest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import se.sundsvall.casestatus.service.scheduler.cache.Mapper;
import se.sundsvall.casestatus.service.scheduler.cache.domain.FamilyId;
import us.codecraft.xsoup.Xsoup;

@Component
public class OpenEIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(OpenEIntegration.class);

	private final OpenEClient client;

	public OpenEIntegration(final OpenEClient client) {
		this.client = client;
	}

	public Optional<String> getPdf(final String externalCaseId) {

		try {
			return Optional.of(Base64.getEncoder().encodeToString(client.getPDF(externalCaseId)));
		} catch (final Exception e) {
			LOG.info("Unable to get pdf for external id {}", externalCaseId, e);
			return Optional.empty();
		}
	}

	public byte[] getErrandIds(final FamilyId familyID) {

		try {
			return client.getErrandIds(String.valueOf(familyID.getValue()));
		} catch (final Exception e) {
			LOG.info("Unable to get errandIds for familyId {}", familyID, e);
			return "".getBytes();
		}
	}

	public byte[] getErrand(final String flowInstanceId) {
		try {
			return client.getErrand(flowInstanceId);
		} catch (final Exception e) {
			LOG.info("Unable to get errand for flowInstanceId {}", flowInstanceId, e);
			return "".getBytes();
		}
	}

	public byte[] getErrandStatus(final String flowInstanceId) {
		try {
			return client.getErrandStatus(flowInstanceId);
		} catch (final Exception e) {
			LOG.info("Unable to get errandStatus for flowInstanceId {}", flowInstanceId, e);
			return "".getBytes();
		}
	}

	public List<CaseEntity> getCaseStatuses(final String municipalityId, final String legalId) {
		try {
			final var response = new String(client.getErrands(legalId), StandardCharsets.ISO_8859_1);
			final var flowInstances = Xsoup.select(Jsoup.parse(response), "//FlowInstances/flowinstance").getElements();

			final var idList = flowInstances.stream().map(flowInstance -> Xsoup.select(flowInstance, "flowInstanceId/text()").get())
				.toList();

			return idList.stream()
				.map(flowInstanceId -> {
					final var errand = Jsoup.parse(new String(getErrand(flowInstanceId), StandardCharsets.ISO_8859_1));
					final var statusString = Jsoup.parse(new String(getErrandStatus(flowInstanceId), StandardCharsets.ISO_8859_1));

					return Mapper.toPrivateCaseEntity(statusString, errand, legalId, municipalityId);

				}).toList();

		} catch (final Exception e) {
			LOG.info("Unable to get case statuses for legal id {}", legalId, e);
			return List.of();
		}
	}

}
