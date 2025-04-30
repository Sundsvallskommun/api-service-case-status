package se.sundsvall.casestatus.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "support_management_status", indexes = {
	@Index(name = "idx_system_status", columnList = "system_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportManagementStatusEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "system_status")
	private String systemStatus;

	@Column(name = "generic_status")
	private String genericStatus;
}
