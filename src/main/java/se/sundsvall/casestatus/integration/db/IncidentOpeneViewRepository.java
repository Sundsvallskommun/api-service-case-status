package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.views.IncidentOpeneView;

@CircuitBreaker(name = "incidentOpeneViewRepository")
public interface IncidentOpeneViewRepository extends JpaRepository<IncidentOpeneView, Integer> {

	Optional<IncidentOpeneView> findByIncidentId(Integer incidentId);

}
