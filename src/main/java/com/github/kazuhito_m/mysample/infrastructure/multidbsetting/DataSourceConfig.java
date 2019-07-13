package com.github.kazuhito_m.mysample.infrastructure.multidbsetting;

import com.github.kazuhito_m.mysample.domain.model.config.Config;
import com.github.kazuhito_m.mysample.domain.model.config.ConfigRepository;
import com.github.kazuhito_m.mysample.domain.model.config.Datasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceConfig.class);

    private final ConfigRepository configRepository;
    private final ApplicationContext context;

    @Bean("dataSource")
    public DataSource dataSource() {
        Config config = configRepository.get();
        return buildDataSource(config.mainDatasource());
    }

    @Bean("logDataSource")
    public DataSource logDataSource() {
        Config config = configRepository.get();
        return buildDataSource(config.logDatasource());
    }

    private DataSource buildDataSource(Datasource dbConfig) {
        DataSourceBuilder builder = DataSourceBuilder.create();
        builder.driverClassName(dbConfig.driverClassName());
        builder.url(dbConfig.url());
        builder.username(dbConfig.username());
        builder.password(dbConfig.password());
        return builder.build();
    }

    @Bean("logDataSourceInitializer")
    public DataSourceInitializer logDataSourceInitializer(@Qualifier("logDataSource") DataSource dataSource) {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

        Resource resource = context.getResource("classpath:schema-log.sql");
        if (resource != null && resource.exists()) {
            databasePopulator.addScript(resource);
        } else {
            LOGGER.debug("DBスキーマ初期化ファイル:schema-log.sql が見つかりませんでした。初期化は行いません");
        }

        dataSourceInitializer.setDatabasePopulator(databasePopulator);
        dataSourceInitializer.setEnabled(true);
        return dataSourceInitializer;
    }

    public DataSourceConfig(ConfigRepository configRepository, ApplicationContext context) {
        this.configRepository = configRepository;
        this.context = context;
    }
}
