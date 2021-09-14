package com.tick42.quicksilver.config;

import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Configuration
@ConfigurationProperties(prefix = "app.schedule")
public class ScheduleConfig implements SchedulingConfigurer {
    @Autowired
    private GitHubService gitHubService;

    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;
    private final Environment env;

    public ScheduleConfig(UserRepository userRepository, SettingsRepository settingsRepository, Environment env) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.env = env;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        UserModel admin = userRepository.findById(1L)
                .orElseGet(this::initializeUser);

        gitHubService.initializeSettings(admin.getGitHubSettings(), admin, null);
        gitHubService.connectGithub();
        gitHubService.createScheduledTask(taskRegistrar);
    }

    private UserModel initializeUser() {
        String[] dockerFile = getTokenFromFile();
        String token = dockerFile[0];
        String password = dockerFile[1];

        UserModel user = new UserModel();
        user.setId(1);
        user.setUsername("admin12345");
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(4)));
        user.setGitHubSettings(new Settings(1, 50_0000, 5000,
                token, "ivailo9512"));

        return user;
    }

    private String[] getTokenFromFile() {
        try(BufferedReader br = new BufferedReader(new FileReader("/adminUser.txt"))){
            return new String[]{br.readLine(), br.readLine()};
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}