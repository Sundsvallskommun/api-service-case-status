package se.sundsvall.casestatus.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.casestatus.integration.db.model.CompanyEntity;

@CircuitBreaker(name = "companyRepository")
public interface CompanyRepository extends JpaRepository<CompanyEntity, String> {

	Optional<CompanyEntity> findByFlowInstanceIdAndMunicipalityId(String flowInstanceId, String municipalityId);

	List<CompanyEntity> findByOrganisationNumberAndMunicipalityId(String organisationNumber, String municipalityId);

}
