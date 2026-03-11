package se.sundsvall.casestatus.service.util;

import java.util.Arrays;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static java.lang.String.join;

@Component
public class EnvironmentUtil {

	private final Environment environment;

	public EnvironmentUtil(final Environment environment) {
		this.environment = environment;
	}

	public String extractEnvironment() {
		return Arrays.stream(environment.getActiveProfiles())
			.filter(string -> string.matches("^(?i)(test|production|it|junit)$"))
			.findFirst()
			.orElse(join(",", environment.getActiveProfiles()));
	}
}
