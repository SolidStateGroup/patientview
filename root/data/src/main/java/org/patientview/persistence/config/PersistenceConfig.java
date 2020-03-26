package org.patientview.persistence.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.patientview.config.CommonConfig;
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
import java.util.Properties;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Configuration
@EnableJpaRepositories(basePackages = {"org.patientview.persistence.repository"})
@EnableTransactionManagement
public class PersistenceConfig extends CommonConfig {

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
        dataSourceProperties.setDriverClassName("org.postgresql.Driver");
        dataSourceProperties.setType(HikariDataSource.class);

        return dataSourceProperties;
    }

    @Bean(name = "patientView")
    @Primary
    @ConfigurationProperties(prefix = "patientview.datasource.hikari")
    public HikariDataSource patientViewDataSource() {
//        BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setMaxTotal(50);
//        dataSource.setDriverClassName("org.postgresql.Driver");
//        dataSource.setUrl(properties.getProperty("pv.url"));
//        dataSource.setUsername(properties.getProperty("pv.user"));
//        dataSource.setPassword(properties.getProperty("pv.password"));
//        return dataSource;
        return pvDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "fhir")
    public BasicDataSource fhirDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setMaxTotal(50);
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(properties.getProperty("fhir.url"));
        dataSource.setUsername(properties.getProperty("fhir.user"));
        dataSource.setPassword(properties.getProperty("fhir.password"));
        return dataSource;
    }

    @Bean(name = "patientView1")
    public BasicDataSource patientView1DataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setMaxTotal(50);
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(properties.getProperty("pv1.url"));
        dataSource.setUsername(properties.getProperty("pv1.user"));
        dataSource.setPassword(properties.getProperty("pv1.password"));
        return dataSource;
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
