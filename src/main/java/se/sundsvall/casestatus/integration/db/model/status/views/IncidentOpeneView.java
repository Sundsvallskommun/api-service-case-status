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
@Table(name = "vStatusIncidentOpenE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Immutable
public class IncidentOpeneView {

	@Id
	@Column(name = "incidentID")
	private int incidentId;

	@Column(name = "openEID")
	private String openEId;

	@Column(name = "municipalityId")
	private String municipalityId;

}
