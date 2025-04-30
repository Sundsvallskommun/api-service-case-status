package se.sundsvall.casestatus.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.casestatus.integration.db.model.SupportManagementStatusEntity;

@Repository
public interface SupportManagementStatusRepository extends JpaRepository<SupportManagementStatusEntity, Integer> {

	Optional<SupportManagementStatusEntity> findBySystemStatus(String systemStatus);
}
