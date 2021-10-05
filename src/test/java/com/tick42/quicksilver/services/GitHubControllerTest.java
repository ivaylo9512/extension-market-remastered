package com.tick42.quicksilver.services;

import com.tick42.quicksilver.controllers.GitHubController;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.GitHubDto;
import com.tick42.quicksilver.models.Dtos.SettingsDto;
import com.tick42.quicksilver.models.specs.SettingsSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GitHubControllerTest {
    @InjectMocks
    private GitHubController gitHubController;

    @Mock
    private GitHubServiceImpl gitHubService;

    @Mock
    private ExtensionServiceImpl extensionService;

    @Mock
    private UserServiceImpl userService;

    private final ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
    private final UserModel userModel = new UserModel(1, "username", "email", "password", "ROLE_ADMIN", "info", "Bulgaria");
    private final UserDetails user = new UserDetails(userModel, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    private final Settings settings = new Settings(1, 5000, 5000, "token", "username");
    private final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getId());

    private void assertGitHub(SettingsDto settingsDto, Settings settings){
        assertEquals(settingsDto.getId(), settings.getId());
        assertEquals(settingsDto.getRate(), settings.getRate());
        assertEquals(settingsDto.getWait(), settings.getWait());
        assertEquals(settingsDto.getUsername(), settings.getUsername());
        assertEquals(settingsDto.getToken(), settings.getToken());
    }

    @Test
    public void setNextSettings(){
        Settings settings = new Settings(1, 5000, 5000, "token", "username");

        when(gitHubService.setNextSettings()).thenReturn(settings);
        doNothing().when(gitHubService).createScheduledTask(taskRegistrar);

        SettingsDto settingsDto = gitHubController.setNextSettings(taskRegistrar);

        verify(gitHubService, times(1)).createScheduledTask(taskRegistrar);
        verify(gitHubService, times(1)).connectGithub();

        assertEquals(settingsDto.getId(), settings.getId());
        assertEquals(settingsDto.getRate(), settings.getRate());
        assertEquals(settingsDto.getToken(), settings.getToken());
        assertEquals(settingsDto.getUsername(), settings.getUsername());
    }

    @Test
    public void delete(){
        doNothing().when(gitHubService).delete(1L);
        doNothing().when(gitHubService).updateSettingsOnDelete(1L, taskRegistrar);

        gitHubController.delete(taskRegistrar, 1L);

        verify(gitHubService, times(1)).delete(1L);
        verify(gitHubService, times(1)).updateSettingsOnDelete(1L, taskRegistrar);
    }

    @Test
    public void reloadGitHubData(){
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        GitHubModel gitHubModel = new GitHubModel();
        gitHubModel.setId(1);
        gitHubModel.setRepo("repo");
        gitHubModel.setUser("user");
        gitHubModel.setPullRequests(2);
        gitHubModel.setOpenIssues(5);

        Extension extension = new Extension();
        extension.setGithub(gitHubModel);

        when(extensionService.findById(2L, user)).thenReturn(extension);
        when(userService.findById(1L, user)).thenReturn(userModel);
        when(gitHubService.reloadGitHub(gitHubModel, userModel)).thenReturn(gitHubModel);
        when(gitHubService.reloadGitHub(gitHubModel, userModel)).thenReturn(gitHubModel);

        GitHubDto gitHubDto = gitHubController.reloadGitHubData(2L);

        assertEquals(gitHubModel.getId(), gitHubDto.getId());
        assertEquals(gitHubModel.getUser(), gitHubDto.getUser());
        assertEquals(gitHubModel.getRepo(), gitHubDto.getRepo());
        assertEquals(gitHubModel.getPullRequests(), gitHubDto.getPullRequests());
        assertEquals(gitHubModel.getOpenIssues(), gitHubDto.getOpenIssues());
    }

    @Test
    public void getGitHubSetting() {
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userService.findById(1L, user)).thenReturn(userModel);
        when(gitHubService.getSettings(userModel)).thenReturn(settings);

        SettingsDto settingsDto = gitHubController.getGitHubSetting();

        assertGitHub(settingsDto, settings);
    }

    @Test
    public void setGitHubSetting(){
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        SettingsSpec settingsSpec = new SettingsSpec("token", 5000, 5000, "username");
        userModel.setGitHubSettings(settings);

        when(userService.findById(1L, user)).thenReturn(userModel);
        when(gitHubService.initializeSettings(settings, userModel, settingsSpec)).thenReturn(settings);
        doNothing().when(gitHubService).createScheduledTask(taskRegistrar);

        SettingsDto settingsDto = gitHubController.setGitHubSetting(taskRegistrar, settingsSpec);

        assertGitHub(settingsDto, settings);
    }

    @Test
    public void getRepoDetails(){
        GitHubModel gitHub = new GitHubModel("user", "repo");
        gitHub.setId(2);

        when(gitHubService.generateGitHub("link")).thenReturn(gitHub);

        GitHubDto generated = gitHubController.getRepoDetails("link");


        assertEquals(generated.getId(), gitHub.getId());
        assertEquals(generated.getRepo(), gitHub.getRepo());
        assertEquals(generated.getUser(), gitHub.getUser());
    }

    @Test
    public void getRepoDetails_WithFail(){
        GitHubModel gitHub = new GitHubModel("user", "repo");
        gitHub.setId(2);
        gitHub.setLastFail(LocalDateTime.now());
        gitHub.setFailMessage("Exception:GhException");

        when(gitHubService.generateGitHub("link")).thenReturn(gitHub);

        GHException thrown = assertThrows(GHException.class,
                () -> gitHubController.getRepoDetails("link"));

        assertEquals(thrown.getMessage(), "GhException");
    }
}
