package se.sundsvall.casestatus.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

public final class PaginationUtil {

	public static final int PAGE_SIZE = 100;

	private static final Logger LOGGER = LoggerFactory.getLogger(PaginationUtil.class);
	private static final int MAX_PAGES = 100;

	private PaginationUtil() {}

	/**
	 * Walks through every page of a paged search and returns the accumulated content.
	 * <p>
	 * Termination depends on paging metadata supplied by the remote service, so the walk is bounded by
	 * {@value #MAX_PAGES} pages and stops on a null or empty page. Without those guards a service that keeps reporting
	 * further pages would loop forever and grow the result list until the heap is exhausted.
	 * <p>
	 * No exceptions are caught here - the caller decides whether a failing page aborts the walk or truncates it. Return
	 * null from {@code pageFetcher} to stop early and keep whatever has been fetched so far.
	 *
	 * @param  pageFetcher fetches a single page by its zero based page number
	 * @param  description what is being fetched, used for the log message emitted when the page limit is reached
	 * @return             the content of every page that was read, never null
	 */
	public static <T> List<T> fetchAllPages(final IntFunction<Page<T>> pageFetcher, final String description) {
		final var allContent = new ArrayList<T>();

		for (int pageNumber = 0; pageNumber < MAX_PAGES; pageNumber++) {
			final var page = pageFetcher.apply(pageNumber);

			if ((page == null) || page.getContent().isEmpty()) {
				return allContent;
			}
			allContent.addAll(page.getContent());

			if (!page.hasNext()) {
				return allContent;
			}
		}

		LOGGER.warn("Reached the limit of {} pages when fetching {}, some entries may be missing", MAX_PAGES, description);
		return allContent;
	}
}
