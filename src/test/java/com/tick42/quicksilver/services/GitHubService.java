package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import javax.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.*;
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
    Future<GitHubModel> future;

    @Spy
    @InjectMocks
    private GitHubServiceImpl gitHubService;

    private final CountDownLatch waiter = new CountDownLatch(1);
    private static String token;

    private GitHub connectToGitHub(){
        Settings settings = new Settings();
        settings.setToken(token);

        return gitHubService.connectGithub(settings);
    }

    @BeforeAll
    private static void setToken() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/adminUser.txt"));
        token = br.readLine();
    }

    @Test
    public void connectGitHub() throws IOException {
        GitHub gitHub = connectToGitHub();

        assertTrue(gitHub.isCredentialValid());
    }

    @Test
    public void connectGitHub_WithWrongToken_GitHubException() throws IOException {
        Settings settings = new Settings();
        settings.setToken("invalid");

        GHException thrown = assertThrows(GHException.class,
                () -> gitHubService.connectGithub(settings));

        assertEquals(thrown.getMessage(), "Couldn't connect to github.");
    }

    @Test
    public void setDetails() {
        connectToGitHub();

        GitHubModel gitHubModel = new GitHubModel();
        gitHubModel.setUser("ivaylo9512");
        gitHubModel.setRepo("extension-market-remastered");

        gitHubService.setRemoteDetails(gitHubModel);

        assertTrue(gitHubModel.getLastCommit().isAfter(LocalDateTime.of(2021, Month.SEPTEMBER, 9, 0, 0)));
        verify(gitHubService, times(1)).submitTask(gitHubModel);
        verify(gitHubService, times(1))
                .executeFuture(any(Future.class), eq(gitHubModel), eq(50));
    }

    @Test
    public void setDetailsWithInvalidRepo() {
        connectToGitHub();

        String user = "ivaylo9512";
        String repo = "invalid";
        GitHubModel gitHubModel = new GitHubModel();
        gitHubModel.setUser(user);
        gitHubModel.setRepo(repo);

        GHException thrown = assertThrows(GHException.class,
                () -> gitHubService.setRemoteDetails(gitHubModel));

        String message = String.format("org.kohsuke.github.GHException: Couldn't connect to repo: '%s' with user: '%s'. " +
                        "Check details.", repo, user);
        assertEquals(thrown.getMessage(), message);
        assertEquals(gitHubModel.getFailMessage(), message);
    }

    @Test
    public void executeFutureOnInterruptedException() throws Exception {
        when(future.get(1, TimeUnit.SECONDS)).thenThrow(InterruptedException.class);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> gitHubService.executeFuture(future, new GitHubModel(), 1));

        assertEquals(thrown.getMessage(), "New Settings are set. Current task canceled.");
    }

    @Test
    public void executeFutureOnTimeoutSetFailMessage() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        waiter.await(2, TimeUnit.SECONDS);

        Future<GitHubModel> future = executor.submit(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new GitHubModel();
        });

        doNothing().when(gitHubService).tryGithub(null);

        gitHubService.executeFuture(future, new GitHubModel(),1);
        verify(gitHubService, times(1)).tryGithub(null);
    }

    @Test
    public void extractFromLink(){
        String link = "https://github.com/user/repo";
        GitHubModel gitHub = gitHubService.extractFromLink(link);

        assertEquals(gitHub.getRepo(), "repo");
        assertEquals(gitHub.getUser(), "user");
    }

    @Test
    public void tryGithub(){
        Settings settings = new Settings("ivaylo9512", "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");

        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));
        doReturn(null).when(gitHubService).connectGithub(settings);

        gitHubService.tryGithub(null);

        verify(gitHubService, times(1)).findAvailableSettings(null);
        verify(gitHubService, times(1)).connectGithub(settings);
    }

    @Test
    public void findAvailableSettings_WhenSettingsArePresent(){
        Settings oldSettings = new Settings();
        oldSettings.setId(2);

        Settings settings = new Settings("ivaylo9512", "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");
        settings.setId(3);

        when(settingsRepository.findById(settings.getId())).thenReturn(Optional.of(settings));

        Settings newSettings = gitHubService.findAvailableSettings(oldSettings);

        assertEquals(settings, newSettings);
    }

    @Test
    public void findAvailableSettings_WithNonExistent_ShouldReturnBackToFirst() {
        Settings settings = new Settings("ivaylo9512", "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");
        when(settingsRepository.findById(4L)).thenReturn(Optional.empty());
        when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));

        Settings oldSettings = new Settings();
        oldSettings.setId(3);

        Settings newSettings = gitHubService.findAvailableSettings(oldSettings);

        assertEquals(settings, newSettings);
    }

    @Test
    public void findAvailableSettings_WithNonExistentFirst() {
        when(settingsRepository.findById(4L)).thenReturn(Optional.empty());
        when(settingsRepository.findById(1L)).thenReturn(Optional.empty());

        Settings oldSettings = new Settings();
        oldSettings.setId(3);

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> gitHubService.findAvailableSettings(oldSettings));

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

        List<GitHubModel> gitHubModels = List.of(gitHubModel1, gitHubModel2);

        when(gitHubRepository.findAll()).thenReturn(gitHubModels);
        doNothing().when(gitHubService).setRemoteDetails(any(GitHubModel.class));

        gitHubService.updateGitHubDetails();

        verify(gitHubService, times(1)).setRemoteDetails(gitHubModel1);
        verify(gitHubService, times(1)).setRemoteDetails(gitHubModel2);
        verify(gitHubRepository, times(1)).save(gitHubModel1);
        verify(gitHubRepository, times(1)).save(gitHubModel2);
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
        verify(gitHubRepository, times(1)).save(gitHubModel);
        assertEquals(savedGitHub, gitHubModel);
    }
}
