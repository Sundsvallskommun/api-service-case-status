package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;

@CircuitBreaker(name = "caseTypeRepository")
public interface CaseTypeRepository extends JpaRepository<CaseTypeEntity, Integer> {

	Optional<CaseTypeEntity> findByEnumValueAndMunicipalityId(String enumValue, String municipalityId);

}
