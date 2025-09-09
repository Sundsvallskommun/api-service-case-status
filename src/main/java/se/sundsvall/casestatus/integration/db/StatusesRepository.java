package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;

@Repository
@CircuitBreaker(name = "statusesRepository")
public interface StatusesRepository extends JpaRepository<StatusesEntity, Integer> {

	List<StatusesEntity> findByOepStatus(String oepStatus);

	Optional<StatusesEntity> findBySupportManagementStatus(String supportManagementStatus);

	Optional<StatusesEntity> findByCaseManagementStatus(String caseManagementStatus);
}
