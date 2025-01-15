package se.sundsvall.casestatus.integration.opene.soap.client;

import static se.sundsvall.casestatus.integration.opene.soap.configuration.OpenECallbackInternalConfiguration.OPENE_CALLBACK_INTERNAL_CLIENT;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import se.sundsvall.casestatus.integration.opene.soap.configuration.OpenECallbackInternalConfiguration;

@FeignClient(
	name = OPENE_CALLBACK_INTERNAL_CLIENT,
	url = "${integration.open-e.callback.internal-base-url}",
	configuration = OpenECallbackInternalConfiguration.class)
@CircuitBreaker(name = OPENE_CALLBACK_INTERNAL_CLIENT)
public interface OpenECallbackInternalClient extends OpenECallbackBaseClient {

}
