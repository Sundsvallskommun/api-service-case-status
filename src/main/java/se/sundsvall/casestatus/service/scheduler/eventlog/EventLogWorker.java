package se.sundsvall.casestatus.service.scheduler.eventlog;

import static se.sundsvall.casestatus.utility.Constants.VALID_CHANNELS;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.opene.SetStatus;
import generated.se.sundsvall.supportmanagement.Errand;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casestatus.integration.eventlog.EventlogClient;
import se.sundsvall.casestatus.integration.opene.soap.OpenECallbackIntegration;
import se.sundsvall.casestatus.service.Mapper;
import se.sundsvall.casestatus.service.SupportManagementService;

@Component
public class EventLogWorker {

	private final EventlogClient eventlogClient;
	private final SupportManagementService supportManagementService;
	private final OpenECallbackIntegration openECallbackIntegration;

	public EventLogWorker(final EventlogClient eventlogClient, final SupportManagementService supportManagementService, final OpenECallbackIntegration openECallbackIntegration) {
		this.eventlogClient = eventlogClient;
		this.supportManagementService = supportManagementService;
		this.openECallbackIntegration = openECallbackIntegration;
	}

	void updateStatus(final String municipalityId, final ExecutionInformationEntity executionTime) {

		final var logKeys = getEvents(municipalityId, executionTime);

		final var filter = createFilterString(logKeys);

		final var result = supportManagementService.getSupportManagementCases(municipalityId, filter);

		sortByChannel(result).forEach(this::doOpenECallback);
	}

	private void doOpenECallback(final String channel, final List<SetStatus> caseEntities) {
		caseEntities.forEach(caseEntity -> openECallbackIntegration.setStatus(channel, caseEntity));
	}

	private ArrayList<String> getEvents(final String municipalityId, final ExecutionInformationEntity executionTime) {
		int pageNumber = 0;
		Page<Event> response;
		final var logKeys = new ArrayList<String>();

		final var filterString = "message:'Ärendet har uppdaterats.' and created > '%s'".formatted(executionTime.getLastSuccessfulExecution());

		do {
			response = eventlogClient.getEvents(municipalityId, PageRequest.of(pageNumber, 100), filterString);
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
				Collectors.mapping(Mapper::toSetStatus, Collectors.toList())));
	}

	private String createFilterString(final ArrayList<String> logKeys) {
		return "id in [" + logKeys.stream()
			.map(logKey -> "'" + logKey + "'")
			.collect(Collectors.joining(","))
			+ "]";
	}

}
