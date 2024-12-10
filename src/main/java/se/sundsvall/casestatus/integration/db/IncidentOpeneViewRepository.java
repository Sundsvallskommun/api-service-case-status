package se.sundsvall.casestatus.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.views.IncidentOpeneView;

public interface IncidentOpeneViewRepository extends JpaRepository<IncidentOpeneView, Integer> {

	Optional<IncidentOpeneView> findByIncidentId(Integer incidentId);

}
