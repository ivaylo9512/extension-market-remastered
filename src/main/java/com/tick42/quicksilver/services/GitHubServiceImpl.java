package com.tick42.quicksilver.services;

import com.tick42.quicksilver.config.Scheduler;
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
            throw new GHException(e.getMessage());

        } catch (TimeoutException e) {
            tryGithub(settings);
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
            } catch (GHException e) {
                throw new GHException(String.format("Connected to repo: '%s' with user: '%s' but couldn't fetch data.", gitHubModel.getRepo(), gitHubModel.getUser()));
            } catch (IOException e) {
                throw new GHException(String.format("Couldn't connect to repo: '%s' with user: '%s'. Check details.", gitHubModel.getRepo(), gitHubModel.getUser()));
            }
        });
    }

    public void tryGithub(Settings settings) {
        settings = findAvailableSettings(settings);
        connectGithub(settings);
    }

    public Settings findAvailableSettings(Settings settings) {
        long settingsId = settings == null ? 1
                : settings.getId() + 1;

        return this.settings = settingsRepository.findById(settingsId)
                .or(() -> settingsRepository.findById(1L))
                .orElseThrow(() -> new EntityNotFoundException("Settings not found."));
    }

    public GitHub connectGithub(Settings settings) {
        try {
            return gitHub = GitHub.connectUsingOAuth(settings.getToken());
        } catch (IOException e) {
            throw new GHException("Couldn't connect to github.");
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
        try {
            gitHub = GitHub.connectUsingOAuth(settings.getToken());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to github.");
        }

        if (scheduler.getTask() != null) scheduler.getTask().cancel();

        FixedRateTask updateGitHubData = new FixedRateTask(this::updateGitHubDetails, settings.getRate(), settings.getWait());

        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
        scheduler.setTask(taskRegistrar.scheduleFixedRateTask(updateGitHubData));
    }

    @Override
    public Settings initializeSettings(Settings settings, UserModel user, GitHubSettingSpec gitHubSettingSpec){
        if (gitHubSettingSpec != null) {
            long id = settings == null ? 0 : settings.getId();
            settings = new Settings(gitHubSettingSpec, user, id);
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
    public void delete(GitHubModel gitHubModel){
        gitHubRepository.delete(gitHubModel);
    }
}
