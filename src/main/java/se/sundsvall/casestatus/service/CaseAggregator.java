package se.sundsvall.casestatus.service;

import feign.RetryableException;
import generated.client.oep_integrator.CaseEnvelope;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.supportmanagement.Errand;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.casedata.CaseDataIntegration;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.OpenEMapper;
import se.sundsvall.casestatus.service.mapper.SupportManagementMapper;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.Constants.SOURCE_CASE_DATA;
import static se.sundsvall.casestatus.util.Constants.SOURCE_CASE_MANAGEMENT;
import static se.sundsvall.casestatus.util.Constants.SOURCE_OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.Constants.SOURCE_SUPPORT_MANAGEMENT;

/**
 * Aggregates {@link CaseStatusResponse} entries across all backing systems (CaseManagement, OeP, SupportManagement and
 * the local Open-E cache) for a given party or organization. The same pipeline — parallel fetch, multi-sign override,
 * OPEN_E_PLATFORM duplicate filter and optional draft filter — is applied to both flows. The party flow additionally
 * resolves the Open-E entries that remain to the case they were handed over to, see
 * {@link #resolveHandedOverOpenECases}.
 *
 * <p>
 * A source that does not answer degrades to an empty contribution rather than failing the whole request, and the
 * outcome per source is reported back to the caller in {@link CaseStatusesResponse#getSources()} so a partial result is
 * never mistaken for a complete one.
 * </p>
 */
@Component
public class CaseAggregator {

	private static final Logger LOG = LoggerFactory.getLogger(CaseAggregator.class);

	private static final Set<String> DRAFT_STATUSES = Set.of("utkast");

	// The shared task executor has a bounded queue (spring.task.execution), and the party flow already takes four of its
	// tasks
	private static final int HAND_OVER_LOOKUP_LANES = 3;

	// A CaseData errand id; at most 18 digits so that it always fits a long
	private static final Pattern ERRAND_ID_PATTERN = Pattern.compile("\\d{1,18}");

	private final PartyIntegration partyIntegration;
	private final CaseManagementIntegration caseManagementIntegration;
	private final CaseManagementMapper caseManagementMapper;
	private final CaseDataIntegration caseDataIntegration;
	private final OepIntegratorClient oepIntegratorClient;
	private final OpenEMapper openEMapper;
	private final CaseRepository caseRepository;
	private final SupportManagementService supportManagementService;
	private final SupportManagementMapper supportManagementMapper;
	private final StatusVocabulary statusVocabulary;
	private final Executor taskExecutor;

	public CaseAggregator(final PartyIntegration partyIntegration,
		final CaseManagementIntegration caseManagementIntegration,
		final CaseManagementMapper caseManagementMapper,
		final CaseDataIntegration caseDataIntegration,
		final OepIntegratorClient oepIntegratorClient,
		final OpenEMapper openEMapper,
		final CaseRepository caseRepository,
		final SupportManagementService supportManagementService,
		final SupportManagementMapper supportManagementMapper,
		final StatusVocabulary statusVocabulary,
		final @Qualifier(APPLICATION_TASK_EXECUTOR_BEAN_NAME) Executor taskExecutor) {
		this.partyIntegration = partyIntegration;
		this.caseManagementIntegration = caseManagementIntegration;
		this.caseManagementMapper = caseManagementMapper;
		this.caseDataIntegration = caseDataIntegration;
		this.oepIntegratorClient = oepIntegratorClient;
		this.openEMapper = openEMapper;
		this.caseRepository = caseRepository;
		this.supportManagementService = supportManagementService;
		this.supportManagementMapper = supportManagementMapper;
		this.statusVocabulary = statusVocabulary;
		this.taskExecutor = taskExecutor;
	}

	public AggregatedCases aggregateForParty(final String partyId, final String municipalityId, final boolean includeDrafts) {
		final var cmFuture = caseManagementByPartyAsync(partyId, municipalityId);
		final var oepFuture = oepByPartyAsync(partyId, municipalityId, includeDrafts);
		final var multisignFuture = oepMultisignByPartyAsync(partyId, municipalityId);

		// SupportManagement stores partyId in stakeholders.externalId for both private persons and enterprises
		final var supportFuture = supportManagementByExternalIdAsync(partyId, municipalityId);

		final var multisignResult = multisignFuture.join();
		final var caseManagementResult = cmFuture.join();
		final var primaryResults = Stream.of(cmFuture, oepFuture, supportFuture).map(CompletableFuture::join).toList();

		final var resolutions = resolveHandedOverOpenECases(filterPrimary(primaryResults, multisignResult.responses(), includeDrafts), partyId, municipalityId, caseManagementResult.ok());
		final var lookupResults = resolutions.stream()
			.map(Resolution::unavailableSource)
			.filter(Objects::nonNull)
			.distinct()
			.map(source -> new SourceResult(source, emptyList(), false))
			.toList();

		return toResult(
			Stream.concat(withoutAlreadyListedCases(resolutions).stream(), multisignResult.responses().stream()).toList(),
			Stream.of(primaryResults, List.of(multisignResult), lookupResults).flatMap(List::stream).toList());
	}

