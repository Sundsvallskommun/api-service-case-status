package se.sundsvall.casestatus.integration.db.model;


import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "private", indexes = {
	@Index(name = "idx_private_person_id", columnList = "person_id"),
	@Index(name = "idx_private_municipality_id", columnList = "municipality_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class PrivateEntity extends BaseEntity {

	@Column(name = "person_id")
	private String personId;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		final PrivateEntity that = (PrivateEntity) o;
		return Objects.equals(personId, that.personId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), personId);
	}

	@Override
	public String toString() {
		return "PrivateEntity{" +
			"personId='" + personId + '\'' +
			"} " + super.toString();
	}

}
