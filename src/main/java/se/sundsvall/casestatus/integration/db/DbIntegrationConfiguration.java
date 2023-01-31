package se.sundsvall.casestatus.integration.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
class DbIntegrationConfiguration {

    @Bean("integration.db.case-status.datasource-properties")
    @ConfigurationProperties(prefix = "integration.db.case-status")
    DataSourceProperties caseStatusDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("integration.db.case-status.datasource")
    DataSource caseStatusDataSource(
            @Qualifier("integration.db.case-status.datasource-properties") final DataSourceProperties properties) {
        return properties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean("integration.db.case-status.jdbc-template")
    NamedParameterJdbcTemplate caseStatusJdbcTemplate(
            @Qualifier("integration.db.case-status.datasource") final DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
    
}
