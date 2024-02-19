package se.sundsvall.casestatus.integration.opene.configuration;

import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import feign.auth.BasicAuthRequestInterceptor;

@Import(FeignConfiguration.class)
public class OpenEIntegrationConfiguration {

	public static final String CLIENT_ID = "open-e";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final OpenEIntegrationProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new ProblemErrorDecoder(CLIENT_ID))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.withRequestInterceptor(new BasicAuthRequestInterceptor(properties.username(), properties.password()))
			.composeCustomizersToOne();
	}

}
