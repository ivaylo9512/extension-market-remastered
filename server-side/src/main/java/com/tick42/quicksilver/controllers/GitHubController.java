package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.GitHubRepositoryException;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.DTO.GitHubDTO;
import com.tick42.quicksilver.models.DTO.GitHubSettingDTO;
import com.tick42.quicksilver.models.Spec.GitHubSettingSpec;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.GitHubService;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/github")
public class GitHubController {

    private final GitHubService gitHubService;
    private final ExtensionService extensionService;
    private final UserService userService;

    public GitHubController(GitHubService gitHubService, ExtensionService extensionService, UserService userService) {
        this.gitHubService = gitHubService;
        this.extensionService = extensionService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/auth")
    public GitHubSettingDTO gitHubSetting(ScheduledTaskRegistrar taskRegistrar, @Valid @RequestBody GitHubSettingSpec gitHubSettingSpec) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        UserModel userModel = userService.findById(userId, loggedUser);
        return new GitHubSettingDTO(gitHubService.createScheduledTask(userModel, taskRegistrar, gitHubSettingSpec));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/auth")
    public GitHubSettingDTO getGitHubSetting() {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        return new GitHubSettingDTO(gitHubService.getSettings(user));
    }

    @GetMapping("/getRepoDetails")
    public GitHubDTO getRepoDetails(@RequestParam(name = "link") String link){
        GitHubModel gitHubModel = gitHubService.generateGitHub(link);
        if(gitHubModel.getFailMessage() != null){
            throw new GitHubRepositoryException("Couldn't connect to GitHubModel check the URL.");
        }
        return generateGithubDTO(gitHubService.generateGitHub(link));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/{id}/fetch")
    public GitHubDTO fetchGitHubData(@PathVariable("id") int id) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        Extension extension = extensionService.findById(id, loggedUser);

        UserModel user = userService.findById(userId, null);

        return generateGithubDTO(gitHubService.fetchGitHub(extension.getGithub(), user));
    }

    private GitHubDTO generateGithubDTO(GitHubModel gitHubModel){
        GitHubDTO gitHubDTO = new GitHubDTO(gitHubModel);
        if(gitHubModel.getLastSuccess() != null) {
            gitHubDTO.setLastSuccess(gitHubModel.getLastSuccess());
        }
        if(gitHubModel.getLastFail() != null){
            gitHubDTO.setLastFail(gitHubModel.getLastFail());
        }
        if(gitHubModel.getLastCommit() != null){
            gitHubDTO.setLastCommit(gitHubModel.getLastCommit());
        }
        if(gitHubModel.getFailMessage() != null){
            gitHubDTO.setFailMessage(gitHubModel.getFailMessage());
        }

        return gitHubDTO;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity handleInvalidGitHubSettingSpecException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toArray());
    }
    @ExceptionHandler
    ResponseEntity handleFeaturedLimitException(GitHubRepositoryException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
