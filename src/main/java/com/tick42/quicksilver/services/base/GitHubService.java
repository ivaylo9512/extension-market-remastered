package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.specs.SettingsSpec;
import com.tick42.quicksilver.models.UserModel;
import org.kohsuke.github.GitHub;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

public interface GitHubService {
    void setRemoteDetails(GitHubModel gitHubModel);

    GitHubModel updateGitHub(long githubId, String githubLink);

    Settings setNextSettings();

    GitHub connectGithub();

    GitHubModel generateGitHub(String link);

    void updateGitHubDetails();

    void createScheduledTask(ScheduledTaskRegistrar taskRegistrar);

    Settings initializeSettings(Settings settings, UserModel user, SettingsSpec settingsSpec);

    Settings getSettings(UserModel user);

    void delete(long id);

    GitHubModel reloadGitHub(GitHubModel gitHubModel, UserModel userModel);

    void updateSettingsOnDelete(long id, ScheduledTaskRegistrar taskRegistrar);
}