	public AggregatedCases aggregateForOrg(final String organizationNumber, final String municipalityId) {
		final var cmFuture = caseManagementByOrgAsync(organizationNumber, municipalityId);
		final var localOpenEFuture = localOpenEByOrgAsync(organizationNumber, municipalityId);
		final var supportFuture = supportManagementByOrganizationNumberAsync(organizationNumber, municipalityId);

		final var results = Stream.of(cmFuture, localOpenEFuture, supportFuture).map(CompletableFuture::join).toList();

		// Org flow has no multi-sign source and preserves the historical "no draft filtering" behavior.
		return toResult(runPipeline(results, emptyList(), true), results);
	}

	/**
	 * Runs one source on the shared task executor. A source that does not answer contributes an empty list and is
	 * reported as UNAVAILABLE, so a single unreachable backing system produces a partial result instead of failing the
	 * whole aggregate.
	 */
	private CompletableFuture<SourceResult> sourceAsync(final String source, final Supplier<List<CaseStatusResponse>> supplier) {
		return submit(() -> {
			try {
				return new SourceResult(source, supplier.get(), true);
			} catch (final RuntimeException e) {
				if (!isSourceUnavailable(e)) {
					throw e;
				}
				LOG.warn("Case status source {} is unavailable and is excluded from the aggregated result", source, e);
				return new SourceResult(source, emptyList(), false);
			}
		}, () -> new SourceResult(source, emptyList(), false));
	}

	/**
	 * Runs a task on the shared task executor. Its queue is bounded, so a burst of requests can make it reject a task;
	 * that degrades to {@code onRejected} like any other unavailable source instead of failing the request.
	 */
	private <T> CompletableFuture<T> submit(final Supplier<T> task, final Supplier<T> onRejected) {
		try {
			return CompletableFuture.supplyAsync(task, taskExecutor);
		} catch (final RejectedExecutionException e) {
			LOG.warn("The task executor is saturated, a case status lookup is skipped and reported as unavailable", e);
			return CompletableFuture.completedFuture(onRejected.get());
		}
	}

	/**
	 * Only a failure of the call itself degrades a source: any problem response from the source, an open circuit breaker,
	 * a connect/read failure, or the local cache being unreachable. Anything else — a NullPointerException, a mapper bug,
	 * any defect of ours — propagates and fails the request rather than being reported as somebody else's outage.
	 *
	 * <p>
	 * A 4xx is included here even though it means we sent a request the source rejected. That is our defect, but this is
	 * a citizen-facing endpoint: turning it into a 500 would deny someone their case list over a bug that costs them
	 * nothing to route around. It is logged at WARN and named in the unavailable-sources header instead.
	 * </p>
	 */
	private static boolean isSourceUnavailable(final RuntimeException e) {
		return e instanceof ThrowableProblem
			|| e instanceof CallNotPermittedException
			|| e instanceof RetryableException
			|| e instanceof DataAccessException;
	}

	/**
	 * A source counts as unavailable when any fetch made against it failed — Open-E is read more than once per request,
	 * and a partial Open-E answer is still an incomplete one.
	 */
	private static AggregatedCases toResult(final List<CaseStatusResponse> cases, final List<SourceResult> results) {
		final var unavailableSources = results.stream()
			.collect(groupingBy(SourceResult::source, LinkedHashMap::new, toList()))
			.entrySet().stream()
			.filter(entry -> !entry.getValue().stream().allMatch(SourceResult::ok))
			.map(Map.Entry::getKey)
			.toList();

		return new AggregatedCases(cases, unavailableSources);
	}

