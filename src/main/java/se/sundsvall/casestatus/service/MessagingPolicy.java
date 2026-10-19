package se.sundsvall.casestatus.service;

import java.util.Optional;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;

@Component
public class MessagingPolicy {

	final AllowMessagesProperties allowMessagesProperties;

	public MessagingPolicy(AllowMessagesProperties allowMessagesProperties) {
		this.allowMessagesProperties = allowMessagesProperties;
	}

	public boolean allowMessages(final String system, final String familyId) {
		if (OPEN_E_PLATFORM.equals(system)) {
			return isSystemAllowed(system) && isEServiceAllowed(familyId);
		}
		return isSystemAllowed(system);
	}

	private boolean isSystemAllowed(final String system) {
		return findPermission(system)
			.map(AllowMessagesProperties.SystemMessagingPermission::allowed)
			.orElse(allowMessagesProperties.defaultAllowed());
	}

	private boolean isEServiceAllowed(final String familyId) {
		final var blockedFamilyIds = findPermission(OPEN_E_PLATFORM)
			.map(AllowMessagesProperties.SystemMessagingPermission::blockedFamilyIds)
			.orElse(emptySet());

		return ofNullable(familyId)
			.filter(not(blockedFamilyIds::contains))
			.isPresent();
	}

	private Optional<AllowMessagesProperties.SystemMessagingPermission> findPermission(String system) {
		return ofNullable(system).map(allowMessagesProperties.systems()::get);
	}

}
