package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;

@CircuitBreaker(name = "caseManagementOpeneViewRepository")
public interface CaseManagementOpeneViewRepository extends JpaRepository<CaseManagementOpeneView, String> {

	Optional<CaseManagementOpeneView> findByCaseManagementId(String id);
}
