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

@Entity
@Table(name = "unknown")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class UnknownEntity {

	@Id
	@Column(name = "flow_instance_id")
	private String flowInstanceId;

	@Column(name = "family_id")
	private String familyId;

	@Column(name = "status")
	private String status;

	@Column(name = "errand_type")
	private String errandType;

	@Column(name = "content_type")
	private String contentType;

	@Column(name = "first_submitted")
	private String firstSubmitted;

	@Column(name = "last_status_change")
	private String lastStatusChange;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final UnknownEntity that = (UnknownEntity) o;
		return Objects.equals(flowInstanceId, that.flowInstanceId) && Objects.equals(familyId, that.familyId) && Objects.equals(status, that.status) && Objects.equals(errandType, that.errandType) && Objects.equals(contentType, that.contentType) && Objects.equals(firstSubmitted, that.firstSubmitted) && Objects.equals(lastStatusChange, that.lastStatusChange) && Objects.equals(municipalityId, that.municipalityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(flowInstanceId, familyId, status, errandType, contentType, firstSubmitted, lastStatusChange, municipalityId);
	}

	@Override
	public String toString() {
		return "UnknownEntity{" +
			"flowInstanceId='" + flowInstanceId + '\'' +
			", familyId='" + familyId + '\'' +
			", status='" + status + '\'' +
			", errandType='" + errandType + '\'' +
			", contentType='" + contentType + '\'' +
			", firstSubmitted='" + firstSubmitted + '\'' +
			", lastStatusChange='" + lastStatusChange + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			'}';
	}

}
