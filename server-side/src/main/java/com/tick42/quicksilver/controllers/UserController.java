package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.UserDto;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.FileService;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    public UserController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @PostMapping(value = "/register")
    public UserDto register(@ModelAttribute RegisterSpec registerSpec, HttpServletResponse response) {
        UserModel newUser = new UserModel(registerSpec, "ROLE_USER");

        if(registerSpec.getProfileImage() != null){
            File profileImage = fileService.create(registerSpec.getProfileImage(), newUser.getId() + "logo");
            newUser.setProfileImage(profileImage);
        }

        String token = Jwt.generate(new UserDetails(newUser, new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority(newUser.getRole())))));
        response.addHeader("Authorization", "Token " + token);

        return new UserDto(userService.create(newUser));
    }

    @PostMapping("/login")
    public UserDto login(){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return new UserDto(user.getUserModel());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "auth/users/adminRegistration")
    public UserDto registerAdmin(@ModelAttribute RegisterSpec registerSpec, HttpServletResponse response){
        UserModel newUser = new UserModel(registerSpec, "ROLE_ADMIN");

        if(registerSpec.getProfileImage() != null){
            File profileImage = fileService.create(registerSpec.getProfileImage(), newUser.getId() + "logo");
            newUser.setProfileImage(profileImage);
        }

        String token = Jwt.generate(new UserDetails(newUser, new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority(newUser.getRole())))));
        response.addHeader("Authorization", "Token " + token);

        return new UserDto(userService.create(newUser));
    }

    @GetMapping(value = "/findById/{id}")
    public UserDto findById(@PathVariable(name = "id") int id, HttpServletRequest request) {
        UserDetails loggedUser;
        try {
            loggedUser = Jwt.validate(request.getHeader("Authorization").substring(6));
        } catch (Exception e) {
            loggedUser = null;
        }
        UserModel user = userService.findById(id, loggedUser);
        return new UserDto(user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/setState/{id}/{newState}")
    public UserDto setState(@PathVariable("newState") String state,
                            @PathVariable("id") int id) {
        return new UserDto(userService.setState(id, state));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/auth/all")
    public List<UserDto> findAll(@RequestParam(name = "state", required = false) String state) {
        return userService.findAll(state).stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/changePassword")
    public UserDto changePassword(@Valid NewPasswordSpec newPasswordSpec, HttpServletRequest request){
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        return new UserDto(userService.changePassword(newPasswordSpec));
    }

    private ExtensionDto generateExtensionDTO(Extension extension) {
        ExtensionDto extensionDto = new ExtensionDto(extension);
        if (extension.getGithub() != null) {
            extensionDto.setGitHubLink(extension.getGithub().getLink());
            extensionDto.setOpenIssues(extension.getGithub().getOpenIssues());
            extensionDto.setPullRequests(extension.getGithub().getPullRequests());
            extensionDto.setGithubId(extension.getGithub().getId());

            if (extension.getGithub().getLastCommit() != null)
                extensionDto.setLastCommit(extension.getGithub().getLastCommit());

            if (extension.getGithub().getLastSuccess() != null)
                extensionDto.setLastSuccessfulPullOfData(extension.getGithub().getLastSuccess());

            if (extension.getGithub().getLastFail() != null) {
                extensionDto.setLastFailedAttemptToCollectData(extension.getGithub().getLastFail());
                extensionDto.setLastErrorMessage(extension.getGithub().getFailMessage());
            }
        }

        if (extension.getImage() != null)
            extensionDto.setImageLocation(extension.getImage());

        if (extension.getFile() != null)
            extensionDto.setFileLocation(extension.getFile());

        if (extension.getCover() != null)
            extensionDto.setCoverLocation(extension.getCover());


        return extensionDto;
    }

    @ExceptionHandler
    ResponseEntity handleExtensionNotFoundException(EntityNotFoundException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleUsernameExistsException(UsernameExistsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handlePasswordsMissMatchException(PasswordsMissMatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleInvalidStateException(InvalidStateException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleDisabledUserException(BlockedUserException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }


    @ExceptionHandler
    ResponseEntity handleUserProfileUnavailableException(UserProfileUnavailableException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity handleInvalidUserSpecException(MethodArgumentNotValidException e)
    {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getBindingResult().
                        getFieldErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toArray());
    }
}
