package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.persistence.EntityNotFoundException;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GitHubService {
    @Mock
    GitHubRepository gitHubRepository;

    @Mock
    SettingsRepository settingsRepository;

    @Mock
    Future<Boolean> future;

    @Spy
    @InjectMocks
    private GitHubServiceImpl gitHubService;

    private final CountDownLatch waiter = new CountDownLatch(1);
    private final LocalDateTime dateTime = LocalDateTime.of(2021, Month.SEPTEMBER, 9, 9, 9);

    @Test
    public void executeFutureOnInterruptedException() throws Exception {
        when(future.get(1, TimeUnit.SECONDS)).thenThrow(InterruptedException.class);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> gitHubService.executeFuture(future, new GitHubModel(), 1, dateTime));

        assertEquals(thrown.getMessage(), "New Settings are set. Current task canceled.");
    }

    @Test
    public void executeFutureOnTimeoutSetFailMessage() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        waiter.await(2, TimeUnit.SECONDS);

        Future<Boolean> future = executor.submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        });

        doNothing().when(gitHubService).tryGithub(null);

        gitHubService.executeFuture(future, new GitHubModel(),1, dateTime);
        verify(gitHubService, times(1)).tryGithub(null);
    }

    @Test
    public void executeFutureOnSetMessageOnExecutionException() throws Exception {
        GitHubModel gitHubModel = new GitHubModel();

        when(future.get(1, TimeUnit.SECONDS)).thenThrow(new ExecutionException(
                new RuntimeException("Github error.")));

        gitHubService.executeFuture(future, gitHubModel, 1, dateTime);

        assertEquals(gitHubModel.getFailMessage(), "java.lang.RuntimeException: Github error.");
        assertEquals(gitHubModel.getLastFail(), dateTime);
    }

    @Test
    public void setRemoteDetails(){
        GitHubModel gitHubModel = new GitHubModel();

        when(gitHubService.submitTask(gitHubModel)).thenReturn(future);
        doNothing().when(gitHubService).executeFuture(eq(future), eq(gitHubModel), eq(50), any(LocalDateTime.class));

        gitHubService.setRemoteDetails(gitHubModel);

        verify(gitHubService, times(1)).submitTask(gitHubModel);
        verify(gitHubService, times(1))
                .executeFuture(eq(future), eq(gitHubModel), eq(50), any(LocalDateTime.class));
    }

    @Test
    public void extractFromLink(){
        String link = "https://github.com/user/repo";
        GitHubModel gitHub = gitHubService.extractFromLink(link);

        assertEquals(gitHub.getRepo(), "repo");
        assertEquals(gitHub.getUser(), "user");
        assertEquals(gitHub.getLink(), link);
    }

    @Test
    public void tryGithub(){
        Settings settings = new Settings("ivaylo9512", "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");
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));

        gitHubService.tryGithub(null);

        try (MockedStatic<GitHub> mocked = mockStatic(GitHub.class)) {
            gitHubService.tryGithub(settings);

            mocked.verify(() -> GitHub.connect(settings.getUsername(), settings.getToken()), times(1));
        }
    }

    @Test
    public void tryGithub_WhenSettingsArePresent(){
        Settings oldSettings = new Settings();
        oldSettings.setId(2);

        Settings settings = new Settings("ivaylo9512", "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");
        settings.setId(3);

        when(settingsRepository.findById(settings.getId())).thenReturn(Optional.of(settings));

        try (MockedStatic<GitHub> mocked = mockStatic(GitHub.class)) {
            gitHubService.tryGithub(oldSettings);

            mocked.verify(() -> GitHub.connect(settings.getUsername(), settings.getToken()), times(1));
        }
    }

    @Test
    public void tryGithub_WithNonExistent_ShouldReturnBackToFirst() {
        Settings settings = new Settings("ivaylo9512", "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");
        when(settingsRepository.findById(4L)).thenReturn(Optional.empty());
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));

        Settings oldSettings = new Settings();
        oldSettings.setId(3);

        try (MockedStatic<GitHub> mocked = mockStatic(GitHub.class)) {
            gitHubService.tryGithub(oldSettings);

            mocked.verify(() -> GitHub.connect(settings.getUsername(), settings.getToken()), times(1));
        }
    }

    @Test
    public void tryGithub_WithNonExistentFirst() {
        when(settingsRepository.findById(4L)).thenReturn(Optional.empty());
        when(settingsRepository.findById(1L)).thenReturn(Optional.empty());

        Settings oldSettings = new Settings();
        oldSettings.setId(3);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> gitHubService.tryGithub(oldSettings));

        assertEquals(thrown.getMessage(), "Settings not found.");
    }

    @Test
    public void updateGitHubDetails() {
        GitHubModel gitHubModel1 = new GitHubModel();
        GitHubModel gitHubModel2 = new GitHubModel();
        gitHubModel1.setUser("username");
        gitHubModel1.setRepo("repo");
        gitHubModel2.setUser("username1");
        gitHubModel2.setRepo("repo1");
        List<GitHubModel> gitHubModels = Arrays.asList(gitHubModel1, gitHubModel2);

        when(gitHubRepository.findAll()).thenReturn(gitHubModels);
        doNothing().when(gitHubService).setRemoteDetails(any(GitHubModel.class));

        gitHubService.updateGitHubDetails();

        verify(gitHubRepository, times(2)).save(isA(GitHubModel.class));
        verify(gitHubService, times(1)).setRemoteDetails(gitHubModel1);
        verify(gitHubService, times(1)).setRemoteDetails(gitHubModel2);
    }

    @Test
    public void generateGitHub(){
        GitHubModel gitHubModel = new GitHubModel();

        when(gitHubService.extractFromLink("https://github.com/user/repo")).thenReturn(gitHubModel);
        doNothing().when(gitHubService).setRemoteDetails(gitHubModel);

        gitHubService.generateGitHub("https://github.com/user/repo");

        verify(gitHubService, times(1)).extractFromLink("https://github.com/user/repo");
        verify(gitHubService, times(1)).setRemoteDetails(gitHubModel);
    }

    @Test
    public void updateGitHub(){
        GitHubModel gitHubModel = new GitHubModel();

        when(gitHubService.extractFromLink("https://github.com/user/repo")).thenReturn(gitHubModel);
        doNothing().when(gitHubService).setRemoteDetails(gitHubModel);

        gitHubService.updateGitHub(2, "https://github.com/user/repo");

        assertEquals(gitHubModel.getId(), 2);
        verify(gitHubService, times(1)).generateGitHub("https://github.com/user/repo");
    }

    @Test
    public void initializeSettings(){
        UserModel oldUser = new UserModel();
        UserModel newUser = new UserModel();
        oldUser.setId(1);
        newUser.setId(2);

        Settings settings = new Settings(20, 30, 40, "token", "username");
        settings.setUser(oldUser);
        GitHubSettingSpec gitHubSettingSpec = new GitHubSettingSpec("newToken", 50, 60, "newUsername");

        Settings newSettings = gitHubService.initializeSettings(settings, newUser, gitHubSettingSpec);

        assertEquals(newSettings.getId(), 20);
        assertEquals(newSettings.getRate(), 50);
        assertEquals(newSettings.getWait(), 60);
        assertEquals(newSettings.getUsername(), "newUsername");
        assertEquals(newSettings.getUser(), newUser);
    }

    @Test
    public void initializeSettingsWhenSettingsIsNull(){
        UserModel oldUser = new UserModel();
        UserModel newUser = new UserModel();
        oldUser.setId(1);
        newUser.setId(2);

        GitHubSettingSpec gitHubSettingSpec = new GitHubSettingSpec("newToken", 50, 60, "newUsername");

        Settings newSettings = gitHubService.initializeSettings(null, newUser, gitHubSettingSpec);

        assertEquals(newSettings.getId(), 0);
        assertEquals(newSettings.getRate(), 50);
        assertEquals(newSettings.getWait(), 60);
        assertEquals(newSettings.getUsername(), "newUsername");
        assertEquals(newSettings.getUser(), newUser);
    }

    @Test
    public void initializeSettingsWhenSettingsSpecIsNull(){
        UserModel oldUser = new UserModel();
        UserModel newUser = new UserModel();
        oldUser.setId(1);
        newUser.setId(2);

        Settings settings = new Settings(20, 30, 40, "token", "username");
        settings.setUser(oldUser);

        Settings newSettings = gitHubService.initializeSettings(settings, newUser, null);

        assertEquals(settings, newSettings);
    }

    @Test
    public void getSettings_WhenSettingsDoNotExist(){
        UserModel userModel = new UserModel();

        when(settingsRepository.findByUser(userModel)).thenReturn(Optional.empty());

        Settings foundSettings = gitHubService.getSettings(userModel);

        assertEquals(foundSettings.getId(), 0);
        assertEquals(foundSettings.getWait(), 0);
        assertEquals(foundSettings.getRate(), 0);
    }

    @Test
    public void getSettings(){
        UserModel userModel = new UserModel();
        Settings settings = new Settings("username", "token");
        settings.setId(2);

        when(settingsRepository.findByUser(userModel)).thenReturn(Optional.of(settings));

        Settings foundSettings = gitHubService.getSettings(userModel);

        assertEquals(settings, foundSettings);
    }

    @Test
    public void reloadGitHub(){
        GitHubModel gitHubModel = new GitHubModel();
        UserModel loggedUser = new UserModel();
        loggedUser.setRole("ROLE_ADMIN");

        when(gitHubRepository.save(gitHubModel)).thenReturn(gitHubModel);
        doNothing().when(gitHubService).setRemoteDetails(gitHubModel);

        GitHubModel savedGitHub = gitHubService.reloadGitHub(gitHubModel, loggedUser);

        verify(gitHubService, times(1)).setRemoteDetails(gitHubModel);
        assertEquals(savedGitHub, gitHubModel);
    }
}
