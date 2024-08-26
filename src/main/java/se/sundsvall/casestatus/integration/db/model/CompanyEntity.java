package se.sundsvall.casestatus.integration.db.model;

import java.sql.Timestamp;
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

@Entity
@Table(name = "Companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class CompanyEntity {

	@Id
	@Column(name = "flowInstanceID")
	private String flowInstanceId;

	@Column(name = "municipalityId")
	private String municipalityId;

	@Column(name = "organisationNumber")
	private String organisationNumber;

	@Column(name = "familyID")
	private String familyID;

	private String status;

	private String errandType;

	private String contentType;

	private String firstSubmitted;

	private String lastStatusChange;

	private Timestamp sysStartTime;

	private Timestamp sysEndTime;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final CompanyEntity that = (CompanyEntity) o;
		return Objects.equals(flowInstanceId, that.flowInstanceId) && Objects.equals(familyID, that.familyID) && Objects.equals(status, that.status) && Objects.equals(errandType, that.errandType) && Objects.equals(contentType, that.contentType) && Objects.equals(firstSubmitted, that.firstSubmitted) && Objects.equals(lastStatusChange, that.lastStatusChange) && Objects.equals(municipalityId, that.municipalityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(flowInstanceId, familyID, status, errandType, contentType, firstSubmitted, lastStatusChange, municipalityId);
	}

	@Override
	public String toString() {
		return "CompanyEntity{" +
			"flowInstanceID='" + flowInstanceId + '\'' +
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
