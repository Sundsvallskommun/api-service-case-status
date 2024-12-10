package se.sundsvall.casestatus.integration.db.model.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "vStatusIncidentOpenE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Immutable
public class IncidentOpeneView {

	@Id
	@Column(name = "incident_id")
	private int incidentId;

	@Column(name = "opene_id")
	private String openEId;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final IncidentOpeneView that = (IncidentOpeneView) o;
		return incidentId == that.incidentId && Objects.equals(openEId, that.openEId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(incidentId, openEId);
	}

	@Override
	public String
		toString() {
		return "IncidentOpeneView{" +
			"incidentId=" + incidentId +
			", openEId='" + openEId + '\'' +
			'}';
	}

}
