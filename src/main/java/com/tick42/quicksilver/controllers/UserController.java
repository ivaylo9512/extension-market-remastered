package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.Dtos.UserDto;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.FileService;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
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
    public UserDto register(@Valid @ModelAttribute RegisterSpec registerSpec, HttpServletResponse response) throws IOException {
        MultipartFile profileImage = registerSpec.getProfileImage();
        File file = null;

        if(profileImage != null){
            file = fileService.generate(profileImage,"profileImage", "image/png");
        }

        UserModel newUser = userService.create(new UserModel(registerSpec, file, "ROLE_USER"));

        if(file != null){
            fileService.save(file.getResourceType() + newUser.getId(), registerSpec.getProfileImage());
        }

        String token = Jwt.generate(new UserDetails(newUser, new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority(newUser.getRole())))));
        response.addHeader("Authorization", "Token " + token);

        return new UserDto(newUser);
    }

    @PostMapping("/login")
    public UserDto login(){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return new UserDto(user.getUserModel());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/auth/registerAdmin")
    public UserDto registerAdmin(@Valid @ModelAttribute RegisterSpec registerSpec) throws IOException {
        MultipartFile profileImage = registerSpec.getProfileImage();
        File file = null;

        if(profileImage != null){
            file = fileService.generate(profileImage,"profileImage", "image/png");
        }

        UserModel newUser = userService.create(new UserModel(registerSpec, file, "ROLE_ADMIN"));
        newUser.setEnabled(true);

        if(file != null){
            fileService.save(file.getResourceType() + newUser.getId(), registerSpec.getProfileImage());
        }

        return new UserDto(newUser);
    }

    @GetMapping(value = "/findById/{id}")
    public UserDto findById(@PathVariable(name = "id") long id, HttpServletRequest request) {
        UserDetails loggedUser = null;
        String authorization = request.getHeader("Authorization");

        if(authorization != null){
            loggedUser = Jwt.validate(authorization.substring(6));
        }

        UserModel user = userService.findById(id, loggedUser);
        return new UserDto(user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/setState/{id}/{newState}")
    public UserDto setState(@PathVariable("newState") String state,
                            @PathVariable("id") long id) {
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
    public UserDto changePassword(@Valid @RequestBody NewPasswordSpec newPasswordSpec){
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();

        return new UserDto(userService.changePassword(newPasswordSpec, loggedUser));
    }

    @PostMapping(value = "/auth/changeUserInfo")
    public UserDto changeUserInfo(@Valid @RequestBody UserSpec userSpec){
        UserDetails loggedUser = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        return new UserDto(userService.changeUserInfo(userSpec, loggedUser));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/setEnabled/{state}/{id}")
    public void setEnable(@PathVariable(name = "state") boolean state,
                          @PathVariable(name = "id") long id){
        userService.setEnabled(state, id);
    }

    @ExceptionHandler
    ResponseEntity<String> handleUsernameExistsException(UsernameExistsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity<String> handleEmailExistsException(EmailExistsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
