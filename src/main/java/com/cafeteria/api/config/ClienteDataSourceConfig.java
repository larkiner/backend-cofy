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

    @Value("${app.jpa.default-schema:CAFETERIA_APP}")
    private String defaultSchema;

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
        emf.setJpaProperties(jpaProperties(showSql, defaultSchema));
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

    static Properties jpaProperties(boolean showSql, String defaultSchema) {
        var props = new Properties();
        props.put("hibernate.default_schema", defaultSchema);
        props.put("hibernate.format_sql", String.valueOf(showSql));
        // Agrupa los INSERT/UPDATE del mismo tipo en un solo round-trip JDBC
        // (por ejemplo, las filas de DETALLE_PEDIDO de un mismo pedido) en
        // vez de uno por fila. No cambia cómo se generan los IDs.
        props.put("hibernate.jdbc.batch_size", "20");
        props.put("hibernate.order_inserts", "true");
        props.put("hibernate.order_updates", "true");
        return props;
    }
}
