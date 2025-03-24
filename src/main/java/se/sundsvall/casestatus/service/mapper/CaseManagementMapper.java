package se.sundsvall.casestatus.service.mapper;

import static java.util.Optional.ofNullable;
import static se.sundsvall.casestatus.utility.Constants.MISSING;
import static se.sundsvall.casestatus.utility.Constants.UNKNOWN;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;

@Component
public final class CaseManagementMapper {

	private final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;
	private final CaseTypeRepository caseTypeRepository;

	static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	public CaseManagementMapper(
		final CaseManagementOpeneViewRepository caseManagementOpeneViewRepository,
		final CaseTypeRepository caseTypeRepository) {
		this.caseManagementOpeneViewRepository = caseManagementOpeneViewRepository;
		this.caseTypeRepository = caseTypeRepository;
	}

	public CaseStatusResponse toCaseStatusResponse(final CaseStatusDTO caseStatus, final String municipalityId) {
		return CaseStatusResponse.builder()
			.withCaseId(caseStatus.getCaseId())
			.withCaseType(getServiceName(caseStatus.getServiceName(), caseStatus.getCaseType(), municipalityId))
			.withStatus(getStatus(caseStatus.getStatus()))
			.withLastStatusChange(getTimestamp(caseStatus.getTimestamp()))
			.withFirstSubmitted(MISSING)
			.withSystem(ofNullable(caseStatus.getSystem()).map(CaseStatusDTO.SystemEnum::toString).orElse(UNKNOWN))
			.withExternalCaseId(caseStatus.getExternalCaseId())
			// TODO: Add the mappings for the following fields when CaseManagement is updated.
			.withErrandNumber(null)
			.withNamespace(null)
			.build();
	}

	/**
	 * Translates the CaseManagement ID to the corresponding OpenE ID or returns the original status if no mapping is found.
	 *
	 * @param  originalStatus The original CaseManagement status.
	 * @return                The corresponding OpenE status or the original status if no mapping is found.
	 */
	String getStatus(final String originalStatus) {
		return caseManagementOpeneViewRepository.findByCaseManagementId(originalStatus)
			.map(CaseManagementOpeneView::getOpenEId)
			.orElse(originalStatus);
	}

	/**
	 * Formats the timestamp to a predetermined format or returns 'Saknas' if the timestamp is null.
	 *
	 * @param  originalTimestamp The original timestamp.
	 * @return                   The formatted timestamp or 'Saknas' if the timestamp is null.
	 */
	String getTimestamp(final LocalDateTime originalTimestamp) {
		return ofNullable(originalTimestamp)
			.map(DATE_TIME_FORMATTER::format)
			.orElse(MISSING);
	}

	/**
	 * Returns the service name if it is present, otherwise it tries to get a description from the database. If no
	 * description is can be found in the database, it returns 'Saknas'.
	 *
	 * @param  serviceName    The service name, returned from CaseManagement.
	 * @param  caseType       The case type, used to look up the description in the database.
	 * @param  municipalityId The municipality id, used to look up the description in the database.
	 * @return                The service name if it is present, otherwise a description from the database. If no
	 *                        description is found, it returns 'Saknas'.
	 */
	String getServiceName(final String serviceName, final String caseType, final String municipalityId) {
		return ofNullable(serviceName)
			.orElse(caseTypeRepository.findByEnumValueAndMunicipalityId(caseType, municipalityId)
				.map(CaseTypeEntity::getDescription)
				.orElse(MISSING));
	}

}
