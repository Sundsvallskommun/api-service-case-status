package se.sundsvall.casestatus.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;

public interface CaseTypeRepository extends JpaRepository<CaseTypeEntity, Integer> {

	Optional<CaseTypeEntity> findByEnumValueAndMunicipalityId(String enumValue, String municipalityId);

}
