package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.casestatus.integration.db.model.SupportManagementStatusEntity;

@Repository
@CircuitBreaker(name = "supportManagementStatusRepository")
public interface SupportManagementStatusRepository extends JpaRepository<SupportManagementStatusEntity, Integer> {

	Optional<SupportManagementStatusEntity> findBySystemStatus(String systemStatus);
}
