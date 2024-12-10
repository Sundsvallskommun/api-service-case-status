package se.sundsvall.casestatus.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.PrivateEntity;

public interface PrivateRepository extends JpaRepository<PrivateEntity, String> {

}
