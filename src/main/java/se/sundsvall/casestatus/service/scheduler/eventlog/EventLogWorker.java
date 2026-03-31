package se.sundsvall.casestatus.service.scheduler.eventlog;

import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.Metadata;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.StatusesRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;
import se.sundsvall.casestatus.integration.eventlog.EventlogClient;
import se.sundsvall.casestatus.integration.messaging.MessagingIntegration;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.service.util.EnvironmentUtil;
import se.sundsvall.dept44.requestid.RequestId;

import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;
import static se.sundsvall.casestatus.util.Constants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.util.Constants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.util.Constants.VALID_CHANNELS;

@Component
public class EventLogWorker {

	private static final String UNHEALTHY_MESSAGE = "Failed to update openE status for case with external ID ";
	private static final String SLACK_MESSAGE_FORMAT = "CaseStatus [%s] - RequestID: %s - Failed to set status '%s' for case with external ID '%s'. Error: '%s'";
	private static final String LOG_STATUS_SUCCESS = "RequestID: {} - Successfully set status '{}' for case with external ID {}";
	private static final String LOG_STATUS_FAILURE = "RequestID: {} - Failed to set status for case with external ID {}: {}";
	private static final String STATUS = "Status";

	private final Logger log = getLogger(EventLogWorker.class);
	private final EventlogClient eventlogClient;
	private final OepIntegratorClient oepIntegratorClient;
	private final StatusesRepository statusesRepository;
	private final MessagingIntegration messagingIntegration;
	private final Duration clockSkew;
	private final EnvironmentUtil environmentUtil;

	public EventLogWorker(final EventlogClient eventlogClient, final OepIntegratorClient oepIntegratorClient,
		final StatusesRepository statusesRepository, final MessagingIntegration messagingIntegration,
		@Value("${scheduler.eventlog.clock-skew:PT5S}") final Duration clockSkew,
		final EnvironmentUtil environmentUtil) {

		this.eventlogClient = eventlogClient;
		this.oepIntegratorClient = oepIntegratorClient;
		this.statusesRepository = statusesRepository;
		this.messagingIntegration = messagingIntegration;
		this.clockSkew = clockSkew;
		this.environmentUtil = environmentUtil;
	}

	boolean updateOepCase(final String serviceName, final ExecutionInformationEntity executionInformation,
		final Consumer<String> setUnHealthyConsumer) {
		final var municipalityId = executionInformation.getMunicipalityId();
		final var filterString = "message ~ 'Status updated to' and created > '%s' and sourceType: 'Errand' and owner: '%s' and type: 'UPDATE'"
			.formatted(executionInformation.getLastSuccessfulExecution().minus(clockSkew), serviceName);
		final var events = fetchAllEvents(municipalityId, filterString);

		if (events.isEmpty()) {
			log.atInfo().setMessage("RequestID: {} - No events found for service {} in municipality {}")
				.addArgument(RequestId::get)
				.addArgument(serviceName).addArgument(municipalityId).log();
			return true;
		}

		var allSuccessful = true;
		for (final var event : events) {
			RequestId.reset();
			RequestId.init();
			final var metadata = toMetadataMap(event);
			final var externalCaseId = metadata.get("ExternalCaseId");
			final var status = statusesRepository.findByCaseManagementStatus(metadata.get(STATUS))
				.map(StatusesEntity::getOepStatus).orElse(null);

			if (externalCaseId == null) {
				log.atInfo().setMessage("RequestID: {} - No ExternalCaseId found for event, skipping")
					.addArgument(RequestId::get).log();
			} else if (status == null) {
				log.atInfo().setMessage("RequestID: {} - No matching OeP status found for CaseManagement status '{}', skipping")
					.addArgument(RequestId::get)
					.addArgument(() -> metadata.get(STATUS)).log();
			} else if (setStatusFailed(municipalityId, InstanceType.EXTERNAL, externalCaseId, status,
				setUnHealthyConsumer)) {
				allSuccessful = false;
			}
		}

		return allSuccessful;
	}

