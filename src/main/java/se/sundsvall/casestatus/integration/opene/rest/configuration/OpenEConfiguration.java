package se.sundsvall.casestatus.integration.opene.rest.configuration;

import feign.auth.BasicAuthRequestInterceptor;
import feign.soap.SOAPErrorDecoder;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

@Import(FeignConfiguration.class)
public class OpenEConfiguration {

	public static final String CLIENT_ID = "open-e";

	@Bean
	FeignBuilderCustomizer feignBuilderCustomizer(final OpenEProperties properties) {
		return FeignMultiCustomizer.create()
			.withErrorDecoder(new SOAPErrorDecoder())
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.withRequestInterceptor(new BasicAuthRequestInterceptor(properties.username(), properties.password()))
			.composeCustomizersToOne();
	}

}
