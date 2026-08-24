package se.sundsvall.casestatus.integration.casemanagement;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.exception.ClientProblem;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Component
public class CaseManagementIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(CaseManagementIntegration.class);

	private final CaseManagementClient client;

	public CaseManagementIntegration(final CaseManagementClient client) {
		this.client = client;
	}

	public Optional<CaseStatusDTO> getCaseStatusForExternalId(final String externalCaseId, final String municipalityId) {
		try {
			return Optional.ofNullable(client.getCaseStatusForExternalCaseId(municipalityId, externalCaseId));
		} catch (final Exception e) {
			LOG.info("Unable to get case status for external id {}", sanitizeForLogging(externalCaseId), e);
			return empty();
		}
	}

	/**
	 * Apart from "no cases" (see {@link #noCasesAsEmptyList}) failures are deliberately not swallowed here:
	 * {@link se.sundsvall.casestatus.service.CaseAggregator} classifies them, so that an unreachable CaseManagement is
	 * reported to the caller as an unavailable source rather than as an empty result indistinguishable from "this
	 * organization has no cases".
	 */
	public List<CaseStatusDTO> getCaseStatusForOrganizationNumber(final String organizationNumber, final String municipalityId) {
		return noCasesAsEmptyList(() -> client.getCaseStatusForOrganizationNumber(municipalityId, organizationNumber));
	}

	/**
	 * Failures other than "no cases" are deliberately not swallowed here — see
	 * {@link #getCaseStatusForOrganizationNumber}.
	 */
	public List<CaseStatusDTO> getCaseStatusForPartyId(final String partyId, final String municipalityId) {
		return noCasesAsEmptyList(() -> client.getCaseStatusForPartyId(municipalityId, partyId));
	}

	/**
	 * CaseManagement answers 404 rather than an empty list when a party or organization has no cases, so a 404 means
	 * "nothing to contribute" and not "the source failed". Everything else propagates to the aggregator, which decides
	 * whether it degrades the source or fails the request.
	 */
	private List<CaseStatusDTO> noCasesAsEmptyList(final Supplier<List<CaseStatusDTO>> supplier) {
		try {
			return ofNullable(supplier.get()).orElse(emptyList());
		} catch (final ClientProblem e) {
			if (NOT_FOUND.equals(e.getStatus())) {
				return emptyList();
			}
			throw e;
		}
	}
}