	/**
	 * Runs {@link #filterPrimary} and appends the multi-sign entries last so they bypass both of its filters.
	 */
	private List<CaseStatusResponse> runPipeline(final List<SourceResult> primarySources, final List<CaseStatusResponse> multisignStatuses, final boolean includeDrafts) {
		return Stream.concat(filterPrimary(primarySources, multisignStatuses, includeDrafts).stream(), multisignStatuses.stream())
			.toList();
	}

	/**
	 * Joins all primary source results, removes any regular entry that is shadowed by a multi-sign entry for the same
	 * flowInstanceId, and applies the OPEN_E_PLATFORM duplicate + draft filter.
	 */
	private List<CaseStatusResponse> filterPrimary(final List<SourceResult> primarySources, final List<CaseStatusResponse> multisignStatuses, final boolean includeDrafts) {

		final var multisignIds = multisignStatuses.stream()
			.map(CaseStatusResponse::getExternalCaseId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		final var primaryStatuses = primarySources.stream()
			.map(SourceResult::responses)
			.flatMap(List::stream)
			.filter(response -> isNotOverriddenByMultisign(response, multisignIds))
			.toList();

		return filterResponses(primaryStatuses, includeDrafts);
	}

	/**
	 * Replaces every Open-E entry that no other source accounted for with the case it was handed over to, if any.
	 *
	 * <p>
	 * Open-E lists the flow instances the party submitted, while CaseManagement lists the cases where the party is a
	 * stakeholder with a role its search covers. The two disagree whenever the submitter is not that stakeholder — an
	 * agent (ombud) applying on someone else's behalf, or a role the target system search does not include — and the
	 * party then only sees the raw Open-E entry: the flow instance id instead of the target system's case number, and
	 * Open-E messaging that no case worker reads. Looking each remaining entry up by externalCaseId is what
	 * {@code GET /{externalCaseId}/status} does.
	 * </p>
	 *
	 * <p>
	 * Having submitted a case is not the same as being a party to it, and consumers treat this list as the cases the party
	 * may open — Mina sidor reads and writes a CaseData errand's conversations for any CaseData case in it. So the handed
	 * over case only replaces the entry when it may be shown to the party, see {@link #resolveHandedOverCase}; otherwise
	 * the entry is kept as the Open-E entry it was.
	 * </p>
	 *
	 * <p>
	 * There is one lookup per remaining Open-E entry; an entry that was never handed over answers 404 from the case mapping
	 * alone. They are spread over at most {@value #HAND_OVER_LOOKUP_LANES} lanes run on the shared task executor, each lane
	 * looking its entries up one after another, so a party with many entries cannot flood the executor's bounded queue or
	 * CaseManagement. Nothing is looked up when the CaseManagement party search already failed in this request, and once
	 * a lookup fails the rest are skipped — otherwise a hanging CaseManagement would cost one read timeout per entry. A
	 * skipped entry is kept as the Open-E entry it is, and CaseManagement is reported as unavailable.
	 * </p>
	 *
	 * <p>
	 * Multi-sign entries are not looked up: they await signatures and have not been submitted yet. Only the party flow
	 * does this — the organization flow reads Open-E from the local cache, which can hold many entries per organization,
	 * and one CaseManagement call per entry is not affordable there.
	 * </p>
	 */
	private List<Resolution> resolveHandedOverOpenECases(final List<CaseStatusResponse> responses, final String partyId, final String municipalityId, final boolean caseManagementAvailable) {
		final var openECases = responses.stream()
			.filter(CaseAggregator::isOpenECase)
			.toList();

		final var resolved = new IdentityHashMap<CaseStatusResponse, Resolution>();
		if (caseManagementAvailable) {
			final var lookupFailure = new AtomicReference<String>();
			// Collected before joining so that every lane is started before the first one is waited on
			final var lanes = IntStream.range(0, Math.min(HAND_OVER_LOOKUP_LANES, openECases.size()))
				// A rejected lane resolves as if a lookup had already failed: its entries are kept and reported, not looked up
				.mapToObj(lane -> submit(() -> resolveLane(openECases, lane, partyId, municipalityId, lookupFailure),
					() -> resolveLane(openECases, lane, partyId, municipalityId, new AtomicReference<>(SOURCE_CASE_MANAGEMENT))))
				.toList();
			lanes.stream()
				.map(CompletableFuture::join)
				.forEach(resolved::putAll);
		}

		return responses.stream()
			.map(response -> ofNullable(resolved.get(response)).orElseGet(() -> Resolution.unchanged(response)))
			.toList();
	}

	private static boolean isOpenECase(final CaseStatusResponse response) {
		return OPEN_E_PLATFORM.equals(response.getSystem()) && response.getExternalCaseId() != null;
	}

	/**
	 * Looks up every {@value #HAND_OVER_LOOKUP_LANES}th entry starting at {@code lane}, keyed by the entry itself.
	 */
	private Map<CaseStatusResponse, Resolution> resolveLane(final List<CaseStatusResponse> openECases, final int lane, final String partyId, final String municipalityId,
		final AtomicReference<String> lookupFailure) {
		final var resolved = new IdentityHashMap<CaseStatusResponse, Resolution>();
		for (var index = lane; index < openECases.size(); index += HAND_OVER_LOOKUP_LANES) {
			final var openECase = openECases.get(index);
			final var failedSource = lookupFailure.get();
			if (failedSource != null) {
				resolved.put(openECase, Resolution.failed(openECase, failedSource));
			} else {
				resolved.put(openECase, resolve(openECase, partyId, municipalityId, lookupFailure));
			}
		}
		return resolved;
	}

	/**
	 * A source that does not answer keeps the Open-E entry and is reported as unavailable, the same way a failing source
	 * is — see {@link #isSourceUnavailable}.
	 */
	private Resolution resolve(final CaseStatusResponse openECase, final String partyId, final String municipalityId, final AtomicReference<String> lookupFailure) {
		try {
			return caseManagementIntegration.findCaseStatusForExternalId(openECase.getExternalCaseId(), municipalityId)
				.map(handedOverCase -> resolveHandedOverCase(openECase, handedOverCase, partyId, municipalityId, lookupFailure))
				.orElseGet(() -> Resolution.unchanged(openECase));
		} catch (final RuntimeException e) {
			return unavailable(openECase, SOURCE_CASE_MANAGEMENT, e, lookupFailure);
		}
	}

	/**
	 * Decides whether the case an Open-E entry was handed over to may be shown to the party in its place.
	 * <ul>
	 * <li>ByggR: yes. The case shows a diary number and a status, and its messages are still read from the Open-E flow
	 * instance the party submitted.</li>
	 * <li>CaseData: only when the party is a stakeholder of the errand, in any role — the agent who submitted on someone's
	 * behalf is one, a submitter the errand does not know is not. Checked by personId, so an enterprise party keeps its
	 * Open-E entry.</li>
	 * <li>Anything else, e.g. Ecos: no. Case-status cannot check who its parties are.</li>
	 * </ul>
	 */
	private Resolution resolveHandedOverCase(final CaseStatusResponse openECase, final CaseStatusDTO handedOverCase, final String partyId, final String municipalityId,
		final AtomicReference<String> lookupFailure) {
		return switch (handedOverCase.getSystem()) {
			case BYGGR -> Resolution.handedOver(caseManagementMapper.toCaseStatusResponse(handedOverCase, municipalityId));
			case CASE_DATA -> resolveCaseDataCase(openECase, handedOverCase, partyId, municipalityId, lookupFailure);
			case null, default -> Resolution.unchanged(openECase);
		};
	}

	/**
	 * Without an errand id and a namespace to check against — a CaseManagement that predates returning the namespace for a
	 * lookup by externalCaseId leaves it out — the party cannot be checked, so the entry is kept.
	 */
	private Resolution resolveCaseDataCase(final CaseStatusResponse openECase, final CaseStatusDTO handedOverCase, final String partyId, final String municipalityId,
		final AtomicReference<String> lookupFailure) {
		final var errandId = ofNullable(handedOverCase.getCaseId()).filter(ERRAND_ID_PATTERN.asMatchPredicate()).map(Long::valueOf);
		if (errandId.isEmpty() || handedOverCase.getNamespace() == null) {
			return Resolution.unchanged(openECase);
		}
		try {
			if (caseDataIntegration.isStakeholder(municipalityId, handedOverCase.getNamespace(), errandId.get(), partyId)) {
				return Resolution.handedOver(caseManagementMapper.toCaseStatusResponse(handedOverCase, municipalityId));
			}
			return Resolution.unchanged(openECase);
		} catch (final RuntimeException e) {
			return unavailable(openECase, SOURCE_CASE_DATA, e, lookupFailure);
		}
	}

	private static Resolution unavailable(final CaseStatusResponse openECase, final String source, final RuntimeException e, final AtomicReference<String> lookupFailure) {
		if (!isSourceUnavailable(e)) {
			throw e;
		}
		lookupFailure.compareAndSet(null, source);
		LOG.warn("Open-E case {} could not be resolved, {} is unavailable, and is kept as an Open-E case", openECase.getExternalCaseId(), source, e);
		return Resolution.failed(openECase, source);
	}

	/**
	 * Drops a handed-over case that is already listed. Several flow instances can be handed over to the same case — a
	 * ByggR case receives later submissions under the same diary number — and the case may already be in the list from
	 * the party search under another flow instance id. It is shown once.
	 */
	private static List<CaseStatusResponse> withoutAlreadyListedCases(final List<Resolution> resolutions) {
		final var listedCases = resolutions.stream()
			.filter(resolution -> !resolution.handedOver())
			.map(resolution -> caseKey(resolution.response()))
			.collect(Collectors.toCollection(HashSet::new));

		final var cases = new ArrayList<CaseStatusResponse>();
		for (final var resolution : resolutions) {
			if (!resolution.handedOver() || listedCases.add(caseKey(resolution.response()))) {
				cases.add(resolution.response());
			}
		}
		return cases;
	}

	private static String caseKey(final CaseStatusResponse response) {
		return response.getSystem() + ":" + response.getCaseId();
	}

	/**
	 * Returns true when a regular response should be kept, i.e. it is not shadowed by a multi-sign entry for the same
	 * flow instance. Multi-sign cases take precedence because they represent an actionable state (awaiting signature) the
	 * consumer must surface. Responses without an externalCaseId cannot be matched as duplicates and are always kept.
	 */
	private boolean isNotOverriddenByMultisign(final CaseStatusResponse response, final Set<String> multisignIds) {
		if (response.getExternalCaseId() == null) {
			return true;
		}
		return !multisignIds.contains(response.getExternalCaseId());
	}

	/**
	 * Removes drafts (when {@code includeDrafts} is false) and removes the OPEN_E_PLATFORM copy when another system
	 * returned the same externalCaseId. Responses with a null externalCaseId are never treated as duplicates.
	 */
	List<CaseStatusResponse> filterResponses(final List<CaseStatusResponse> responses, final boolean includeDrafts) {
		if (responses == null) {
			return emptyList();
		}

		final var filterDrafts = draftFilter(includeDrafts);

		final var nullExternalCaseIdStream = responses.stream()
			.filter(response -> response.getExternalCaseId() == null)
			.filter(filterDrafts);

		final var filteredStream = responses.stream()
			.filter(response -> response.getExternalCaseId() != null)
			.filter(filterDrafts)
			.collect(groupingBy(CaseStatusResponse::getExternalCaseId))
			.entrySet().stream()
			.flatMap(entry -> dropOpenEDuplicates(entry.getValue()));

		return Stream.concat(nullExternalCaseIdStream, filteredStream)
			.toList();
	}

	private Stream<CaseStatusResponse> dropOpenEDuplicates(final List<CaseStatusResponse> entries) {
		if (entries.size() > 1 && entries.stream().anyMatch(response -> OPEN_E_PLATFORM.equals(response.getSystem()))) {
			return entries.stream().filter(response -> !OPEN_E_PLATFORM.equals(response.getSystem()));
		}
		return entries.stream();
	}

	private Predicate<CaseStatusResponse> draftFilter(final boolean includeDrafts) {
		return response -> includeDrafts || !DRAFT_STATUSES.contains(ofNullable(response.getStatus()).orElse("").toLowerCase());
	}

	private CompletableFuture<SourceResult> caseManagementByPartyAsync(final String partyId, final String municipalityId) {
		return sourceAsync(SOURCE_CASE_MANAGEMENT, () -> caseManagementIntegration.getCaseStatusForPartyId(partyId, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.toList());
	}

	private CompletableFuture<SourceResult> caseManagementByOrgAsync(final String organizationNumber, final String municipalityId) {
		return sourceAsync(SOURCE_CASE_MANAGEMENT, () -> caseManagementIntegration.getCaseStatusForOrganizationNumber(organizationNumber, municipalityId).stream()
			.map(dto -> caseManagementMapper.toCaseStatusResponse(dto, municipalityId))
			.toList());
	}

	/**
	 * Fetches this party's Open-E cases: submitted cases always, and cases that are saved but not yet submitted only when
	 * {@code includeDrafts} is true. Unsubmitted cases are drafts by definition, so they are gated at the call site
	 * rather than by the status-name based draft filter in {@link #filterResponses} — that saves a round-trip on the
	 * default path and keeps the outcome independent of how the Open-E flow happens to name its draft status. When both
	 * reads are made they run sequentially in a single task rather than as separate parallel calls, keeping the number of
	 * concurrent first-use decodes on the OeP client bounded (the Feign message-converter setup is only reliably
	 * initialized after the first decode). includeStatus is requested so the mapper can populate a status (envelopes
	 * without a status are dropped).
	 */
	private CompletableFuture<SourceResult> oepByPartyAsync(final String partyId, final String municipalityId, final boolean includeDrafts) {
		// The OeP client dismisses 404 responses, which yields a null list
		return sourceAsync(SOURCE_OPEN_E_PLATFORM, () -> Stream.concat(
			ofNullable(oepIntegratorClient.getCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId, true)).orElse(emptyList()).stream(),
			unsubmittedCasesByPartyId(partyId, municipalityId, includeDrafts).stream())
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList());
	}

	private List<CaseEnvelope> unsubmittedCasesByPartyId(final String partyId, final String municipalityId, final boolean includeDrafts) {
		if (!includeDrafts) {
			return emptyList();
		}
		return ofNullable(oepIntegratorClient.getUnsubmittedCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId, true)).orElse(emptyList());
	}

