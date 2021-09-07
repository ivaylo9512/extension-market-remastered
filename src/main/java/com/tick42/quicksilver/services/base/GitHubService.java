package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

public interface GitHubService {
    void setRemoteDetails(GitHubModel gitHubModel);

    void getRepoDetails(GitHubModel gitHubModel) throws Exception;

    GitHubModel updateGithub(long githubId, String githubLink);

    GitHubModel generateGitHub(String link);

    void updateExtensionDetails();

    Settings createScheduledTask(ScheduledTaskRegistrar taskRegistrar);

    void initializeSettings(Settings settings, UserModel user, GitHubSettingSpec gitHubSettingSpec);

    Settings getSettings(UserModel user);

    void delete(GitHubModel gitHubModel);

    GitHubModel fetchGitHub(GitHubModel gitHubModel, UserModel userModel);

}
