package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.repositories.base.GitHubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GitHubModelServiceImplTests {

    @Mock
    GitHubRepository gitHubRepository;

    @InjectMocks
    private GitHubServiceImpl gitHubService;

    @Test
    public void setRemoteDetails_whenGitHubModelIsValid() {
        GitHubModel gitHubModel = new GitHubModel();
        gitHubModel.setUser("user");
        gitHubModel.setRepo("repo");

        gitHubService.setRemoteDetails(gitHubModel);
    }

    @Test
    public void updateExtensionDetails_whenListProvided_shouldUpdateResults() {
        GitHubModel gitHubModel1 = new GitHubModel();
        GitHubModel gitHubModel2 = new GitHubModel();
        gitHubModel1.setUser("username");
        gitHubModel1.setRepo("repo");
        gitHubModel2.setUser("wrong");
        gitHubModel2.setRepo("wrong");
        List<GitHubModel> gitHubModels = Arrays.asList(gitHubModel1, gitHubModel2);

        when(gitHubRepository.findAll()).thenReturn(gitHubModels);

        gitHubService.updateExtensionDetails();

        verify(gitHubRepository, times(2)).save(isA(GitHubModel.class));
    }
}
