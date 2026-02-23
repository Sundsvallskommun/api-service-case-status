package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;

@CircuitBreaker(name = "executionInformationRepository")
public interface ExecutionInformationRepository extends JpaRepository<ExecutionInformationEntity, String> {

	Optional<ExecutionInformationEntity> findByMunicipalityIdAndServiceName(String municipalityId, String serviceName);

}
