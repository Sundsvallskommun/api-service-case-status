package se.sundsvall.casestatus.integration.casemanagement.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.casestatus.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class CaseManagementPropertiesTest {

	@Autowired
	private CaseManagementProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeout()).isEqualTo(5);
		assertThat(properties.readTimeout()).isEqualTo(30);
	}

}
