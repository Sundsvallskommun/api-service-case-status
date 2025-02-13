package se.sundsvall.casestatus.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Entity
@Table(name = "case_status", indexes = {
	@Index(name = "idx_flow_instance_id_municipality_id", columnList = "flow_instance_id, municipality_id"),
	@Index(name = "idx_organisation_number_municipality_id", columnList = "organisation_number, municipality_id"),
	@Index(name = "idx_person_id_municipality_id", columnList = "person_id, municipality_id")
})
public class CaseEntity {

	@Id
	@Column(name = "flow_instance_id")
	private String flowInstanceId;

	@Column(name = "family_id")
	private String familyId;

	@Column(name = "status")
	private String status;

	@Column(name = "errand_type")
	private String errandType;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "first_submitted")
	private String firstSubmitted;

	@Column(name = "last_status_change")
	private String lastStatusChange;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "organisation_number")
	private String organisationNumber;

	@Column(name = "person_id")
	private String personId;
}
