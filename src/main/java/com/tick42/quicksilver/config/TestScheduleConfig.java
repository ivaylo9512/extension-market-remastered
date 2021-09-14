package com.tick42.quicksilver.config;

import com.tick42.quicksilver.models.specs.SettingsSpec;
import com.tick42.quicksilver.services.base.GitHubService;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "app.schedule")
@Profile("test")
public class TestScheduleConfig implements SchedulingConfigurer {
    @Autowired
    private GitHubService gitHubService;
    private final UserService userService;

    public TestScheduleConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        String token = getTokenFromFile();

        gitHubService.initializeSettings(null, null, new SettingsSpec(token,
                500_000, 5000, "ivaylo9512"));
        gitHubService.connectGithub();
        gitHubService.createScheduledTask(taskRegistrar);
    }

    private String getTokenFromFile() {
        try(BufferedReader br = new BufferedReader(new FileReader("/adminUser.txt"))){
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}