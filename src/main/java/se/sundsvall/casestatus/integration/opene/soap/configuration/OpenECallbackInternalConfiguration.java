package se.sundsvall.casestatus.integration.opene.soap.configuration;

import feign.auth.BasicAuthRequestInterceptor;
import feign.jaxb.JAXBContextFactory;
import feign.soap.SOAPDecoder;
import feign.soap.SOAPEncoder;
import feign.soap.SOAPErrorDecoder;
import jakarta.xml.soap.SOAPConstants;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;

@Import(FeignConfiguration.class)
public class OpenECallbackInternalConfiguration {

	public static final String OPENE_CALLBACK_INTERNAL_CLIENT = "opene-callback-internal";

	private static final JAXBContextFactory JAXB_FACTORY = new JAXBContextFactory.Builder().build();

	private static final SOAPEncoder.Builder SOAP_ENCODER_BUILDER = new SOAPEncoder.Builder()
		.withFormattedOutput(false)
		.withJAXBContextFactory(JAXB_FACTORY)
		.withSOAPProtocol(SOAPConstants.SOAP_1_1_PROTOCOL)
		.withWriteXmlDeclaration(true);

	@Bean
	FeignBuilderCustomizer internalFeignBuilderCustomizer(final OpenECallbackProperties properties) {
		return FeignMultiCustomizer.create()
			.withDecoder(new SOAPDecoder(JAXB_FACTORY))
			.withEncoder(SOAP_ENCODER_BUILDER.build())
			.withErrorDecoder(new SOAPErrorDecoder())
			.withRequestInterceptor(new BasicAuthRequestInterceptor(properties.username(), properties.internalPassword()))
			.withRequestTimeoutsInSeconds(properties.connectTimeout(), properties.readTimeout())
			.composeCustomizersToOne();
	}
}
