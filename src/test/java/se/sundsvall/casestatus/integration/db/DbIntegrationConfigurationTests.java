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
    @Qualifier("integrationDbCaseStatusDatasourceProperties")
    private DataSourceProperties caseStatusDataSourceProperties;

    @Autowired
    @Qualifier("integrationDbCaseStatusDatasource")
    private DataSource caseStatusDataSource;

    @Autowired
    @Qualifier("integrationDbCaseStatusJdbcTemplate")
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
