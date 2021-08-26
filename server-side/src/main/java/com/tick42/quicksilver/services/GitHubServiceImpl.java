package com.tick42.quicksilver.services;

import com.tick42.quicksilver.config.Scheduler;
import com.tick42.quicksilver.exceptions.GitHubRepositoryException;
import com.tick42.quicksilver.exceptions.UnauthorizedException;
import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import com.tick42.quicksilver.services.base.GitHubService;
import org.kohsuke.github.*;
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
    private Settings settings;
    private GitHub gitHub;

    public GitHubServiceImpl(GitHubRepository gitHubRepository, Scheduler scheduler, ThreadPoolTaskScheduler threadPoolTaskScheduler, SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
        this.gitHubRepository = gitHubRepository;
        this.scheduler = scheduler;
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    @Override
    public void setRemoteDetails(GitHubModel gitHubModel) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Future<Boolean> future = executor.submit(() -> {
            try {

                GHRepository repo = this.gitHub.getRepository(gitHubModel.getUser() + "/" + gitHubModel.getRepo());

                int pulls = repo.getPullRequests(GHIssueState.OPEN).size();
                int issues = repo.getIssues(GHIssueState.OPEN).size() - pulls;

                LocalDateTime lastCommit = null;
                List<GHCommit> commits = repo.listCommits().asList();
                if (commits.size() > 0) {
                    lastCommit = commits.get(0).getCommitDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                }

                gitHubModel.setPullRequests(pulls);
                gitHubModel.setOpenIssues(issues);
                gitHubModel.setLastCommit(lastCommit);
                gitHubModel.setLastSuccess(LocalDateTime.now());
                return true;
            } catch (GHException e) {
                throw new GitHubRepositoryException("Connected to " + gitHubModel.getLink() + " but couldn't fetch data.");
            } catch (IOException e) {
                throw new GitHubRepositoryException("Couldn't connect to " + gitHubModel.getLink() + ". Check URL.");
            }
        });

        try {
            future.get(50, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            throw new RuntimeException("New Settings are set. Current task canceled.");

        } catch (ExecutionException e){
            e.printStackTrace();
            gitHubModel.setFailMessage(e.getMessage());
            gitHubModel.setLastFail(LocalDateTime.now());

        } catch (TimeoutException e) {
            tryGithub();
        }
    }

    private void tryGithub(){
        settings = settingsRepository.findById(settings.getId() + 1)
                .orElse(settingsRepository.findById(1L)
                        .orElseThrow(() -> new RuntimeException("No settings found.")));
        try {
            gitHub = org.kohsuke.github.GitHub.connect(settings.getUsername(), settings.getToken());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to github.");
        }
    }

    @Override
    public void getRepoDetails(GitHubModel gitHubModel) throws IOException{
        GHRepository repo = this.gitHub.getRepository(gitHubModel.getUser() + "/" + gitHubModel.getRepo());

        int pulls = repo.getPullRequests(GHIssueState.OPEN).size();
        int issues = repo.getIssues(GHIssueState.OPEN).size() - pulls;

        LocalDateTime lastCommit = null;
        List<GHCommit> commits = repo.listCommits().asList();
        if (commits.size() > 0) {
            lastCommit = commits.get(0).getCommitDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        gitHubModel.setPullRequests(pulls);
        gitHubModel.setOpenIssues(issues);
        gitHubModel.setLastCommit(lastCommit);
        gitHubModel.setLastSuccess(LocalDateTime.now());
    }
    @Override
    public GitHubModel generateGitHub(String link) {
        if(link == null){
            return null;
        }

        String[] githubCred = link.replaceAll("https://github.com/", "").split("/");
        String user = githubCred[0];
        String repo = githubCred[1];
        GitHubModel gitHubModel = new GitHubModel(link, user, repo);
        setRemoteDetails(gitHubModel);
        return gitHubModel;
    }

    @Override
    public GitHubModel updateGithub(long githubId, String githubLink) {
        GitHubModel newGitHubModel = generateGitHub(githubLink);
        newGitHubModel.setId(githubId);

        return newGitHubModel;
    }
    @Override
    public void updateExtensionDetails() {
        List<GitHubModel> gitHubModels = gitHubRepository.findAll();
        gitHubModels.forEach(gitHub -> {
            setRemoteDetails(gitHub);
            gitHubRepository.save(gitHub);
        });
    }

    @Override
    public Settings createScheduledTask(UserModel user, ScheduledTaskRegistrar taskRegistrar, GitHubSettingSpec gitHubSettingSpec) {
        settings = settingsRepository.findByUser(user);

        if(settings == null) settings = new Settings();

        if (gitHubSettingSpec != null) {
            Settings newSettings = new Settings(gitHubSettingSpec);

            if(settings.getId() != 0) newSettings.setId(settings.getId());

            newSettings.setUser(user);
            settings = settingsRepository.save(newSettings);
        }

        if (settings.getToken() == null || settings.getUsername() == null) return null;

        try {
            gitHub = org.kohsuke.github.GitHub.connect(settings.getUsername(), settings.getToken());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to github.");
        }


        if (scheduler.getTask() != null) scheduler.getTask().cancel();


        FixedRateTask updateGitHubData = new FixedRateTask(this::updateExtensionDetails, settings.getRate(), settings.getWait());

        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        scheduler.setTask(taskRegistrar.scheduleFixedRateTask(updateGitHubData));

        return settings;
    }

    @Override
    public Settings getSettings(UserModel user) {
        Settings userSettings = settingsRepository.findByUser(user);

        if(userSettings == null) userSettings = new Settings();

        return userSettings;
    }


    @Override
    public GitHubModel fetchGitHub(GitHubModel gitHubModel, UserModel loggedUser) {
        if (!loggedUser.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedException("You are not authorized to trigger a github refresh.");
        }
        setRemoteDetails(gitHubModel);

        return gitHubRepository.save(gitHubModel);
    }

    @Override
    public void delete(GitHubModel gitHubModel){
        gitHubRepository.delete(gitHubModel);
    }
}
