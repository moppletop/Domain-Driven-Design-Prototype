package com.moppletop.ddd.autoconfigure;

import com.moppletop.ddd.autoconfigure.DriverDatabaseAutoConfiguration.DatabaseProperties;
import com.moppletop.ddd.database.TransactionAwareDataSourceProxy;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
@AutoConfigureBefore({DataSourceAutoConfiguration.class, DDDAutoConfiguration.class})
@Slf4j
public class DriverDatabaseAutoConfiguration {

    @ConfigurationProperties("driver.ds")
    @NoArgsConstructor
    public static class DatabaseProperties extends HikariConfig {
    }

    @ConditionalOnProperty(name = "driver.ds.jdbcUrl")
    @ConditionalOnMissingBean(DataSource.class)
    @Bean
    @Primary
    public DataSource driverDataSource(DatabaseProperties config) {
        log.info("Driver DataSource Configuration Found!");
        // Important here that we wrap this datasource, this will mean we never have to worry about transaction management
        // or connections for the domain event handlers
        return new TransactionAwareDataSourceProxy(new HikariDataSource(config));
    }
}
