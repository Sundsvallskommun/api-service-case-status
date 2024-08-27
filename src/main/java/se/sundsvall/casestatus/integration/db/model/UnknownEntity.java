package se.sundsvall.casestatus.integration.db.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "unknown",
	indexes = {
		@Index(name = "idx_unknown_municipality_id", columnList = "municipality_id")
	})
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class UnknownEntity extends BaseEntity {

	@Override
	public String toString() {
		return "UnknownEntity{} " + super.toString();
	}

}
