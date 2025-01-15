package se.sundsvall.casestatus.integration.opene.soap.client;

import static se.sundsvall.casestatus.integration.opene.soap.configuration.OpenECallbackExternalConfiguration.OPENE_CALLBACK_EXTERNAL_CLIENT;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import se.sundsvall.casestatus.integration.opene.soap.configuration.OpenECallbackExternalConfiguration;

@FeignClient(
	name = OPENE_CALLBACK_EXTERNAL_CLIENT,
	url = "${integration.open-e.callback.external-base-url}",
	configuration = OpenECallbackExternalConfiguration.class)
@CircuitBreaker(name = OPENE_CALLBACK_EXTERNAL_CLIENT)
public interface OpenECallbackExternalClient extends OpenECallbackBaseClient {

}
