package se.sundsvall.casestatus.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("junit")
@SpringBootTest(classes = DbIntegrationConfiguration.class)
class DbIntegrationConfigurationTests {

    @Autowired
    @Qualifier("integration.db.case-status.datasource-properties")
    private DataSourceProperties caseStatusDataSourceProperties;

    @Autowired
    @Qualifier("integration.db.case-status.datasource")
    private DataSource caseStatusDataSource;

    @Autowired
    @Qualifier("integration.db.case-status.jdbc-template")
    private NamedParameterJdbcTemplate caseStatusJdbcTemplate;


    @Test
    void caseStatusDataSourcePropertiesBeanIsCreated() {
        assertThat(caseStatusDataSourceProperties).isNotNull();
    }

    @Test
    void caseStatusDataSourceBeanIsCreated() {
        assertThat(caseStatusDataSource).isNotNull();
    }

    @Test
    void caseStatusJdbcTemplateBeanIsCreated() {
        assertThat(caseStatusJdbcTemplate).isNotNull();
    }
}
