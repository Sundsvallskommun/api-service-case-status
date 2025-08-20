package se.sundsvall.casestatus.util;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sm-status-search")
public class RoleSearchProperties {

	private Map<String, Map<String, String>> roles;

	public Map<String, Map<String, String>> getRoles() {
		return roles;
	}

	public void setRoles(Map<String, Map<String, String>> roles) {
		this.roles = roles;
	}
}
