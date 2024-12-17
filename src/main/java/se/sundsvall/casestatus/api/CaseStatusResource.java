package se.sundsvall.casestatus.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.service.CaseStatusService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@RestController
@Validated
@RequestMapping("/{municipalityId}")
@Tag(name = "Status Resources")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CaseStatusResource {

	private final CaseStatusService service;

	CaseStatusResource(final CaseStatusService service) {
		this.service = service;
	}

	@Operation(summary = "Get status in openE Platform format", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))

	})
	@GetMapping("/{externalCaseId}/oepstatus")
	ResponseEntity<OepStatusResponse> getOepStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("externalCaseId") final String externalCaseId) {
		return ok(service.getOepStatus(externalCaseId, municipalityId));
	}

	@Operation(summary = "Get case status", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(path = "/{externalCaseId}/status", produces = {
		APPLICATION_JSON_VALUE
	})
	ResponseEntity<CaseStatusResponse> getCaseStatus(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("externalCaseId") final String externalCaseId) {
		return ok(service.getCaseStatus(externalCaseId, municipalityId));
	}

	@Operation(summary = "Get case PDF", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	})
	@GetMapping(path = "/{externalCaseId}/pdf", produces = {
		APPLICATION_JSON_VALUE
	})
	ResponseEntity<CasePdfResponse> getCasePdf(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("externalCaseId") final String externalCaseId) {
		return ok(service.getCasePdf(externalCaseId));
	}

	@Operation(summary = "Get organization statuses", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(path = "/{organizationNumber}/statuses", produces = {
		APPLICATION_JSON_VALUE
	})
	ResponseEntity<List<CaseStatusResponse>> getOrganisationStatuses(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable("organizationNumber") final String organizationNumber) {
		return ok(service.getCaseStatuses(organizationNumber, municipalityId));
	}

	@Operation(summary = "Get all statuses connected to a partyId", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	})
	@GetMapping(path = "/party/{partyId}/statuses", produces = {
		APPLICATION_JSON_VALUE
	})
	ResponseEntity<List<CaseStatusResponse>> getPartyStatuses(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "partyId", description = "PartyId to find cases for", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable("partyId") @ValidUuid final String partyId) {
		return ok(service.getCaseStatuses(partyId, municipalityId));
	}

}
