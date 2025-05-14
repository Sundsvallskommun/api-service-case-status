package se.sundsvall.casestatus.service.scheduler.eventlog;

import static org.slf4j.LoggerFactory.getLogger;
import static se.sundsvall.casestatus.service.mapper.SupportManagementMapper.getExternalCaseId;
import static se.sundsvall.casestatus.util.Constants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.util.Constants.INTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.util.Constants.VALID_CHANNELS;

import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.supportmanagement.Errand;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

	void updateStatus(final ExecutionInformationEntity executionInformation) {

		final var logKeys = getEvents(executionInformation).stream().distinct().toList();

		if (logKeys.isEmpty()) {
			log.info("RequestID: {} - No events found for municipality {}", RequestId.get(), executionInformation.getMunicipalityId());
			return;
		}

		final var namespaces = supportManagementService.getSupportManagementNamespaces();

		final var result = logKeys.stream()
			.map(id -> supportManagementService.getSupportManagementCaseById(executionInformation.getMunicipalityId(), namespaces, id))
			.toList();

		result.forEach(errand -> setStatus(executionInformation, errand));

	}

	private void setStatus(final ExecutionInformationEntity executionInformation, final Errand errand) {
		if (VALID_CHANNELS.contains(errand.getChannel())) {

			final var channel = Objects.requireNonNull(errand.getChannel());
			final var instanceType = getInstanceType(channel);
			final var externalCaseId = getExternalCaseId(errand);
			if (externalCaseId.isEmpty()) {
				log.warn("RequestID: {} - No external case ID found for errand {}", RequestId.get(), errand.getId());
				return;
			}

			final var openEId = caseManagementOpeneViewRepository
				.findByCaseManagementId(errand.getStatus())
				.orElseThrow()
				.getOpenEId();

			final CaseStatusChangeRequest statusChangeRequest = new CaseStatusChangeRequest().name(openEId);

			oepIntegratorClient.setStatus(
				executionInformation.getMunicipalityId(),
				instanceType,
				externalCaseId.get(),
				statusChangeRequest);
		}
	}

	private List<String> getEvents(final ExecutionInformationEntity executionInformation) {
		int pageNumber = 0;
		Page<Event> response;
		final var logKeys = new ArrayList<String>();

		final var filterString = "message:'Ärendet har uppdaterats.' and created > '%s' and sourceType: 'Errand' and owner: 'SupportManagement' and type: 'UPDATE'"
			.formatted(executionInformation.getLastSuccessfulExecution().minus(clockSkew));
		do {
			response = eventlogClient.getEvents(executionInformation.getMunicipalityId(), PageRequest.of(pageNumber, 100), filterString);
			logKeys.addAll(response.getContent().stream().map(Event::getLogKey).toList());
			pageNumber++;
		} while (response.hasNext());
		return logKeys;
	}

	private InstanceType getInstanceType(final String channel) {
		return switch (channel) {
			case INTERNAL_CHANNEL_E_SERVICE -> InstanceType.INTERNAL;
			case EXTERNAL_CHANNEL_E_SERVICE -> InstanceType.EXTERNAL;
			default -> throw new IllegalStateException("Unexpected value: " + channel);
		};
	}

}
