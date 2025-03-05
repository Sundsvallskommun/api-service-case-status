package se.sundsvall.casestatus.service.scheduler.eventlog;

import static org.slf4j.LoggerFactory.getLogger;
import static se.sundsvall.casestatus.utility.Constants.VALID_CHANNELS;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.opene.SetStatus;
import generated.se.sundsvall.supportmanagement.Errand;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casestatus.integration.eventlog.EventlogClient;
import se.sundsvall.casestatus.integration.opene.soap.OpenECallbackIntegration;
import se.sundsvall.casestatus.service.Mapper;
import se.sundsvall.casestatus.service.SupportManagementService;
import se.sundsvall.dept44.requestid.RequestId;

@Component
public class EventLogWorker {

	private final Logger log = getLogger(EventLogWorker.class);
	private final EventlogClient eventlogClient;
	private final SupportManagementService supportManagementService;
	private final OpenECallbackIntegration openECallbackIntegration;
	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;
	private final Duration clockSkew;

	public EventLogWorker(final EventlogClient eventlogClient, final SupportManagementService supportManagementService, final OpenECallbackIntegration openECallbackIntegration, final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository,
		@Value("${scheduler.eventlog.clock-skew:PT5S}") final Duration clockSkew) {
		this.eventlogClient = eventlogClient;
		this.supportManagementService = supportManagementService;
		this.openECallbackIntegration = openECallbackIntegration;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.clockSkew = clockSkew;
	}

	void updateStatus(final ExecutionInformationEntity executionInformation) {

		final var logKeys = getEvents(executionInformation).stream().filter(Objects::nonNull).toList();

		if (logKeys.isEmpty()) {
			log.info("RequestID: {} - No events found for municipality {}", RequestId.get(), executionInformation.getMunicipalityId());
			return;
		}

		logKeys.forEach(errandId -> {
			final var errand = supportManagementService.getSupportManagementCase(executionInformation.getMunicipalityId(), errandId);
			if (errand != null) {
				sortByChannel(List.of(errand)).forEach(this::doOpenECallback);
			}
		});

	}

	private void doOpenECallback(final String channel, final List<SetStatus> caseEntities) {
		caseEntities.forEach(caseEntity -> openECallbackIntegration.setStatus(channel, caseEntity));
	}

	private ArrayList<String> getEvents(final ExecutionInformationEntity executionInformation) {
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

	private Map<String, List<SetStatus>> sortByChannel(final List<Errand> result) {
		return result.stream()
			.filter(errand -> errand.getChannel() != null && VALID_CHANNELS.contains(errand.getChannel()))
			.collect(Collectors.groupingBy(
				Errand::getChannel,
				Collectors.mapping(errand -> caseManagementOpeneViewRepository
					.findByCaseManagementId(errand.getStatus())
					.map(view -> Mapper.toSetStatus(errand, view.getOpenEId()))
					.orElse(null),
					Collectors.filtering(Objects::nonNull, Collectors.toList()))));
	}

}
