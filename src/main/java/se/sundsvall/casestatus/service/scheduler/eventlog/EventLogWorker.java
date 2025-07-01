package se.sundsvall.casestatus.service.scheduler.eventlog;

import static org.slf4j.LoggerFactory.getLogger;
import static se.sundsvall.casestatus.service.mapper.SupportManagementMapper.getExternalCaseId;
import static se.sundsvall.casestatus.util.Constants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.util.Constants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.util.Constants.VALID_CHANNELS;

import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.Metadata;
import generated.se.sundsvall.supportmanagement.Errand;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casestatus.integration.eventlog.EventlogClient;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.service.SupportManagementService;
import se.sundsvall.dept44.requestid.RequestId;

@Component
public class EventLogWorker {

	private final Logger log = getLogger(EventLogWorker.class);
	private final EventlogClient eventlogClient;
	private final SupportManagementService supportManagementService;
	private final OepIntegratorClient oepIntegratorClient;
	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;
	private final Duration clockSkew;

	public EventLogWorker(
		final EventlogClient eventlogClient,
		final SupportManagementService supportManagementService,
		final OepIntegratorClient oepIntegratorClient,
		final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository,
		@Value("${scheduler.eventlog.clock-skew:PT5S}") final Duration clockSkew) {
		this.eventlogClient = eventlogClient;
		this.supportManagementService = supportManagementService;
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.clockSkew = clockSkew;
	}

	void updateStatus(final ExecutionInformationEntity executionInformation, final Consumer<String> setUnHealthyConsumer) {

		final var events = getEvents(executionInformation).stream().distinct().toList();

		if (events.isEmpty()) {
			log.info("RequestID: {} - No events found for municipality {}", RequestId.get(), executionInformation.getMunicipalityId());
			return;
		}

		final var result = events.stream()
			.map(event -> supportManagementService.getSupportManagementCaseById(executionInformation.getMunicipalityId(), getNamespace(event), event.getLogKey()))
			.filter(Objects::nonNull)
			.toList();

		result.forEach(errand -> {
			try {
				setStatus(executionInformation, errand, setUnHealthyConsumer);
			} catch (final Exception e) {
				log.error("RequestID: {} - Error setting status for errand {}: {}", RequestId.get(), errand.getId(), e.getMessage());
				setUnHealthyConsumer.accept("Error setting status for errand " + errand.getId());
			}
		});

	}

	private void setStatus(final ExecutionInformationEntity executionInformation, final Errand errand, final Consumer<String> setUnHealthyConsumer) {
		if (errand.getChannel() != null && VALID_CHANNELS.contains(errand.getChannel())) {

			log.info("RequestID: {} - setStatus on errand with ID: {}", RequestId.get(), errand.getId());

			final var channel = Objects.requireNonNull(errand.getChannel());
			final var instanceType = getInstanceType(channel);
			final var externalCaseId = getExternalCaseId(errand);
			if (externalCaseId.isEmpty()) {
				log.warn("RequestID: {} - No external case ID found for errand: {}", RequestId.get(), errand.getId());
				return;
			}

			final String openEId;
			try {
				openEId = caseManagementOpeneViewRepository
					.findByCaseManagementId(errand.getStatus())
					.orElseThrow()
					.getOpenEId();
			} catch (final Exception e) {
				setUnHealthyConsumer.accept("Mismatch for status " + errand.getStatus() + "was not found in OpenEId mapping");
				log.error("RequestID: {} - Failed to find OpenEId for errand {}: {}", RequestId.get(), errand.getId(), e.getMessage());
				return;
			}

			log.info("RequestID: {} - found mapped OpenEId: {} for errand: {}", RequestId.get(), openEId, errand.getId());

			final CaseStatusChangeRequest statusChangeRequest = new CaseStatusChangeRequest().name(openEId);

			try {
				oepIntegratorClient.setStatus(
					executionInformation.getMunicipalityId(),
					instanceType,
					externalCaseId.get(),
					statusChangeRequest);
			} catch (final Exception e) {
				setUnHealthyConsumer.accept("Failed to update openE status for errand " + errand.getId());
				log.error("RequestID: {} - Failed to set status for errand {} with external case ID {}: {}", RequestId.get(), errand.getId(), externalCaseId.get(), e.getMessage());
			}
		}
	}

	private List<Event> getEvents(final ExecutionInformationEntity executionInformation) {
		int pageNumber = 0;
		Page<Event> response;
		final var allEvents = new ArrayList<Event>();
		final var filterString = "message:'Ärendet har uppdaterats.' and created > '%s' and sourceType: 'Errand' and owner: 'SupportManagement' and type: 'UPDATE'"
			.formatted(executionInformation.getLastSuccessfulExecution().minus(clockSkew));
		do {
			response = eventlogClient.getEvents(executionInformation.getMunicipalityId(), PageRequest.of(pageNumber, 100), filterString);
			allEvents.addAll(response.getContent());
			pageNumber++;
		} while (response.hasNext());
		return allEvents;
	}

	private InstanceType getInstanceType(final String channel) {
		return switch (channel) {
			case INTERNAL_CHANNEL_E_SERVICE -> InstanceType.INTERNAL;
			case EXTERNAL_CHANNEL_E_SERVICE -> InstanceType.EXTERNAL;
			default -> throw new IllegalStateException("Unexpected value: " + channel);
		};
	}

	private String getNamespace(final Event event) {
		return event.getMetadata().stream()
			.filter(key -> "Namespace".equals(key.getKey()))
			.map(Metadata::getValue)
			.findFirst()
			.orElse(null);
	}

}
