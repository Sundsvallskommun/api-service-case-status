package openapi;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.util.ResourceUtils;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;

@ActiveProfiles("it")
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = Application.class,
    properties = {
        "spring.main.banner-mode=off",
        "logging.level.se.sundsvall.dept44.payload=OFF",
        "wiremock.server.port=10101"
    }
)
class OpenApiSpecificationIT {

    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();

    @Value("${openapi.name}")
    private String openApiName;
    @Value("${openapi.version}")
    private String openApiVersion;

    @Value("classpath:/openapi.yaml")
    private Resource openApiResource;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void compareOpenApiSpecifications() {
        String existingOpenApiSpecification = ResourceUtils.asString(openApiResource);
        String currentOpenApiSpecification = getCurrentOpenApiSpecification();

        assertThatJson(toJson(existingOpenApiSpecification))
            .withOptions(new Options(Option.IGNORING_ARRAY_ORDER))
            .whenIgnoringPaths("servers")
            .isEqualTo(toJson(currentOpenApiSpecification));
    }

    /**
     * Fetches and returns the current OpenAPI specification in YAML format.
     *
     * @return the current OpenAPI specification
     */
    private String getCurrentOpenApiSpecification() {
        var uri = UriComponentsBuilder.fromPath("/api-docs.yaml")
            .buildAndExpand(openApiName, openApiVersion)
            .toUri();

        return restTemplate.getForObject(uri, String.class);
    }

    /**
     * Attempts to convert the given YAML (no YAML-check...) to JSON.
     *
     * @param yaml the YAML to convert
     * @return a JSON string
     */
    private String toJson(final String yaml) {
        try {
            return YAML_MAPPER.readTree(yaml).toString();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert YAML to JSON", e);
        }
    }
}
