package se.sundsvall.casestatus.integration.db.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "unknown")
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
