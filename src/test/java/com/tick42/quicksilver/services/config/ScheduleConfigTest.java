package com.tick42.quicksilver.services.config;

import com.tick42.quicksilver.config.ScheduleConfig;
import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.GitHubServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleConfigTest {
    private ScheduleConfig scheduleConfig;

    @Mock
    private GitHubServiceImpl gitHubService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Logger logger;

    @Mock
    private ScheduledTaskRegistrar taskRegistrar;

    private static String adminPassword, token;

    @BeforeAll
    public static void setupAll() throws IOException{
        try(BufferedReader br = new BufferedReader(new FileReader("/adminUser.txt"))){
            token = br.readLine();
            adminPassword = br.readLine();
        }
    }

    @BeforeEach
    public void setup(){
        try(MockedStatic<LogManager> mock = mockStatic(LogManager.class)){
            mock.when(LogManager::getLogger).thenReturn(logger);
            scheduleConfig = Mockito.spy(new ScheduleConfig(userRepository, gitHubService));
        }
    }

    @Test
    public void initializeUser(){
        UserModel user = scheduleConfig.initializeUser();

        assertEquals(user.getId(), 1);
        assertEquals(user.getUsername(), "admin9512");
        assertTrue(BCrypt.checkpw(adminPassword, user.getPassword()));
        assertEquals(user.getGitHubSettings().getId(), 1);
        assertEquals(user.getGitHubSettings().getRate(), 50_0000);
        assertEquals(user.getGitHubSettings().getWait(), 5000);
        assertEquals(user.getGitHubSettings().getToken(), token);
        assertEquals(user.getGitHubSettings().getUsername(), "ivaylo9512");
    }

    @Test
    public void getTokenFromFile() {
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> scheduleConfig.getTokenFromFile("/incorrect"));
        ArgumentCaptor<FileNotFoundException> captor = ArgumentCaptor.forClass(FileNotFoundException.class);

        verify(logger).error(captor.capture());
        FileNotFoundException logException = captor.getValue();

        assertEquals(logException.getMessage(), "/incorrect (No such file or directory)");
        assertEquals(thrown.getMessage(), "java.io.FileNotFoundException: /incorrect (No such file or directory)");
    }

    @Test
    public void configureTask(){
        Settings settings = new Settings();
        UserModel user = new UserModel();
        user.setGitHubSettings(settings);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        scheduleConfig.configureTasks(taskRegistrar);

        verify(gitHubService, times(1)).initializeSettings(settings, user, null);
        verify(gitHubService, times(1)).connectGithub();
        verify(gitHubService, times(1)).createScheduledTask(taskRegistrar);
    }

    @Test
    public void configureTask_WithNotFound(){
        Settings settings = new Settings();
        UserModel user = new UserModel();
        user.setGitHubSettings(settings);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(scheduleConfig.initializeUser()).thenReturn(user);

        scheduleConfig.configureTasks(taskRegistrar);

        verify(gitHubService, times(1)).initializeSettings(settings, user, null);
        verify(gitHubService, times(1)).connectGithub();
        verify(gitHubService, times(1)).createScheduledTask(taskRegistrar);
    }
}
