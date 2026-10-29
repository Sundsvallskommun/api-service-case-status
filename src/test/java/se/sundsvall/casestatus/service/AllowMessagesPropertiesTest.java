package se.sundsvall.casestatus.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casestatus.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("junit")
public class AllowMessagesPropertiesTest {

	@Autowired
	private AllowMessagesProperties properties;

	@Test
	void testMessagingConfig() {
		assertThat(properties.defaultAllowed()).isFalse();
		assertThat(properties.systems()).containsOnlyKeys("BYGGR", "ECOS", "CASE_DATA", "SUPPORT_MANAGEMENT", "OPEN_E_PLATFORM");
		assertThat(properties.systems().get("BYGGR").allowed()).isFalse();
		assertThat(properties.systems().get("CASE_DATA").allowed()).isTrue();
		assertThat(properties.systems().get("OPEN_E_PLATFORM").blockedFamilyIds()).containsExactlyInAnyOrder("111", "222");
	}
}
