package se.sundsvall.casestatus.service;

import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.integration.casedata.CaseDataIntegration;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.OpenEMapper;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.hasText;
import static se.sundsvall.casestatus.util.Constants.CASE_NOT_FOUND;

@Service
public class CaseStatusService {

	private final CaseManagementIntegration caseManagementIntegration;
	private final OepIntegratorClient oepIntegratorClient;
	private final CaseRepository caseRepository;
	private final CaseManagementMapper caseManagementMapper;
	private final CaseDataIntegration caseDataIntegration;
	private final SupportManagementService supportManagementService;
	private final OpenEMapper openEMapper;
	private final StatusVocabulary statusVocabulary;
	private final CaseAggregator caseAggregator;

	public CaseStatusService(final CaseManagementIntegration caseManagementIntegration,
		final OepIntegratorClient oepIntegratorClient,
		final CaseRepository caseRepository,
		final CaseManagementMapper caseManagementMapper,
		final CaseDataIntegration caseDataIntegration,
		final SupportManagementService supportManagementService,
		final OpenEMapper openEMapper,
		final StatusVocabulary statusVocabulary,
		final CaseAggregator caseAggregator) {
		this.caseManagementIntegration = caseManagementIntegration;
		this.oepIntegratorClient = oepIntegratorClient;
		this.caseRepository = caseRepository;
		this.caseManagementMapper = caseManagementMapper;
		this.caseDataIntegration = caseDataIntegration;
		this.supportManagementService = supportManagementService;
		this.openEMapper = openEMapper;
		this.statusVocabulary = statusVocabulary;
		this.caseAggregator = caseAggregator;
	}

	public OepStatusResponse getOepStatus(final String externalCaseId, final String municipalityId) {
		final var cmStatus = caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(CaseStatusDTO::getStatus)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));

		final var oepStatus = statusVocabulary.findOepStatusForCaseManagementStatus(cmStatus)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Could not find matching open-E status for status %s".formatted(cmStatus)));

		return openEMapper.toOepStatusResponse(oepStatus);
	}

	public CaseStatusResponse getCaseStatus(final String externalCaseId, final String municipalityId) {
		return caseManagementIntegration.getCaseStatusForExternalId(externalCaseId, municipalityId)
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.or(() -> caseRepository.findByFlowInstanceIdAndMunicipalityId(externalCaseId, municipalityId)
				.map(openEMapper::toCaseStatusResponse))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, CASE_NOT_FOUND.formatted(externalCaseId)));
	}

	public CasePdfResponse getCasePdf(final String municipalityId, final String externalCaseId) {
		final var response = oepIntegratorClient.getCasePdfByFlowInstanceId(municipalityId, InstanceType.EXTERNAL, externalCaseId);
		final var body = response.getBody();

		if (response.getStatusCode().is4xxClientError() || body == null) {
			throw Problem.valueOf(NOT_FOUND, "Could not find PDF for case with externalCaseId %s".formatted(externalCaseId));
		}

		try {
			return openEMapper.toCasePdfResponse(externalCaseId, body);
		} catch (final IOException _) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to read PDF data");
		}
	}

	public List<CaseStatusResponse> getCaseStatuses(final String organizationNumber, final String municipalityId) {
		return caseAggregator.aggregateForOrg(organizationNumber, municipalityId);
	}

	public List<CaseStatusResponse> getCaseStatusesForParty(final String partyId, final String municipalityId, final boolean includeDrafts) {
		return caseAggregator.aggregateForParty(partyId, municipalityId, includeDrafts);
	}

	public List<CaseStatusResponse> getErrandStatuses(final String municipalityId, final String propertyDesignation, final String errandNumber) {
		if (hasText(propertyDesignation) && hasText(errandNumber)) {
			throw Problem.valueOf(BAD_REQUEST, "Both propertyDesignation and errandNumber cannot be provided at the same time");
		}
		if (propertyDesignation == null && errandNumber == null) {
			throw Problem.valueOf(BAD_REQUEST, "Either propertyDesignation or errandNumber must be provided");
		}

		if (hasText(propertyDesignation)) {
			return caseDataIntegration.getNamespaces().stream()
				.flatMap(namespace -> caseDataIntegration.getCaseDataCaseByPropertyDesignation(municipalityId, namespace, propertyDesignation).stream())
				.toList();
		}

		final var filterString = "errandNumber:'%s'".formatted(errandNumber);
		final var supportManagementCases = supportManagementService.getSupportManagementCases(municipalityId, filterString);
		final var caseStatusResponses = caseAggregator.mapSupportManagementErrands(municipalityId, supportManagementCases);
		final var caseDataCases = caseDataIntegration.getNamespaces().stream()
			.flatMap(namespace -> caseDataIntegration.getCaseDataCaseByErrandNumber(municipalityId, namespace, errandNumber).stream())
			.toList();

		return Stream.of(caseStatusResponses, caseDataCases)
			.flatMap(List::stream)
			.toList();
	}
}
