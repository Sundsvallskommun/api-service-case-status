package se.sundsvall.casestatus.integration.db.model.status.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "vStatusCaseManagementOpenE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Immutable
public class CaseManagementOpeneView {

	@Id
	@Column(name = "caseManagementID")
	private String caseManagementId;

	@Column(name = "openeID")
	private String openEId;

	@Column(name = "municipalityID")
	private String municipalityId;

}
