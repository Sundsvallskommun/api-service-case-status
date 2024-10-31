package se.sundsvall.casestatus.integration.db.model;

import java.util.Objects;

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
@Table(name = "case_type")
public class CaseTypeEntity {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "enum")
	private String enumValue;

	@Column(name = "description")
	private String description;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final CaseTypeEntity that = (CaseTypeEntity) o;
		return id == that.id && Objects.equals(enumValue, that.enumValue) && Objects.equals(description, that.description) && Objects.equals(municipalityId, that.municipalityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, enumValue, description, municipalityId);
	}

	@Override
	public String toString() {
		return "CaseTypeEntity{" +
			"id=" + id +
			", enumValue='" + enumValue + '\'' +
			", description='" + description + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			'}';
	}

}
