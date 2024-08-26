package se.sundsvall.casestatus.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.casestatus.integration.db.model.UnknownEntity;

public interface UnknownRepository extends JpaRepository<UnknownEntity, String> {

}
