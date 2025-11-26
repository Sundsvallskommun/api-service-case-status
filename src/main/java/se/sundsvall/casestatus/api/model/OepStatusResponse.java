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
public class OepStatusResponse {

	@Schema(description = "Key", examples = "status")
	private String key;

	@Schema(description = "Value", examples = "In progress")
	private String value;
}
