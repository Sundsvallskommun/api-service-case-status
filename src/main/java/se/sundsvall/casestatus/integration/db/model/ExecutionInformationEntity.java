package se.sundsvall.casestatus.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

@Entity
@Table(name = "execution_information")
@Data
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExecutionInformationEntity {

	@Id
	@Column(name = "family_id", unique = true)
	private String familyId;

	@Column(name = "municipality_id", length = 4, nullable = false)
	private String municipalityId;

	@Column(name = "last_successful_execution")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime lastSuccessfulExecution;

}
