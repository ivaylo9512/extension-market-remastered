package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.GitHub;
import com.tick42.quicksilver.models.Spec.GitHubSettingSpec;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

public interface GitHubService {
    void setRemoteDetails(GitHub gitHub);

    void getRepoDetails(GitHub gitHub) throws Exception;

    GitHub updateGithub(int githubId, String githubLink);

    GitHub generateGitHub(String link);

    void updateExtensionDetails();

    void createScheduledTask(int userId, ScheduledTaskRegistrar taskRegistrar, GitHubSettingSpec gitHubSettingSpec);

    GitHubSettingSpec getSettings(int userId);

    void delete(GitHub gitHub);

    GitHub fetchGitHub(GitHub gitHub, UserModel userModel);

}
