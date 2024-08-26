package se.sundsvall.casestatus.integration.db.model;


import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Private")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class PrivateEntity {

	@Id
	private String flowInstanceID;

	private String municipalityId;

	private String familyID;

	private String personId;

	private String status;

	private String errandType;

	private String contentType;

	private String firstSubmitted;

	private String lastStatusChange;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final PrivateEntity that = (PrivateEntity) o;
		return Objects.equals(flowInstanceID, that.flowInstanceID) && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(familyID, that.familyID) && Objects.equals(personId, that.personId) && Objects.equals(status, that.status) && Objects.equals(errandType, that.errandType) && Objects.equals(contentType, that.contentType) && Objects.equals(firstSubmitted, that.firstSubmitted) && Objects.equals(lastStatusChange, that.lastStatusChange);
	}

	@Override
	public int hashCode() {
		return Objects.hash(flowInstanceID, municipalityId, familyID, personId, status, errandType, contentType, firstSubmitted, lastStatusChange);
	}

	@Override
	public String toString() {
		return "PrivateEntity{" +
			"flowInstanceID='" + flowInstanceID + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			", familyID='" + familyID + '\'' +
			", personId='" + personId + '\'' +
			", status='" + status + '\'' +
			", errandType='" + errandType + '\'' +
			", contentType='" + contentType + '\'' +
			", firstSubmitted='" + firstSubmitted + '\'' +
			", lastStatusChange='" + lastStatusChange + '\'' +
			'}';
	}

}
