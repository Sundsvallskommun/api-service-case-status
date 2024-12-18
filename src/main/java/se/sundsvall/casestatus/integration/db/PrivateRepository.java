package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.PrivateEntity;

@CircuitBreaker(name = "privateRepository")
public interface PrivateRepository extends JpaRepository<PrivateEntity, String> {

	List<PrivateEntity> findByPersonIdAndMunicipalityId(String personId, String municipalityId);

}
