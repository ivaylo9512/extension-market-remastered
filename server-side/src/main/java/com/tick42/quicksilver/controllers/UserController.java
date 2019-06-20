package com.tick42.quicksilver.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.DTO.UserDTO;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.Spec.ChangeUserPasswordSpec;
import com.tick42.quicksilver.models.Spec.UserSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.FileService;
import com.tick42.quicksilver.services.base.UserService;
import com.tick42.quicksilver.validators.UserValidator;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    public UserController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @PostMapping(value = "/users/register")
    public UserDetails register(@RequestParam(name = "image", required = false) MultipartFile image,
                                @RequestParam(name = "user") String userJson) throws IOException, BindException {
        ObjectMapper mapper = new ObjectMapper();
        UserSpec user = mapper.readValue(userJson, UserSpec.class);

        Validator validator = new UserValidator();
        BindingResult bindingResult = new DataBinder(user).getBindingResult();
        validator.validate(user, bindingResult);
        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }

        UserModel newUser = userService.register(user, "ROLE_USER");

        if(image != null){
            File logo = fileService.storeUserLogo(image, newUser.getId(), "logo");
            newUser.setProfileImage(logo);
            userService.save(newUser);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(newUser.getRole()));

        return new UserDetails(newUser, authorities);
    }

    @PostMapping("/users/login")
    public UserDetails login(){
        return (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "auth/users/adminRegistration")
    public UserModel registerAdmin(@Valid @RequestBody UserSpec user){
        return userService.register(user, "ROLE_ADMIN");
    }

    @GetMapping(value = "/users/{id}")
    public UserDTO profile(@PathVariable(name = "id") int id, HttpServletRequest request) {
        UserDetails loggedUser;
        try {
            loggedUser = Jwt.validate(request.getHeader("Authorization").substring(6));
        } catch (Exception e) {
            loggedUser = null;
        }
        return userService.findById(id, loggedUser);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/users/setState/{id}/{newState}")
    public UserDTO setState(@PathVariable("newState") String state,
                            @PathVariable("id") int id) {
        return userService.setState(id, state);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/auth/users/all")
    public List<UserDTO> listAllUsers(@RequestParam(name = "state", required = false) String state) {
        return userService.findAll(state);
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/changePassword")
    public UserDTO changePassword(@Valid @RequestBody ChangeUserPasswordSpec changePasswordSpec, HttpServletRequest request){
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        return userService.changePassword(userId, changePasswordSpec);
    }

    @ExceptionHandler
    ResponseEntity handleInvalidCredentialsException(InvalidCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
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
