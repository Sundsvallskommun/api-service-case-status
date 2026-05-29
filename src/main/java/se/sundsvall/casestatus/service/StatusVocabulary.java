package se.sundsvall.casestatus.service;

import generated.client.oep_integrator.CaseStatus;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import se.sundsvall.casestatus.integration.db.StatusesRepository;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static se.sundsvall.casestatus.util.Constants.DEFAULT_EXTERNAL_STATUS;

/**
 * Central translator between the three status domains the service deals with (CaseManagement, OeP and
 * SupportManagement)
 * and the external status string exposed to consumers. All access to {@link StatusesRepository} for translation
 * purposes
 * lives here so the rest of the service does not need to know how the mapping table is shaped.
 */
@Component
public class StatusVocabulary {

	private final StatusesRepository repository;

	public StatusVocabulary(final StatusesRepository repository) {
		this.repository = repository;
	}

	/**
	 * Looks up the OeP status that corresponds to the given CaseManagement status. Returns an empty Optional when no
	 * mapping exists so the caller can decide whether to fall back, throw or skip.
	 */
	public Optional<String> findOepStatusForCaseManagementStatus(final String caseManagementStatus) {
		if (isBlank(caseManagementStatus)) {
			return Optional.empty();
		}
		return repository.findByCaseManagementStatus(caseManagementStatus)
			.map(StatusesEntity::getOepStatus);
	}

	/**
	 * Translates an OeP {@link CaseStatus} envelope to an external status string. Returns null when the input is missing
	 * or carries no usable status. When the status is present but unmapped the configured default external status is
	 * used.
	 */
	public String translateOepStatus(final CaseStatus oepStatus) {
		if (oepStatus == null) {
			return null;
		}
		return translateOepStatus(oepStatus.getStatus());
	}

	/**
	 * Translates an OeP status string to an external status string. Returns null when the input is blank. When the status
	 * is present but unmapped the configured default external status is used.
	 */
	public String translateOepStatus(final String oepStatus) {
		if (isBlank(oepStatus)) {
			return null;
		}
		return repository.findByOepStatus(oepStatus).stream()
			.map(StatusesEntity::getExternalStatus)
			.filter(Objects::nonNull)
			.filter(StringUtils::hasText)
			.findFirst()
			.orElse(DEFAULT_EXTERNAL_STATUS);
	}

	/**
	 * Translates a CaseManagement status string to an external status string. Returns null when the input is blank. When
	 * the status is present but unmapped the configured default external status is used.
	 */
	public String translateCaseManagementStatus(final String caseManagementStatus) {
		if (isBlank(caseManagementStatus)) {
			return null;
		}
		return repository.findByCaseManagementStatus(caseManagementStatus)
			.map(StatusesEntity::getExternalStatus)
			.orElse(DEFAULT_EXTERNAL_STATUS);
	}

	/**
	 * Looks up the full {@link StatusesEntity} for a SupportManagement status. Returns an empty entity for blank input
	 * and a synthesized entity (carrying the input status and the default external status) when no row is found, so
	 * callers can rely on a non-null result.
	 */
	public StatusesEntity lookupBySupportManagementStatus(final String supportManagementStatus) {
		if (isBlank(supportManagementStatus)) {
			return StatusesEntity.builder().build();
		}
		return repository.findBySupportManagementStatus(supportManagementStatus)
			.orElse(StatusesEntity.builder()
				.withSupportManagementStatus(supportManagementStatus)
				.withExternalStatus(DEFAULT_EXTERNAL_STATUS)
				.build());
	}
}
