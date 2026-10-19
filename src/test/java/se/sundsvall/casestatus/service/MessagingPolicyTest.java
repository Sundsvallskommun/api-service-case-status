package se.sundsvall.casestatus.service;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.Constants.SUPPORT_MANAGEMENT_SYSTEM;

class MessagingPolicyTest {

	private final MessagingPolicy messagingPolicy = new MessagingPolicy(new AllowMessagesProperties(false, Map.of(
		"BYGGR", new AllowMessagesProperties.SystemMessagingPermission(false, null),
		"CASE_DATA", new AllowMessagesProperties.SystemMessagingPermission(true, null),
		SUPPORT_MANAGEMENT_SYSTEM, new AllowMessagesProperties.SystemMessagingPermission(true, null),
		OPEN_E_PLATFORM, new AllowMessagesProperties.SystemMessagingPermission(true, Set.of("999")))));

	@Test
	void allowMessages_systemAllowed_returnsTrue() {
		assertThat(messagingPolicy.allowMessages("CASE_DATA", null)).isTrue();
	}

	@Test
	void allowMessages_systemNotAllowed_returnsFalse() {
		assertThat(messagingPolicy.allowMessages("BYGGR", null)).isFalse();
	}

	@Test
	void allowMessages_systemNotConfiguredAndDefaultFalse_returnsFalse() {
		assertThat(messagingPolicy.allowMessages("ALKT", null)).isFalse();
	}

	@Test
	void allowMessages_systemNotConfiguredAndDefaultTrue_returnsTrue() {
		final var policy = new MessagingPolicy(new AllowMessagesProperties(true, Map.of()));

		assertThat(policy.allowMessages("ALKT", null)).isTrue();
	}

	@Test
	void allowMessages_systemNull_returnsDefault() {
		final var policy = new MessagingPolicy(new AllowMessagesProperties(true, Map.of()));

		assertThat(messagingPolicy.allowMessages(null, null)).isFalse();
		assertThat(policy.allowMessages(null, null)).isTrue();
	}

	@Test
	void allowMessages_openEFamilyIdNotBlocked_returnsTrue() {
		assertThat(messagingPolicy.allowMessages(OPEN_E_PLATFORM, "111")).isTrue();
	}

	@Test
	void allowMessages_openEFamilyIdBlocked_returnsFalse() {
		assertThat(messagingPolicy.allowMessages(OPEN_E_PLATFORM, "999")).isFalse();
	}

	@Test
	void allowMessages_openEFamilyIdNull_returnsFalse() {
		assertThat(messagingPolicy.allowMessages(OPEN_E_PLATFORM, null)).isFalse();
	}

	@Test
	void allowMessages_openENotConfiguredAndDefaultTrue_returnsTrue() {
		final var policy = new MessagingPolicy(new AllowMessagesProperties(true, Map.of()));

		assertThat(policy.allowMessages(OPEN_E_PLATFORM, "111")).isTrue();
	}

	@Test
	void allowMessages_blockedFamilyIdOnOtherSystem_isIgnored() {
		assertThat(messagingPolicy.allowMessages("CASE_DATA", "999")).isTrue();
	}

}
