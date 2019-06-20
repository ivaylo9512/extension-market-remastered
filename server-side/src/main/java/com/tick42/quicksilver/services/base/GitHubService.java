package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Spec.GitHubSettingSpec;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.io.IOException;

public interface GitHubService {
    void setRemoteDetails(GitHubModel gitHubModel);

    void getRepoDetails(GitHubModel gitHubModel) throws Exception;

    GitHubModel generateGitHub(String link);

    void updateExtensionDetails();

    void createScheduledTask(int userId, ScheduledTaskRegistrar taskRegistrar, GitHubSettingSpec gitHubSettingSpec);

    GitHubSettingSpec getSettings(int userId);

    void delete(GitHubModel gitHub);

    GitHubModel fetchGitHub(GitHubModel gitHub, UserModel userModel);

}
