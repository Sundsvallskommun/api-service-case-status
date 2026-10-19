package se.sundsvall.casestatus.service;

import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;

@ConfigurationProperties(prefix = "case-status.messaging")
public record AllowMessagesProperties(boolean defaultAllowed, Map<String, SystemMessagingPermission> systems) {

	public AllowMessagesProperties {
		systems = systems == null ? Map.of() : Map.copyOf(systems);
	}

	public record SystemMessagingPermission(boolean allowed, Set<String> blockedFamilyIds) {

		public SystemMessagingPermission {
			blockedFamilyIds = ofNullable(blockedFamilyIds).orElse(emptySet());
		}
	}
}
