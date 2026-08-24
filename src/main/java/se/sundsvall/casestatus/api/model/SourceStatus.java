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
@Schema(description = """
	The fetch status for one case source. When a source is UNAVAILABLE its cases are missing from the response - \
	consumers must not present a degraded response as the complete set of the citizen's cases.""")
public class SourceStatus {

	public static final String STATUS_OK = "OK";
	public static final String STATUS_UNAVAILABLE = "UNAVAILABLE";

	@Schema(description = "The source system", examples = "CASE_MANAGEMENT", allowableValues = {
		"CASE_MANAGEMENT", "OPEN_E_PLATFORM", "SUPPORT_MANAGEMENT"
	})
	private String source;

	@Schema(description = "The fetch status of the source", examples = "OK", allowableValues = {
		"OK", "UNAVAILABLE"
	})
	private String status;
}
