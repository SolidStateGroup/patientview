package org.patientview.persistence.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.patientview.config.CommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by james@solidstategroup.com Created on 03/06/2014
 */
@Configuration
@EnableJpaRepositories(basePackages = {"org.patientview.persistence.repository"})
@EnableTransactionManagement
public class PersistenceConfig extends CommonConfig {

    private final Logger LOG = LoggerFactory.getLogger(PersistenceConfig.class);

    @Inject
    private Properties properties;

    @PostConstruct
    public void init() {
        properties = propertiesBean();
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        //properties.setProperty("hibernate.show_sql", "true"); // uncomment for sql debug
        //properties.setProperty("hibernate.dialect", "org.patientview.persistence.dialect.PostgresCustomDialect");

        //properties.setProperty("hibernate.ddl-auto", "none"); // using fly away script
        // The SQL dialect makes Hibernate generate better SQL for the chosen database
        properties.setProperty("jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("jpa.properties.hibernate.temp.use_jdbc_metadata_defaults", "false");
        properties.setProperty("jpa.properties.jdbc.lob.non_contextual_creation", "true");
    }

//    @Bean(name = "flyway")
//    public Flyway flyway() throws IOException {
//        Flyway flyway = Flyway.configure().dataSource(patientViewDataSource()).load();
//        flyway.repair(); // repair each script checksum
//        flyway.migrate();
//        return flyway;
//    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        // NOTE: make sure it FALSE as we are using flyway to init schema and db changes
        vendorAdapter.setGenerateDdl(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("org.patientview.persistence");
        factory.setDataSource(patientViewDataSource());
        factory.setJpaProperties(properties);
        factory.afterPropertiesSet();

        return factory.getObject();
    }

    @Bean
    @Primary
    public DataSourceProperties pvDataSourceProperties() {

        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setUrl(properties.getProperty("pv.url"));
        dataSourceProperties.setUsername(properties.getProperty("pv.user"));
        dataSourceProperties.setPassword(properties.getProperty("pv.password"));
        // dataSourceProperties.setDriverClassName("org.postgresql.Driver"); // should be resolved by Hikari
        dataSourceProperties.setType(HikariDataSource.class);

        return dataSourceProperties;
    }

    @Bean(name = "patientView")
    @Primary
    @ConfigurationProperties(prefix = "patientview.datasource.hikari")
    public HikariDataSource patientViewDataSource() {
        LOG.info("Initializing PV datasource ");
        return pvDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "fhir")
    public HikariDataSource fhirDataSource() {
        LOG.info("Initializing PV Fhir datasource ");

        // https://github.com/brettwooldridge/HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("fhir.url"));
        config.setUsername(properties.getProperty("fhir.user"));
        config.setPassword(properties.getProperty("fhir.password"));
        config.setLeakDetectionThreshold(30000); // 30 seconds
        // config.setDriverClassName("org.postgresql.Driver"); //set this if not found by Hikari

        config.setPoolName("PatientViewFhirHikariCP");
        config.setMinimumIdle(20);
        config.setMaximumPoolSize(80);
        config.setIdleTimeout(600000); // 10 min
        config.setMaxLifetime(1800000); // 30 min
        config.setConnectionTimeout(30000); // 30 seconds
        config.addDataSourceProperty("cachePrepStmts", "false");
        //  config.addDataSourceProperty("prepStmtCacheSize", "250");
        // config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

}
