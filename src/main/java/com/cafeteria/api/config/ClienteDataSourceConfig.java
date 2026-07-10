package com.cafeteria.api.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Properties;

/**
 * Datasource del PORTAL DE CLIENTES: conecta como CAFETERIA_CLIENTE_APP.
 * Gestiona las entidades de los paquetes menu, cliente, sucursal y pedido.
 * Cualquier intento de tocar un objeto no permitido por ROL_CLIENTE
 * fallará en la BD (ORA-00942/ORA-01031): defensa en profundidad.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.cafeteria.api.menu",
                "com.cafeteria.api.cliente",
                "com.cafeteria.api.sucursal",
                "com.cafeteria.api.pedido"
        },
        entityManagerFactoryRef = "clienteEntityManagerFactory",
        transactionManagerRef = "clienteTransactionManager")
public class ClienteDataSourceConfig {

    @Value("${app.jpa.show-sql:false}")
    private boolean showSql;

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.cliente")
    public HikariDataSource clienteDataSource() {
        return new HikariDataSource();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean clienteEntityManagerFactory() {
        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(clienteDataSource());
        emf.setPackagesToScan(
                "com.cafeteria.api.menu",
                "com.cafeteria.api.cliente",
                "com.cafeteria.api.sucursal",
                "com.cafeteria.api.pedido");
        emf.setPersistenceUnitName("cliente");
        emf.setJpaVendorAdapter(vendorAdapter(showSql));
        emf.setJpaProperties(jpaProperties(showSql));
        return emf;
    }

    @Bean
    @Primary
    public PlatformTransactionManager clienteTransactionManager(
            @Qualifier("clienteEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    static HibernateJpaVendorAdapter vendorAdapter(boolean showSql) {
        var adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(showSql);
        return adapter;
    }

    static Properties jpaProperties(boolean showSql) {
        var props = new Properties();
        // Las tablas/vistas viven en el esquema CAFETERIA_APP
        props.put("hibernate.default_schema", "CAFETERIA_APP");
        props.put("hibernate.format_sql", String.valueOf(showSql));
        return props;
    }
}
