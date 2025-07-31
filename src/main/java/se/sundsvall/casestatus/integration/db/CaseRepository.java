package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;

@CircuitBreaker(name = "caseRepository")
public interface CaseRepository extends JpaRepository<CaseEntity, String> {

	Optional<CaseEntity> findByFlowInstanceIdAndMunicipalityId(String flowInstanceId, String municipalityId);

	List<CaseEntity> findByOrganisationNumberAndMunicipalityId(String organisationNumber, String municipalityId);

	List<CaseEntity> findByPersonIdAndMunicipalityId(String personId, String municipalityId);
}
