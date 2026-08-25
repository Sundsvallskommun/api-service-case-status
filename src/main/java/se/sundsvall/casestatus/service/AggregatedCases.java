package se.sundsvall.casestatus.service;

import java.util.List;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;

/**
 * The outcome of one aggregation: the cases from every source that answered, plus the sources that did not.
 *
 * <p>
 * Deliberately not an API model. The response body stays a plain list of cases — case-status has too many subscribers
 * to change its shape right now — so the incomplete-result signal reaches the caller as a response header instead. This
 * record is what carries the information from {@link CaseAggregator} to the resource layer, and is what a future
 * response body would be built from if the shape is ever versioned.
 * </p>
 *
 * @param cases              the merged cases from the sources that answered
 * @param unavailableSources the sources that did not answer, and whose cases are therefore missing from {@code cases}
 */
public record AggregatedCases(List<CaseStatusResponse> cases, List<String> unavailableSources) {
}
