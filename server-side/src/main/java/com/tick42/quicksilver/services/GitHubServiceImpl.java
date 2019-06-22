package com.tick42.quicksilver.services;

import com.tick42.quicksilver.config.Scheduler;
import com.tick42.quicksilver.exceptions.GitHubRepositoryException;
import com.tick42.quicksilver.exceptions.UnauthorizedExtensionModificationException;
import com.tick42.quicksilver.models.GitHub;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.Spec.GitHubSettingSpec;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.GitHubService;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.*;

@Service
public class GitHubServiceImpl implements GitHubService {

    private final GitHubRepository gitHubRepository;
    private final Scheduler scheduler;
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private SettingsRepository settingsRepository;
    private UserRepository userRepository;
    private Settings settings;
    private org.kohsuke.github.GitHub gitHub;



    @Autowired
    public GitHubServiceImpl(GitHubRepository gitHubRepository, Scheduler scheduler, ThreadPoolTaskScheduler threadPoolTaskScheduler, SettingsRepository settingsRepository, UserRepository userRepository) throws IOException {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.gitHubRepository = gitHubRepository;
        this.scheduler = scheduler;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    @Override
    public void setRemoteDetails(GitHub gitHub) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Future<Boolean> future = executor.submit(() -> {
            try {

                GHRepository repo = this.gitHub.getRepository(gitHub.getUser() + "/" + gitHub.getRepo());

                int pulls = repo.getPullRequests(GHIssueState.OPEN).size();
                int issues = repo.getIssues(GHIssueState.OPEN).size() - pulls;

                LocalDateTime lastCommit = null;
                List<GHCommit> commits = repo.listCommits().asList();
                if (commits.size() > 0) {
                    lastCommit = commits.get(0).getCommitDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                }

                gitHub.setPullRequests(pulls);
                gitHub.setOpenIssues(issues);
                gitHub.setLastCommit(lastCommit);
                gitHub.setLastSuccess(LocalDateTime.now());
                return true;
            } catch (GHException e) {
                throw new GitHubRepositoryException("Connected to " + gitHub.getLink() + " but couldn't fetch data.");
            } catch (IOException e) {
                throw new GitHubRepositoryException("Couldn't connect to " + gitHub.getLink() + ". Check URL.");
            }
        });

        try {
            future.get(50, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            throw new RuntimeException("New Settings are set. Current task canceled.");

        } catch (ExecutionException e){
            e.printStackTrace();
            gitHub.setFailMessage(e.getMessage());
            gitHub.setLastFail(LocalDateTime.now());

        } catch (TimeoutException e) {
            settings = settingsRepository.findById(settings.getId() + 1)
                    .orElse(settingsRepository.findById(1)
                            .orElseThrow(() -> new RuntimeException("No settings found.")));
        }
    }

    @Override
    public void getRepoDetails(GitHub gitHub) throws IOException{
        GHRepository repo = this.gitHub.getRepository(gitHub.getUser() + "/" + gitHub.getRepo());

        int pulls = repo.getPullRequests(GHIssueState.OPEN).size();
        int issues = repo.getIssues(GHIssueState.OPEN).size() - pulls;

        LocalDateTime lastCommit = null;
        List<GHCommit> commits = repo.listCommits().asList();
        if (commits.size() > 0) {
            lastCommit = commits.get(0).getCommitDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        gitHub.setPullRequests(pulls);
        gitHub.setOpenIssues(issues);
        gitHub.setLastCommit(lastCommit);
        gitHub.setLastSuccess(LocalDateTime.now());
    }
    @Override
    public GitHub generateGitHub(String link) {
        String[] githubCred = link.replaceAll("https://github.com/", "").split("/");
        String user = githubCred[0];
        String repo = githubCred[1];
        GitHub gitHub = new GitHub(link, user, repo);
        setRemoteDetails(gitHub);
        return gitHub;
    }

    @Override
    public GitHub updateGithub(int githubId, String githubLink) {
        GitHub newGitHub = generateGitHub(githubLink);
        newGitHub.setId(githubId);

        return newGitHub;
    }
    @Override
    public void updateExtensionDetails() {
        List<GitHub> gitHubs = gitHubRepository.findAll();
        gitHubs.forEach(gitHub -> {
            setRemoteDetails(gitHub);
            gitHubRepository.save(gitHub);
        });
    }

    @Override
    public void createScheduledTask(int userId, ScheduledTaskRegistrar taskRegistrar, GitHubSettingSpec gitHubSettingSpec) {

        settings = settingsRepository.findById(userId).orElse(new Settings());

        if (gitHubSettingSpec != null) {
            Settings newSettings = new Settings(gitHubSettingSpec);

            if(settings.getId() != null){
                newSettings.setId(settings.getId());
             }
            newSettings.setUser(userRepository.getOne(userId));
            settings = settingsRepository.save(newSettings);
        }

        if (settings.getToken() == null || settings.getUsername() == null) return;

        try {
            gitHub = org.kohsuke.github.GitHub.connect(settings.getUsername(), settings.getToken());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to github.");
        }


        if (scheduler.getTask() != null) {
            scheduler.getTask().cancel();
        }

        FixedRateTask updateGitHubData = new FixedRateTask(this::updateExtensionDetails, settings.getRate(), settings.getWait());

        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        scheduler.setTask(taskRegistrar.scheduleFixedRateTask(updateGitHubData));
    }

    @Override
    public GitHubSettingSpec getSettings(int userId) {
        Settings userSettings = settingsRepository.findByUser(userRepository.getOne(userId));
        GitHubSettingSpec currentSettings = new GitHubSettingSpec();
        if(userSettings != null){
            currentSettings.setToken(userSettings.getToken());
            currentSettings.setUsername(userSettings.getUsername());
        }
        currentSettings.setRate(settings.getRate());
        currentSettings.setWait(settings.getWait());
        return currentSettings;
    }


    @Override
    public GitHub fetchGitHub(GitHub gitHub, UserModel loggedUser) {

        if (!loggedUser.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedExtensionModificationException("You are not authorized to trigger a github refresh.");
        }
        setRemoteDetails(gitHub);

        return gitHubRepository.save(gitHub);
    }

    @Override
    public void delete(GitHub gitHub){
        gitHubRepository.delete(gitHub);
    }
}
