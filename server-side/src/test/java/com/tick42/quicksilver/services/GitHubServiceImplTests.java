package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.GitHub;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GitHubServiceImplTests {

    @Mock
    GitHubRepository gitHubRepository;

    @InjectMocks
    private GitHubServiceImpl gitHubService;

    @Test
    public void setRemoteDetails_whenGitHubModelIsValid_shouldNotThrow() {
        //Arrange
        GitHub gitHub = new GitHub();
        gitHub.setUser("Smytt");
        gitHub.setRepo("Tick42-ExtensionRepository");

        //Act
        try {
            gitHubService.setRemoteDetails(gitHub);
            //Assert
            Assert.assertTrue(Boolean.TRUE);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void generateGitHub_whenLinkCorrect_returnGitHubModel() {
        //Arrange
        String link = "https://github.com/Smytt/Tick42-ExtensionRepository";

        //Act
        GitHub gitHub = gitHubService.generateGitHub(link);

        //Assert
        Assert.assertEquals(gitHub.getUser(), "Smytt");
        Assert.assertEquals(gitHub.getRepo(), "Tick42-ExtensionRepository");
    }

    @Test
    public void updateExtensionDetails_whenListProvided_shouldUpdateResults() {
        //Arrange
        GitHub gitHub1 = new GitHub();
        GitHub gitHub2 = new GitHub();
        gitHub1.setUser("Smytt");
        gitHub1.setRepo("Tick42-ExtensionRepository");
        gitHub2.setUser("wrong");
        gitHub2.setRepo("wrong");
        List<GitHub> gitHubs = Arrays.asList(gitHub1, gitHub2);

        when(gitHubRepository.findAll()).thenReturn(gitHubs);

        //Act
        gitHubService.updateExtensionDetails();

        //Assert
        verify(gitHubRepository, times(2)).save(isA(GitHub.class));
    }
}
