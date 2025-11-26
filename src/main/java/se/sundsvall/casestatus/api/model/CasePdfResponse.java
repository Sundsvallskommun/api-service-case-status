package se.sundsvall.casestatus.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "Case status response")
public class CasePdfResponse {

	@Schema(description = "External case id", examples = "1234567890")
	private String externalCaseId;

	@Schema(description = "Base64 encoded PDF", examples = "JVBERi0x")
	private String base64;
}
