package se.sundsvall.casestatus.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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
	The merged cases from all sources, with the fetch status per source. A source that is UNAVAILABLE contributed \
	nothing to 'cases', so the list is incomplete whenever any source is not OK.""")
public class CaseStatusesResponse {

	@ArraySchema(schema = @Schema(description = "The merged cases from the sources that answered"))
	private List<CaseStatusResponse> cases;

	@ArraySchema(schema = @Schema(description = "The fetch status per case source"))
	private List<SourceStatus> sources;
}
