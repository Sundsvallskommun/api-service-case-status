package se.sundsvall.casestatus.integration.db.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class CompanyEntity extends BaseEntity {

	@Column(name = "organisation_number")
	private String organisationNumber;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		final CompanyEntity that = (CompanyEntity) o;
		return Objects.equals(organisationNumber, that.organisationNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), organisationNumber);
	}

	@Override
	public String toString() {
		return "CompanyEntity{" +
			"organisationNumber='" + organisationNumber + '\'' +
			"} " + super.toString();
	}

}
