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
@Table(name = "Unknown")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class UnknownEntity {

	@Id
	private String flowInstanceID;

	private String familyID;

	private String status;

	private String errandType;

	private String contentType;

	private String firstSubmitted;

	private String lastStatusChange;

	private String municipalityId;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final UnknownEntity that = (UnknownEntity) o;
		return Objects.equals(flowInstanceID, that.flowInstanceID) && Objects.equals(familyID, that.familyID) && Objects.equals(status, that.status) && Objects.equals(errandType, that.errandType) && Objects.equals(contentType, that.contentType) && Objects.equals(firstSubmitted, that.firstSubmitted) && Objects.equals(lastStatusChange, that.lastStatusChange) && Objects.equals(municipalityId, that.municipalityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(flowInstanceID, familyID, status, errandType, contentType, firstSubmitted, lastStatusChange, municipalityId);
	}

	@Override
	public String toString() {
		return "UnknownEntity{" +
			"flowInstanceID='" + flowInstanceID + '\'' +
			", familyID='" + familyID + '\'' +
			", status='" + status + '\'' +
			", errandType='" + errandType + '\'' +
			", contentType='" + contentType + '\'' +
			", firstSubmitted='" + firstSubmitted + '\'' +
			", lastStatusChange='" + lastStatusChange + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			'}';
	}

}
