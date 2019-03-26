package com.tick42.quicksilver.config;

import com.tick42.quicksilver.models.*;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class AppConfig {


    @Bean
    public SessionFactory createSessionFactory() {
        return new org.hibernate.cfg.Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Extension.class)
                .addAnnotatedClass(UserModel.class)
                .addAnnotatedClass(Rating.class)
                .addAnnotatedClass(Tag.class)
                .addAnnotatedClass(File.class)
                .addAnnotatedClass(GitHubModel.class)
                .addAnnotatedClass(Settings.class)
                .buildSessionFactory();
    }
}
