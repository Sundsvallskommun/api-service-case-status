package se.sundsvall.casestatus.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "Case status response")
public class CaseStatusResponse {

	@Schema(description = "Case id", example = "1234567890")
	private String caseId;

	@Schema(description = "External case id", example = "1234567890")
	private String externalCaseId;

	@Schema(description = "Case type", example = "Building permit")
	private String caseType;

	@Schema(description = "Status", example = "In progress")
	private String status;

	@Schema(description = "First submitted", example = "2021-01-01")
	private String firstSubmitted;

	@Schema(description = "Last status change", example = "2021-01-01")
	private String lastStatusChange;

	@Schema(description = "The system that the case is in", example = "BYGGR")
	private String system;

	@Schema(description = "The namespace of the case", example = "Namespace")
	private String namespace;

	@Schema(description = "Human readable identifier for the case", example = "BYGGR-2024-123456")
	private String errandNumber;

}
