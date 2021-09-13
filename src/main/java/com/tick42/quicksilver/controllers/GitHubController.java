package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.GitHubRepositoryException;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.GitHubDto;
import com.tick42.quicksilver.models.Dtos.GitHubSettingDto;
import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.GitHubService;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @PostMapping("/auth/setSettings")
    public GitHubSettingDto setGitHubSetting(ScheduledTaskRegistrar taskRegistrar, @Valid @RequestBody GitHubSettingSpec gitHubSettingSpec) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel userModel = userService.findById(userId, loggedUser);

        Settings settings = gitHubService.initializeSettings(userModel.getGitHubSettings(), userModel, gitHubSettingSpec);
        gitHubService.createScheduledTask(taskRegistrar);

        return new GitHubSettingDto(settings);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/auth/getSettings")
    public GitHubSettingDto getGitHubSetting() {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        return new GitHubSettingDto(gitHubService.getSettings(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/reload/{id}")
    public GitHubDto reloadGitHubData(@PathVariable("id") long id) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        Extension extension = extensionService.findById(id, loggedUser);
        UserModel user = userService.findById(userId, loggedUser);

        return new GitHubDto(gitHubService.reloadGitHub(extension.getGithub(), user));
    }

    @ExceptionHandler
    ResponseEntity<String> handleGitHubRepositoryExceptionException(GitHubRepositoryException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
