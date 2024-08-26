package se.sundsvall.casestatus.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casestatus.integration.db.model.status.views.CaseManagementOpeneView;

public interface CaseManagementOpeneViewRepository extends JpaRepository<CaseManagementOpeneView, String> {

	Optional<CaseManagementOpeneView> findByCaseManagementIdAndMunicipalityId(String id, String municipalityId);

}
