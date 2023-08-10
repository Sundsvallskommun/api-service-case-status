package se.sundsvall.casestatus.api;

import static org.springframework.http.ResponseEntity.ok;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import se.sundsvall.casestatus.api.domain.CasePdfResponse;
import se.sundsvall.casestatus.api.domain.CaseStatusResponse;
import se.sundsvall.casestatus.api.domain.OepStatusResponse;
import se.sundsvall.casestatus.service.CaseStatusService;

@RestController
@Validated
@Tag(name = "Status Resources")
class CaseStatusResource {

	private final CaseStatusService service;

	CaseStatusResource(final CaseStatusService service) {
		this.service = service;
	}

	@Operation(summary = "Get OEP status")
	@ApiResponse(
		responseCode = "200",
		description = "Successful Operation",
		content = @Content(schema = @Schema(implementation = OepStatusResponse.class)))
	@ApiResponse(
		responseCode = "400",
		description = "Bad Request",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "500",
		description = "Internal Server Error",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping("/{externalCaseId}/oepstatus")
	ResponseEntity<OepStatusResponse> getOepStatus(@PathVariable("externalCaseId") final String externalCaseId) {
		final var response = service.getOepStatus(externalCaseId);

		return ok(response);
	}

	@Operation(summary = "Get case status")
	@ApiResponse(
		responseCode = "200",
		description = "Successful Operation",
		content = @Content(schema = @Schema(implementation = CaseStatusResponse.class)))
	@ApiResponse(
		responseCode = "400",
		description = "Bad Request",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "500",
		description = "Internal Server Error",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping("/{externalCaseId}/status")
	ResponseEntity<CaseStatusResponse> getCaseStatus(@PathVariable("externalCaseId") final String externalCaseId) {
		final var caseStatus = service.getCaseStatus(externalCaseId);

		return ok(caseStatus);
	}

	@Operation(summary = "Get case PDF")
	@ApiResponse(
		responseCode = "200",
		description = "Successful Operation",
		content = @Content(schema = @Schema(implementation = CasePdfResponse.class)))
	@ApiResponse(
		responseCode = "400",
		description = "Bad Request",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "500",
		description = "Internal Server Error",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping("/{externalCaseId}/pdf")
	ResponseEntity<CasePdfResponse> getCasePdf(@PathVariable("externalCaseId") final String externalCaseId) {
		final var response = service.getCasePdf(externalCaseId);

		return ok(response);
	}

	@Operation(summary = "Get organization statuses")
	@ApiResponse(
		responseCode = "200",
		description = "Successful Operation",
		content = @Content(array = @ArraySchema(schema = @Schema(implementation = CaseStatusResponse.class))))
	@ApiResponse(
		responseCode = "400",
		description = "Bad Request",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "404",
		description = "Not Found",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@ApiResponse(
		responseCode = "500",
		description = "Internal Server Error",
		content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping("/{organizationNumber}/statuses")
	ResponseEntity<List<CaseStatusResponse>> getOrganisationStatuses(@PathVariable("organizationNumber") final String organizationNumber) {
		final var result = service.getCaseStatuses(organizationNumber);

		if (result.isEmpty()) {
			throw Problem.valueOf(Status.NOT_FOUND);
		}

		return ok(result);
	}
}
