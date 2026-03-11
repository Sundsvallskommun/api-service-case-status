package se.sundsvall.casestatus.service.util;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvironmentUtilTest {

	@Mock
	private Environment environmentMock;

	@InjectMocks
	private EnvironmentUtil environmentUtil;

	@ParameterizedTest
	@MethodSource("profileArguments")
	void extractEnvironment(final String profile, final String expected) {
		when(environmentMock.getActiveProfiles()).thenReturn(new String[] {
			profile
		});

		assertThat(environmentUtil.extractEnvironment()).isEqualTo(expected);
	}

	private static Stream<Arguments> profileArguments() {
		return Stream.of(
			Arguments.of("production", "production"),
			Arguments.of("PRODUCTION", "PRODUCTION"),
			Arguments.of("test", "test"),
			Arguments.of("junit", "junit"),
			Arguments.of("it", "it"),
			Arguments.of("dev", "dev"));
	}
}
