package com.tick42.quicksilver.services;

import com.tick42.quicksilver.config.Scheduler;
import com.tick42.quicksilver.exceptions.UnauthorizedException;
import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.specs.SettingsSpec;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import com.tick42.quicksilver.services.base.GitHubService;
import org.kohsuke.github.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
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
    private final SettingsRepository settingsRepository;
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
        Future<GitHubModel> future = submitTask(gitHubModel);
        executeFuture(future, gitHubModel, 50);
    }

    public void executeFuture(Future<GitHubModel> future, GitHubModel gitHubModel, int seconds) {
        try {
            future.get(seconds, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            throw new RuntimeException("New Settings are set. Current task canceled.");

        } catch (ExecutionException e){
            gitHubModel.setFailMessage(e.getMessage());
            gitHubModel.setLastFail(LocalDateTime.now());
        } catch (TimeoutException e) {
            tryGithub();
        }
    }

    public Future<GitHubModel> submitTask(GitHubModel gitHubModel) {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        return executor.submit(() -> {
            try {
                GHRepository repo = this.gitHub.getRepository(gitHubModel.getUser() + "/" + gitHubModel.getRepo());

                GHIssueState ghIssueState = GHIssueState.OPEN;
                int pulls = repo.getPullRequests(ghIssueState).size();
                int issues = repo.getIssues(ghIssueState).size() - pulls;

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

                return gitHubModel;
            } catch (IOException e) {
                throw new GHException(String.format("Couldn't connect to repo: '%s'. Check details.", gitHubModel.getRepo()));
            }
        });
    }

    public void tryGithub() {
        setNextSettings();
        connectGithub();
    }

    @Override
    public Settings setNextSettings() {
        long settingsId = settings == null ? 0
                : settings.getId();

        return this.settings = settingsRepository.getNextAvailable(settingsId);
    }

    @Override
    public GitHub connectGithub() {
        try {
            return gitHub = GitHub.connectUsingOAuth(settings.getToken());
        } catch (IOException e) {
            throw new GHException("Couldn't connect to github.");
        }
    }

    @Override
    public GitHubModel generateGitHub(String link) {
        GitHubModel gitHubModel = extractFromLink(link);
        setRemoteDetails(gitHubModel);

        return gitHubModel;
    }

    public GitHubModel extractFromLink(String link) {
        String[] githubCred = link.replaceAll("https://github.com/", "").split("/");
        String user = githubCred[0];
        String repo = githubCred[1];

        return new GitHubModel(user, repo);
    }

    @Override
    public GitHubModel updateGitHub(long githubId, String githubLink) {
        GitHubModel newGitHubModel = generateGitHub(githubLink);
        newGitHubModel.setId(githubId);

        return newGitHubModel;
    }

    @Override
    public void updateGitHubDetails() {
        List<GitHubModel> gitHubModels = gitHubRepository.findAll();
        gitHubModels.forEach(gitHub -> {
            setRemoteDetails(gitHub);
            gitHubRepository.save(gitHub);
        });
    }

    @Override
    public void createScheduledTask(ScheduledTaskRegistrar taskRegistrar) {
        if (scheduler.getTask() != null) scheduler.getTask().cancel();

        FixedRateTask updateGitHubData = new FixedRateTask(this::updateGitHubDetails, settings.getRate(), settings.getWait());

        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        scheduler.setTask(taskRegistrar.scheduleFixedRateTask(updateGitHubData));
    }

    @Override
    public Settings initializeSettings(Settings settings, UserModel user, SettingsSpec settingsSpec){
        if (settingsSpec != null) {
            long id = settings == null ? 0 : settings.getId();
            settings = new Settings(settingsSpec, user, id);
            settingsRepository.save(settings);
        }

        this.settings = settings;
        return settings;
    }

    @Override
    public Settings getSettings(UserModel user) {
        return settingsRepository.findByUser(user)
                .orElse(new Settings());
    }

    @Override
    public GitHubModel reloadGitHub(GitHubModel gitHubModel, UserModel loggedUser) {
        setRemoteDetails(gitHubModel);
        return gitHubRepository.save(gitHubModel);
    }

    @Override
    public void delete(long id){
        if(id == 1){
            throw new UnauthorizedException("Deleting master admin is not allowed.");
        }
        Settings settings = settingsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("GitHub not found"));
        settingsRepository.delete(settings);
    }

    @Override
    public void updateSettingsOnDelete(long id, ScheduledTaskRegistrar taskRegistrar){
        if(id != settings.getId()){
            return;
        }

        setNextSettings();
        connectGithub();
        createScheduledTask(taskRegistrar);
    }
}
