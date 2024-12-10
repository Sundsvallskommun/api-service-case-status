package se.sundsvall.casestatus.integration.db;

import static java.nio.file.Files.readString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
class SchemaVerificationTest {

	private static final String STORED_SCHEMA_FILE = "db/schema.sql";

	@Value("${spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target}")
	private String generatedSchemaFile;

	@Test
	void verifySchemaUpdates() throws IOException, URISyntaxException {

		final var storedSchema = getResourceString();
		final var generatedSchema = readString(Path.of(generatedSchemaFile));

		assertThat(storedSchema)
			.as("Please reflect modifications to entities in file: %s".formatted(STORED_SCHEMA_FILE))
			.isEqualToNormalizingWhitespace(generatedSchema);
	}

	private String getResourceString() throws IOException, URISyntaxException {
		return readString(Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(SchemaVerificationTest.STORED_SCHEMA_FILE)).toURI()));
	}

}
