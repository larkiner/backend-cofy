package com.cafeteria.api.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Datasource del PANEL INTERNO: conecta como CAFETERIA_EMPLEADO_APP
 * (tiene ROL_EMPLEADO + ROL_SUPERVISOR). Gestiona las entidades del
 * paquete interno: trabajadores, pedidos en curso, pagos, turnos,
 * métricas e inventario.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.cafeteria.api.interno",
        entityManagerFactoryRef = "empleadoEntityManagerFactory",
        transactionManagerRef = "empleadoTransactionManager")
public class EmpleadoDataSourceConfig {

    @Value("${app.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${app.jpa.default-schema:CAFETERIA_APP}")
    private String defaultSchema;

    @Bean
    @ConfigurationProperties("app.datasource.empleado")
    public HikariDataSource empleadoDataSource() {
        return new HikariDataSource();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean empleadoEntityManagerFactory() {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(empleadoDataSource());
        emf.setPackagesToScan("com.cafeteria.api.interno");
        emf.setPersistenceUnitName("empleado");
        emf.setJpaVendorAdapter(ClienteDataSourceConfig.vendorAdapter(showSql));
        emf.setJpaProperties(ClienteDataSourceConfig.jpaProperties(showSql, defaultSchema));
        return emf;
    }

    @Bean
    public PlatformTransactionManager empleadoTransactionManager(
            @Qualifier("empleadoEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
