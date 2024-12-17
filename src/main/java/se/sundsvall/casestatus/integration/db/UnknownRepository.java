package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.UnknownEntity;

@CircuitBreaker(name = "unknownRepository")
public interface UnknownRepository extends JpaRepository<UnknownEntity, String> {

}
