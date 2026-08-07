package se.sundsvall.casestatus.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.util.PaginationUtil.fetchAllPages;

class PaginationUtilTest {

	private static final String DESCRIPTION = "things";

	@Test
	void readsEveryPage() {
		final var firstPage = new PageImpl<>(List.of("a"), PageRequest.of(0, 1), 2);
		final var secondPage = new PageImpl<>(List.of("b"), PageRequest.of(1, 1), 2);

		final var result = fetchAllPages(pageNumber -> pageNumber == 0 ? firstPage : secondPage, DESCRIPTION);

		assertThat(result).containsExactly("a", "b");
	}

	@Test
	void stopsWhenThereIsNoNextPage() {
		final var calls = new AtomicInteger();

		final var result = fetchAllPages(pageNumber -> {
			calls.incrementAndGet();
			return new PageImpl<>(List.of("only"));
		}, DESCRIPTION);

		assertThat(result).containsExactly("only");
		assertThat(calls).hasValue(1);
	}

	@Test
	void stopsOnNullPage() {
		final var calls = new AtomicInteger();

		final var result = fetchAllPages(pageNumber -> {
			calls.incrementAndGet();
			return null;
		}, DESCRIPTION);

		assertThat(result).isEmpty();
		assertThat(calls).hasValue(1);
	}

	@Test
	void stopsOnEmptyPageEvenWhenMorePagesAreReported() {
		final var calls = new AtomicInteger();

		final var result = fetchAllPages(pageNumber -> {
			calls.incrementAndGet();
			return new PageImpl<>(List.<String>of(), PageRequest.of(0, 1), 10);
		}, DESCRIPTION);

		assertThat(result).isEmpty();
		assertThat(calls).hasValue(1);
	}

	@Test
	void keepsPagesReadBeforeTheFetcherGivesUp() {
		// Returning null is how a caller signals a failed page without discarding what was already read
		final var result = fetchAllPages(pageNumber -> pageNumber == 0
			? new PageImpl<>(List.of("a"), PageRequest.of(0, 1), 5)
			: null, DESCRIPTION);

		assertThat(result).containsExactly("a");
	}

	@Test
	void stopsAtThePageLimitWhenTheRemoteAlwaysReportsMorePages() {
		// Paging metadata comes from the remote service - a page always claiming a successor must not loop forever
		final var calls = new AtomicInteger();

		final Page<String> runawayPage = new PageImpl<>(List.of("x"), PageRequest.of(0, 1), 1000);
		final var result = fetchAllPages(pageNumber -> {
			calls.incrementAndGet();
			return runawayPage;
		}, DESCRIPTION);

		assertThat(calls).hasValue(100);
		assertThat(result).hasSize(100);
	}
}
