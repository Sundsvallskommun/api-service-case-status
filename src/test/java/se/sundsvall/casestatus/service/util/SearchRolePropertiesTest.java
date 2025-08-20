package se.sundsvall.casestatus.service.util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casestatus.Application;
import se.sundsvall.casestatus.util.RoleSearchProperties;

@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("junit")
class SearchRolePropertiesTest {

	static final String MUNICIPALITY_ID = "2281";
	static final String MY_NAMESPACE = "MY_NAMESPACE";
	static final String MY_NAMESPACE2 = "MY_NAMESPACE2";

	@Autowired
	private RoleSearchProperties searchRoleProperties;

	@Test
	void searchRoleOfMyNamespace() {
		assertThat(searchRoleProperties.getRoles().get(MUNICIPALITY_ID)).containsEntry(MY_NAMESPACE, "MY_ROLE");
		assertThat(searchRoleProperties.getRoles().get(MUNICIPALITY_ID)).containsEntry(MY_NAMESPACE2, "MY_ROLE2");

	}
}
