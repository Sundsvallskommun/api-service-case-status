package se.sundsvall.casestatus.integration.opene;


import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@Component
public class OpenEClient {

    private final OpenEIntegrationProperties properties;

    private final static String ERRANDS_PATH = "/api/instanceapi/getinstances/family/{familyid}";
    private final static String PDF_PATH = "/api/instanceapi/getinstance/{flowinstanceid}/pdf";
    private final static String ERRAND_PATH = "/api/instanceapi/getinstance/{flowinstanceid}/xml";
    private final static String STATUS_PATH = "/api/instanceapi/getstatus/{flowinstanceid}";

    public OpenEClient(OpenEIntegrationProperties properties) {
        this.properties = properties;
    }


    public byte[] getErrandIds(String familyid) throws IOException {
        return getBytes(buildUrl(ERRANDS_PATH, Map.of("familyid", familyid)));
    }

    public byte[] getErrand(String id) throws IOException {
        return getBytes(buildUrl(ERRAND_PATH, Map.of("flowinstanceid", id)));
    }

    public byte[] getErrandStatus(String id) throws IOException {
        return getBytes(buildUrl(STATUS_PATH, Map.of("flowinstanceid", id)));
    }

    public byte[] getPDF(String id) throws IOException {
        return getBytes(buildUrl(PDF_PATH, Map.of("flowinstanceid", id)));
    }

    private byte[] getBytes(URI url) throws IOException {

        var response = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentials())
                .build()
                .execute(new HttpGet(url));

        if (response.getCode() == 200) {
            return EntityUtils.toByteArray(response.getEntity());
        }
        throw new RuntimeException(response.toString());

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
        var user = properties.getBasicAuth().getUsername();
        var password = properties.getBasicAuth().getPassword().toCharArray();
        var credsProvider = new BasicCredentialsProvider();

        credsProvider.setCredentials(new AuthScope(null, -1),
                new UsernamePasswordCredentials(user, password));
        return credsProvider;
    }


}
