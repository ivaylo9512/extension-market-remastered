package com.tick42.quicksilver.config;

import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.services.base.GitHubService;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@ConfigurationProperties(prefix = "app.schedule")
@Profile("production")
public class ScheduleConfig implements SchedulingConfigurer {
    @Autowired
    private GitHubService gitHubService;
    private final UserService userService;

    public ScheduleConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        UserModel admin = userService.findById(1, null);

        gitHubService.initializeSettings(admin.getGitHubSettings(), admin, null);
        gitHubService.createScheduledTask(taskRegistrar);
    }
}