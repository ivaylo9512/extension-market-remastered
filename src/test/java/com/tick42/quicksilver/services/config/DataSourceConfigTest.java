package com.tick42.quicksilver.services.config;

import com.tick42.quicksilver.config.DataSourceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataSourceConfigTest {
    @InjectMocks
    DataSourceConfig dataSourceConfig;

    @Mock
    Environment env;

    @Test()
    public void entityManagerFactory_WithTestProfile() {
        when(env.acceptsProfiles(Profiles.of("test"))).thenReturn(true);

        LocalContainerEntityManagerFactoryBean em = dataSourceConfig.entityManagerFactory();

        assertEquals(em.getJpaPropertyMap().get("hibernate.hbm2ddl.auto"), "create");
    }

    @Test()
    public void entityManagerFactory_WithProductionProfile() {
        when(env.acceptsProfiles(Profiles.of("test"))).thenReturn(false);

        LocalContainerEntityManagerFactoryBean em = dataSourceConfig.entityManagerFactory();

        assertFalse(em.getJpaPropertyMap().containsKey("hibernate.hbm2ddl.auto"));
    }

    @Test
    public void DataSource_WithTestProfile(){
        when(env.acceptsProfiles(Profiles.of("test"))).thenReturn(true);

        DriverManagerDataSource dataSource = (DriverManagerDataSource) dataSourceConfig.dataSource();

        assertEquals(dataSource.getUrl(), "jdbc:mysql://database-2.cdad4jowljyd.eu-central-1.rds.amazonaws.com:3306" +
                "/extensions-market-test?serverTimezone=UTC");
    }

    @Test
    public void DataSource_WithProductionProfile(){
        when(env.acceptsProfiles(Profiles.of("test"))).thenReturn(false);

        DriverManagerDataSource dataSource = (DriverManagerDataSource) dataSourceConfig.dataSource();

        assertEquals(dataSource.getUrl(), "jdbc:mysql://database-2.cdad4jowljyd.eu-central-1.rds.amazonaws.com:3306" +
                "/extensions-market?serverTimezone=UTC");
    }
}
