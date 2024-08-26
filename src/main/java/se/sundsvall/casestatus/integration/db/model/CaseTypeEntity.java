package se.sundsvall.casestatus.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Entity
@Table(name = "MapCaseTypeEnums")
public class CaseTypeEntity {

	@Id
	@Column(name = "ID")
	private int id;

	@Column(name = "ENUM")
	private String enumValue;

	@Column(name = "Text")
	private String description;

	@Column(name = "municipalityId")
	private String municipalityId;

}
