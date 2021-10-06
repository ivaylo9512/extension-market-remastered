package com.tick42.quicksilver.config;

import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.GitHubService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Configuration
@ConfigurationProperties(prefix = "app.schedule")
public class ScheduleConfig implements SchedulingConfigurer {
    private final GitHubService gitHubService;
    private final UserRepository userRepository;

    private final Logger log = LogManager.getLogger();

    public ScheduleConfig(UserRepository userRepository, GitHubService gitHubService) {
        this.userRepository = userRepository;
        this.gitHubService = gitHubService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        UserModel admin = userRepository.findById(1L)
                .orElseGet(this::initializeUser);

        gitHubService.initializeSettings(admin.getGitHubSettings(), admin, null);
        gitHubService.connectGithub();
        gitHubService.createScheduledTask(taskRegistrar);
    }

    public UserModel initializeUser() {
        String[] dockerFile = getTokenFromFile("/adminUser.txt");
        String token = dockerFile[0];
        String password = dockerFile[1];

        UserModel user = new UserModel();
        user.setId(1);
        user.setUsername("admin9512");
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt(4)));
        user.setGitHubSettings(new Settings(1, 50_0000, 5000,
                token, "ivaylo9512"));

        return user;
    }

    public String[] getTokenFromFile(String url) {
        try(BufferedReader br = new BufferedReader(new FileReader(url))){
            return new String[]{br.readLine(), br.readLine()};
        } catch (IOException e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }
}