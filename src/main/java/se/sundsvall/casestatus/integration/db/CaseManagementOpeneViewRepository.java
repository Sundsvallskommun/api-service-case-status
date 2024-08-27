package se.sundsvall.casestatus.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;

public interface CaseManagementOpeneViewRepository extends JpaRepository<CaseManagementOpeneView, String> {

	Optional<CaseManagementOpeneView> findByCaseManagementId(String id);

}
