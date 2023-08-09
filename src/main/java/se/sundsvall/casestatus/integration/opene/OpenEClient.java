package se.sundsvall.casestatus.integration.opene;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import se.sundsvall.casestatus.integration.opene.exception.OpenEException;

@Component
public class OpenEClient {

	private static final String ERRANDS_PATH = "/api/instanceapi/getinstances/family/{familyid}";
	private static final String PDF_PATH = "/api/instanceapi/getinstance/{flowinstanceid}/pdf";
	private static final String ERRAND_PATH = "/api/instanceapi/getinstance/{flowinstanceid}/xml";
	private static final String STATUS_PATH = "/api/instanceapi/getstatus/{flowinstanceid}";

	private static final String FLOW_INSTANCE_ID_ATTRIBUTE_NAME = "flowinstanceid";
	private static final String FAMILY_ID_ATTRIBUTE_NAME = "familyid";

	private final OpenEIntegrationProperties properties;

	public OpenEClient(OpenEIntegrationProperties properties) {
		this.properties = properties;
	}

	public byte[] getErrandIds(String familyid) throws IOException {
		return getBytes(buildUrl(ERRANDS_PATH, Map.of(FAMILY_ID_ATTRIBUTE_NAME, familyid)));
	}

	public byte[] getErrand(String id) throws IOException {
		return getBytes(buildUrl(ERRAND_PATH, Map.of(FLOW_INSTANCE_ID_ATTRIBUTE_NAME, id)));
	}

	public byte[] getErrandStatus(String id) throws IOException {
		return getBytes(buildUrl(STATUS_PATH, Map.of(FLOW_INSTANCE_ID_ATTRIBUTE_NAME, id)));
	}

	public byte[] getPDF(String id) throws IOException {
		return getBytes(buildUrl(PDF_PATH, Map.of(FLOW_INSTANCE_ID_ATTRIBUTE_NAME, id)));
	}

	private byte[] getBytes(URI url) throws IOException {

		final var response = HttpClients.custom()
			.setDefaultCredentialsProvider(getCredentials())
			.build()
			.execute(new HttpGet(url));

		if (response.getCode() == 200) {
			return EntityUtils.toByteArray(response.getEntity());
		}
		throw new OpenEException(response.toString());
	}

	private URI buildUrl(String path, Map<String, String> parameters) {
		return UriComponentsBuilder.newInstance()
			.scheme(properties.getScheme())
			.host(properties.getBaseUrl())
			.port(properties.getPort())
			.path(path)
			.queryParam("fromDate", LocalDate.now(ZoneId.systemDefault()).minusDays(1).toString())
			.build(parameters);
	}

	private BasicCredentialsProvider getCredentials() {
		final var user = properties.getBasicAuth().getUsername();
		final var password = properties.getBasicAuth().getPassword().toCharArray();
		final var credsProvider = new BasicCredentialsProvider();

		credsProvider.setCredentials(new AuthScope(null, -1),
			new UsernamePasswordCredentials(user, password));
		return credsProvider;
	}
}
