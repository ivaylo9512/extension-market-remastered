package com.tick42.quicksilver.config;

import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
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
        UserModel admin = new UserModel();
        admin.setUsername("admin");
        userService.save(admin);

        gitHubService.initializeSettings(null, null, new GitHubSettingSpec("2ee74795fbdb976f7c03701af532d52a35afa5ad",
                500_000, 5000, "ivaylo9512"));
        gitHubService.createScheduledTask(taskRegistrar);
    }
}