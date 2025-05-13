package se.sundsvall.casestatus.integration.oepintegrator;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casestatus.integration.oepintegrator.configuration.OepIntegratorConfiguration.REGISTRATION_ID;

import generated.client.oep_integrator.CaseEnvelope;
import generated.client.oep_integrator.CaseStatus;
import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.ConfirmDeliveryRequest;
import generated.client.oep_integrator.InstanceType;
import generated.client.oep_integrator.ModelCase;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casestatus.integration.oepintegrator.configuration.OepIntegratorConfiguration;

@FeignClient(name = REGISTRATION_ID, url = "${integration.oep-integrator.base-url}", configuration = OepIntegratorConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = REGISTRATION_ID)
public interface OepIntegratorClient {

	@PostMapping(value = "{municipalityId}/{instanceType}/cases/{flowInstanceId}/delivery", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> confirmDelivery(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId,
		@RequestBody final ConfirmDeliveryRequest confirmDeliveryRequest);

	@PutMapping(value = "{municipalityId}/{instanceType}/cases/{flowInstanceId}/status", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> setStatus(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId,
		@RequestBody final CaseStatusChangeRequest setStatusRequest);

	@GetMapping(value = "{municipalityId}/{instanceType}/cases/families/{familyId}", produces = APPLICATION_JSON_VALUE)
	List<CaseEnvelope> getCases(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("familyId") final int familyId);

	@GetMapping(value = "{municipalityId}/{instanceType}/cases/{flowInstanceId}", produces = APPLICATION_JSON_VALUE)
	ModelCase getCase(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId);

	@GetMapping(value = "{municipalityId}/{instanceType}/cases/{flowInstanceId}/status", produces = APPLICATION_JSON_VALUE)
	CaseStatus getCaseStatus(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId);

	@GetMapping(path = "{municipalityId}/{instanceType}/cases/parties/{partyId}", produces = APPLICATION_JSON_VALUE)
	List<CaseEnvelope> getCasesByPartyId(
		@PathVariable final String municipalityId,
		@PathVariable final InstanceType instanceType,
		@PathVariable final String partyId);

	@GetMapping(value = "{municipalityId}/{instanceType}/cases/{flowInstanceId}/pdf", produces = ALL_VALUE)
	ResponseEntity<InputStreamResource> getCasePdfByFlowInstanceId(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("instanceType") final InstanceType instanceType,
		@PathVariable("flowInstanceId") final String flowInstanceId);

}