	private CompletableFuture<SourceResult> oepMultisignByPartyAsync(final String partyId, final String municipalityId) {
		return sourceAsync(SOURCE_OPEN_E_PLATFORM, () -> ofNullable(oepIntegratorClient.getMultisignCasesByPartyId(municipalityId, InstanceType.EXTERNAL, partyId, true)).orElse(emptyList()).stream()
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList());
	}

	private CompletableFuture<SourceResult> localOpenEByOrgAsync(final String organizationNumber, final String municipalityId) {
		return sourceAsync(SOURCE_OPEN_E_PLATFORM, () -> caseRepository.findByOrganisationNumberAndMunicipalityId(organizationNumber, municipalityId).stream()
			.map(openEMapper::toCaseStatusResponse)
			.filter(Objects::nonNull)
			.toList());
	}

	private CompletableFuture<SourceResult> supportManagementByExternalIdAsync(final String partyId, final String municipalityId) {
		return sourceAsync(SOURCE_SUPPORT_MANAGEMENT,
			() -> mapSupportManagementErrands(municipalityId, supportManagementService.getSupportManagementCasesByExternalId(municipalityId, partyId)));
	}

	/**
	 * SupportManagement stores partyId (not organization number) in stakeholders.externalId, so the organization number
	 * must be translated via Party before searching. An organization number unknown to Party yields no SupportManagement
	 * matches while the other sources still contribute. A Party lookup that fails outright makes the SupportManagement
	 * contribution unobtainable, so it is reported as an unavailable SupportManagement source.
	 */
	private CompletableFuture<SourceResult> supportManagementByOrganizationNumberAsync(final String organizationNumber, final String municipalityId) {
		return sourceAsync(SOURCE_SUPPORT_MANAGEMENT, () -> partyIntegration.getPartyIdByOrganizationNumber(municipalityId, organizationNumber)
			.map(partyId -> mapSupportManagementErrands(municipalityId, supportManagementService.getSupportManagementCasesByExternalId(municipalityId, partyId)))
			.orElse(emptyList()));
	}

	public List<CaseStatusResponse> mapSupportManagementErrands(final String municipalityId, final Map<String, List<Errand>> errandsByNamespace) {
		return errandsByNamespace.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream()
				.map(errand -> supportManagementMapper.toCaseStatusResponse(
					errand,
					entry.getKey(),
					statusVocabulary.lookupBySupportManagementStatus(errand.getStatus()),
					supportManagementService.getClassificationDisplayName(municipalityId, entry.getKey(), errand))))
			.toList();
	}

	private record SourceResult(String source, List<CaseStatusResponse> responses, boolean ok) {
	}

	/**
	 * One entry after the hand-over lookup: the entry itself or the case it was handed over to, and the source that kept
	 * the lookup from completing, if any.
	 */
	private record Resolution(CaseStatusResponse response, boolean handedOver, String unavailableSource) {

		static Resolution unchanged(final CaseStatusResponse response) {
			return new Resolution(response, false, null);
		}

		static Resolution handedOver(final CaseStatusResponse response) {
			return new Resolution(response, true, null);
		}

		static Resolution failed(final CaseStatusResponse response, final String unavailableSource) {
			return new Resolution(response, false, unavailableSource);
		}
	}
}