	boolean updateSupportManagementStatuses(final ExecutionInformationEntity executionInformation,
		final Consumer<String> setUnHealthyConsumer) {
		final var municipalityId = executionInformation.getMunicipalityId();
		final var filterString = "message:'Ärendet har uppdaterats.' and created > '%s' and sourceType: 'Errand' and owner: 'SupportManagement' and type: 'UPDATE'"
			.formatted(executionInformation.getLastSuccessfulExecution().minus(clockSkew));
		final var events = fetchAllEvents(municipalityId, filterString);

		if (events.isEmpty()) {
			log.atInfo().setMessage("RequestID: {} - No events found for municipality {}")
				.addArgument(RequestId::get)
				.addArgument(municipalityId).log();
			return true;
		}

		var allSuccessful = true;
		for (final var event : events) {
			RequestId.reset();
			RequestId.init();
			final var metadata = toMetadataMap(event);
			final var channel = metadata.get("Channel");
			final var externalCaseId = metadata.get("CaseId");
			final var status = Optional.ofNullable(channel).filter(VALID_CHANNELS::contains)
				.flatMap(_ -> statusesRepository.findBySupportManagementStatus(metadata.get(STATUS)))
				.map(StatusesEntity::getOepStatus).orElse(null);

			if (channel == null || !VALID_CHANNELS.contains(channel)) {
				log.atInfo().setMessage("RequestID: {} - Skipping event with logKey: {}, channel: '{}'")
					.addArgument(RequestId::get)
					.addArgument(event::getLogKey)
					.addArgument(channel).log();
			} else if (externalCaseId == null) {
				log.atInfo().setMessage("RequestID: {} - No CaseId found for event with logKey: {}, skipping")
					.addArgument(RequestId::get)
					.addArgument(event::getLogKey).log();
			} else if (status == null) {
				log.atInfo().setMessage("RequestID: {} - No matching OeP status found for SupportManagement status '{}', skipping")
					.addArgument(RequestId::get)
					.addArgument(() -> metadata.get(STATUS)).log();
			} else if (setStatusFailed(municipalityId, getInstanceType(channel), externalCaseId, status,
				setUnHealthyConsumer)) {
				allSuccessful = false;
			}
		}

		return allSuccessful;
	}

	private boolean setStatusFailed(final String municipalityId, final InstanceType instanceType,
		final String externalCaseId, final String status, final Consumer<String> setUnHealthyConsumer) {
		try {
			oepIntegratorClient.setStatus(municipalityId, instanceType, externalCaseId,
				new CaseStatusChangeRequest().name(status));
			log.atInfo().setMessage(LOG_STATUS_SUCCESS).addArgument(RequestId::get).addArgument(status)
				.addArgument(externalCaseId).log();
			return false;
		} catch (final Exception e) {
			log.atError().setMessage(LOG_STATUS_FAILURE).addArgument(RequestId::get).addArgument(externalCaseId)
				.addArgument(e::getMessage).log();
			setUnHealthyConsumer.accept(UNHEALTHY_MESSAGE + externalCaseId);
			messagingIntegration.sendSlackMessage(municipalityId, SLACK_MESSAGE_FORMAT.formatted(
				environmentUtil.extractEnvironment(), RequestId.get(), status, externalCaseId, e.getMessage()));
			return true;
		}
	}

	private List<Event> fetchAllEvents(final String municipalityId, final String filterString) {
		int pageNumber = 0;
		Page<Event> response;
		final var allEvents = new ArrayList<Event>();
		do {
			response = eventlogClient.getEvents(municipalityId, PageRequest.of(pageNumber, 100), filterString);
			allEvents.addAll(response.getContent());
			pageNumber++;
		} while (response.hasNext());

		return allEvents.stream().distinct().toList();
	}

	private Map<String, String> toMetadataMap(final Event event) {
		return event.getMetadata().stream().collect(toMap(Metadata::getKey, Metadata::getValue));
	}

	private InstanceType getInstanceType(final String channel) {
		return switch (channel) {
			case INTERNAL_CHANNEL_E_SERVICE -> InstanceType.INTERNAL;
			case EXTERNAL_CHANNEL_E_SERVICE -> InstanceType.EXTERNAL;
			default -> throw new IllegalStateException("Unexpected value: " + channel);
		};
	}

}
