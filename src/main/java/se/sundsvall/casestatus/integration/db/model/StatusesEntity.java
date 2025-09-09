package se.sundsvall.casestatus.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "statuses", indexes = {
	@Index(name = "idx_oep_status", columnList = "oep_status"),
	@Index(name = "idx_support_management_status", columnList = "support_management_status"),
	@Index(name = "idx_case_management_status", columnList = "case_management_status"),
})
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class StatusesEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "support_management_status")
	private String supportManagementStatus;

	@Column(name = "support_management_display_name")
	private String supportManagementDisplayName;

	@Column(name = "case_management_status")
	private String caseManagementStatus;

	@Column(name = "case_management_display_name")
	private String caseManagementDisplayName;

	@Column(name = "oep_status")
	private String oepStatus;

	@Column(name = "oep_display_name")
	private String oepDisplayName;

	@Column(name = "external_status")
	private String externalStatus;

	@Column(name = "external_display_name")
	private String externalDisplayName;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final StatusesEntity that = (StatusesEntity) o;
		return id == that.id && Objects.equals(supportManagementStatus, that.supportManagementStatus) && Objects.equals(supportManagementDisplayName, that.supportManagementDisplayName) &&
			Objects.equals(caseManagementStatus, that.caseManagementStatus) && Objects.equals(caseManagementDisplayName, that.caseManagementDisplayName) &&
			Objects.equals(oepStatus, that.oepStatus) && Objects.equals(oepDisplayName, that.oepDisplayName) && Objects.equals(externalStatus, that.externalStatus) &&
			Objects.equals(externalDisplayName, that.externalDisplayName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, supportManagementStatus, supportManagementDisplayName, caseManagementStatus, caseManagementDisplayName,
			caseManagementDisplayName, oepStatus, oepDisplayName, externalStatus, externalDisplayName);
	}

	@Override
	public String toString() {
		return "StatusesEntity{" +
			"id=" + id +
			", supportManagementStatus='" + supportManagementStatus + '\'' +
			", supportManagementDisplayName='" + supportManagementDisplayName + '\'' +
			", caseManagementStatus='" + caseManagementStatus + '\'' +
			", caseManagementDisplayName='" + caseManagementDisplayName + '\'' +
			", oepStatus='" + oepStatus + '\'' +
			", oepDisplayName='" + oepDisplayName + '\'' +
			", externalStatus='" + externalStatus + '\'' +
			", externalDisplayName='" + externalDisplayName + '\'' +
			'}';
	}
}
