package se.sundsvall.casestatus.integration.db.model.views;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vStatusCaseManagementOpenE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Immutable
public class CaseManagementOpeneView {

	@Id
	@Column(name = "case_management_id")
	private String caseManagementId;

	@Column(name = "opene_id")
	private String openEId;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final CaseManagementOpeneView that = (CaseManagementOpeneView) o;
		return Objects.equals(caseManagementId, that.caseManagementId) && Objects.equals(openEId, that.openEId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(caseManagementId, openEId);
	}

	@Override
	public String toString() {
		return "CaseManagementOpeneView{" +
			"caseManagementId='" + caseManagementId + '\'' +
			", openEId='" + openEId + '\'' +
			'}';
	}

}
