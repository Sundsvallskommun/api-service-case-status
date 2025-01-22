package se.sundsvall.casestatus.service.scheduler.eventlog;

import static org.slf4j.LoggerFactory.getLogger;
import static se.sundsvall.casestatus.utility.Constants.VALID_CHANNELS;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.opene.SetStatus;
import generated.se.sundsvall.supportmanagement.Errand;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
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

	public EventLogWorker(final EventlogClient eventlogClient, final SupportManagementService supportManagementService, final OpenECallbackIntegration openECallbackIntegration, final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository) {
		this.eventlogClient = eventlogClient;
		this.supportManagementService = supportManagementService;
		this.openECallbackIntegration = openECallbackIntegration;
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
	}

	void updateStatus(final ExecutionInformationEntity executionInformation) {

		final var logKeys = getEvents(executionInformation);

		if (logKeys.isEmpty()) {
			log.info("RequestID: {} - No events found for municipality {}", RequestId.get(), executionInformation.getMunicipalityId());
			return;
		}

		final var filter = createFilterString(logKeys);

		final var result = supportManagementService.getSupportManagementCases(executionInformation.getMunicipalityId(), filter);

		sortByChannel(result).forEach(this::doOpenECallback);
	}

	private void doOpenECallback(final String channel, final List<SetStatus> caseEntities) {
		caseEntities.forEach(caseEntity -> openECallbackIntegration.setStatus(channel, caseEntity));
	}

	private ArrayList<String> getEvents(final ExecutionInformationEntity executionInformation) {
		int pageNumber = 0;
		Page<Event> response;
		final var logKeys = new ArrayList<String>();

		final var filterString = "message:'Ärendet har uppdaterats.' and created > '%s'".formatted(executionInformation.getLastSuccessfulExecution());

		do {
			response = eventlogClient.getEvents(executionInformation.getMunicipalityId(), PageRequest.of(pageNumber, 100), filterString);
			logKeys.addAll(response.getContent().stream().map(Event::getLogKey).toList());
			pageNumber++;
		} while (response.hasNext());
		return logKeys;
	}

	private Map<String, List<SetStatus>> sortByChannel(final List<Errand> result) {
		return result.stream()
			.filter(errand -> VALID_CHANNELS.contains(errand.getChannel()))
			.collect(Collectors.groupingBy(
				Errand::getChannel,
				Collectors.mapping(errand1 -> {
					final var status = caseManagementOpeneViewRepository.findByCaseManagementId(errand1.getStatus());
					return status.map(caseManagementOpeneView -> Mapper.toSetStatus(errand1, caseManagementOpeneView.getOpenEId()))
						.orElse(null);
				}, Collectors.toList())));
	}

	private String createFilterString(final ArrayList<String> logKeys) {
		return "id in [" + logKeys.stream()
			.map(logKey -> "'" + logKey + "'")
			.collect(Collectors.joining(","))
			+ "]";
	}

}
