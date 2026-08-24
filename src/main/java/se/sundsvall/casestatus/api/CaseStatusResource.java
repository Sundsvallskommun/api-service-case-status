package se.sundsvall.casestatus.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.service.AggregatedCases;
import se.sundsvall.casestatus.service.CaseStatusService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static se.sundsvall.casestatus.util.Constants.UNAVAILABLE_SOURCES_HEADER;

@RestController
@Validated
@RequestMapping("/{municipalityId}")
@Tag(name = "Status Resources")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CaseStatusResource {

	/**
	 * Search terms are embedded in a spring-filter expression by the integrations. The wildcard of the like operator is
	 * rejected here rather than escaped - it is a valid character inside a filter literal, and allowing it would let a
	 * single request match every errand in every namespace.
	 */
	static final String SEARCH_TERM_PATTERN = "[^*\\p{Cntrl}]*";
	static final String SEARCH_TERM_MESSAGE = "must not contain wildcards or control characters";
	static final int SEARCH_TERM_MAX_LENGTH = 255;

	private final CaseStatusService service;

	CaseStatusResource(final CaseStatusService service) {
		this.service = service;
	}

	@Operation(summary = "Get status in openE Platform format", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(path = "/{externalCaseId}/oepstatus", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<OepStatusResponse> getOepStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable final String externalCaseId) {
		return ok(service.getOepStatus(externalCaseId, municipalityId));
	}

	@Operation(summary = "Get case status", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(path = "/{externalCaseId}/status", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<CaseStatusResponse> getCaseStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable final String externalCaseId) {
		return ok(service.getCaseStatus(externalCaseId, municipalityId));
	}

	@Operation(summary = "Get case PDF", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(path = "/{externalCaseId}/pdf", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<CasePdfResponse> getCasePdf(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable final String externalCaseId) {
		return ok(service.getCasePdf(municipalityId, externalCaseId));
	}

	@Operation(summary = "Get organization statuses", responses = {
		@ApiResponse(responseCode = "200",
			description = "Successful Operation",
			useReturnTypeSchema = true,
			headers = @Header(name = UNAVAILABLE_SOURCES_HEADER,
				description = "Comma separated list of sources that did not answer, whose cases are therefore missing from the response. Absent when every source contributed.",
				schema = @Schema(type = "string")))
	})
	@GetMapping(path = "/{organizationNumber}/statuses", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<CaseStatusResponse>> getOrganisationStatuses(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable final String organizationNumber) {
		return toResponse(service.getCaseStatuses(organizationNumber, municipalityId));
	}

	@Operation(summary = "Get all statuses connected to a partyId", responses = {
		@ApiResponse(responseCode = "200",
			description = "Successful Operation",
			useReturnTypeSchema = true,
			headers = @Header(name = UNAVAILABLE_SOURCES_HEADER,
				description = "Comma separated list of sources that did not answer, whose cases are therefore missing from the response. Absent when every source contributed.",
				schema = @Schema(type = "string")))
	})
	@GetMapping(path = "/party/{partyId}/statuses", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<CaseStatusResponse>> getPartyStatuses(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "PartyId to find cases for", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable @ValidUuid final String partyId,
		@Parameter(name = "includeDrafts", description = "Include draft statuses", example = "true") @RequestParam(defaultValue = "false") boolean includeDrafts) {
		return toResponse(service.getCaseStatusesForParty(partyId, municipalityId, includeDrafts));
	}

	/**
	 * The body stays a plain list of cases; a partial result is signalled with a header instead, so that subscribers that
	 * ignore the header keep working exactly as before.
	 */
	private static ResponseEntity<List<CaseStatusResponse>> toResponse(final AggregatedCases aggregatedCases) {
		if (aggregatedCases.unavailableSources().isEmpty()) {
			return ok(aggregatedCases.cases());
		}
		return ok()
			.header(UNAVAILABLE_SOURCES_HEADER, String.join(",", aggregatedCases.unavailableSources()))
			.body(aggregatedCases.cases());
	}

	@Operation(summary = "Get errand statuses by errandNumber or propertyDesignation", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(path = "/errands/statuses", produces = APPLICATION_JSON_VALUE)
	ResponseEntity<List<CaseStatusResponse>> getErrandStatuses(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "propertyDesignation", description = "Property designation to find cases for", example = "Körsbärsdalen 123") @RequestParam(required = false) @Size(max = SEARCH_TERM_MAX_LENGTH) @Pattern(regexp = SEARCH_TERM_PATTERN,
			message = SEARCH_TERM_MESSAGE) final String propertyDesignation,
		@Parameter(name = "errandNumber", description = "Errand number to find cases for", example = "Number 123") @RequestParam(required = false) @Size(max = SEARCH_TERM_MAX_LENGTH) @Pattern(regexp = SEARCH_TERM_PATTERN,
			message = SEARCH_TERM_MESSAGE) final String errandNumber) {

		return ok(service.getErrandStatuses(municipalityId, propertyDesignation, errandNumber));
	}
}
