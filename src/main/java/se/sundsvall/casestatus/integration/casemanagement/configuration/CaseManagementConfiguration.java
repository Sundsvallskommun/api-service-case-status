package se.sundsvall.casestatus.integration.casemanagement.configuration;

import java.util.List;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * CaseManagement answers 404, not an empty list, when a party or organization has no cases. 404 is therefore bypassed
 * in
 * the error decoder so it arrives as a ClientProblem with NOT_FOUND rather than being wrapped as BAD_GATEWAY like any
 * other error — without it, "this person has no cases" is indistinguishable from "CaseManagement is broken".
 */
@Import(FeignConfiguration.class)
public class CaseManagementConfiguration {

	public static final String CLIENT_ID = "case-management";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final CaseManagementProperties caseManagementProperties, final ClientRegistrationRepository clientRepository) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID, List.of(NOT_FOUND.value())))
			.withRequestTimeoutsInSeconds(caseManagementProperties.connectTimeout(), caseManagementProperties.readTimeout())
			.withRetryableOAuth2InterceptorForClientRegistration(clientRepository.findByRegistrationId(CLIENT_ID))
			.composeCustomizersToOne();
	}

}
