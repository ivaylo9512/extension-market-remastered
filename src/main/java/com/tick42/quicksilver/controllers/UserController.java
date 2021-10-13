package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.Dtos.PageDto;
import com.tick42.quicksilver.models.Dtos.UserDto;
import com.tick42.quicksilver.models.EmailToken;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.EmailTokenService;
import com.tick42.quicksilver.services.base.FileService;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final FileService fileService;
    private final EmailTokenService emailTokenService;

    public UserController(UserService userService, FileService fileService, EmailTokenService emailTokenService) {
        this.userService = userService;
        this.fileService = fileService;
        this.emailTokenService = emailTokenService;
    }

    @PostMapping(value = "/register")
    @Transactional
    public void register(@Valid @ModelAttribute RegisterSpec registerSpec, HttpServletResponse response) throws IOException, MessagingException {
        File file = generateFile(registerSpec.getProfileImage(), null);

        UserModel newUser = userService.create(new UserModel(registerSpec, file, "ROLE_USER"));

        if(file != null){
            fileService.save(file.getResourceType() + newUser.getId(), registerSpec.getProfileImage());
        }

        emailTokenService.sendVerificationEmail(newUser);
    }

    @GetMapping(value = "/activate/{token}")
    public void activate(@PathVariable("token") String token, HttpServletResponse httpServletResponse) throws IOException {
        EmailToken emailToken = emailTokenService.findByToken(token);
        UserModel user = emailToken.getUser();

        if(emailToken.getExpiryDate().isBefore(LocalDateTime.now())){
            emailTokenService.delete(emailToken);
            userService.delete(emailToken.getUser());

            throw new UnauthorizedException("Token has expired. Repeat your registration.");
        }

        user.setEnabled(true);

        userService.save(user);
        emailTokenService.delete(emailToken);

        httpServletResponse.sendRedirect("https://localhost:4200");
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
        File file = generateFile(registerSpec.getProfileImage(), null);

        UserModel user = new UserModel(registerSpec, file, "ROLE_ADMIN");
        user.setEnabled(true);

        UserModel newUser = userService.create(user);

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
    @PatchMapping(value = "/auth/setActive/{id}/{newState}")
    public UserDto setActive(@PathVariable("newState") boolean state,
                            @PathVariable("id") long id) {
        return new UserDto(userService.setActive(id, state));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/auth/findAll")
    public PageDto<UserDto> findAll(@RequestParam(name = "isActive", required = false) Boolean isActive,
                                 @RequestParam(name = "name", required = false, defaultValue = "") String name,
                                 @RequestParam(name = "pageSize") int pageSize,
                                 @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName) {
        if(isActive != null){
            Page<UserModel> page = userService.findByActive(isActive, name, lastName, pageSize);
            return new PageDto<>(page.stream().map(UserDto::new).collect(Collectors.toList()), page.getTotalPages(), page.getTotalElements());
        }

        Page<UserModel> page = userService.findByName(name, lastName, pageSize);
        return new PageDto<>(page.stream().map(UserDto::new).collect(Collectors.toList()), page.getTotalPages(), page.getTotalElements());
    }

    @DeleteMapping(value = "/auth/delete/{id}")
    public void delete(@PathVariable("id") long id){
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();

        UserModel user = userService.findById(id, loggedUser);
        String fileName = "profileImage" + id + "." + user.getProfileImage().getExtensionType();

        userService.delete(user);
        fileService.deleteFromSystem(fileName);
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/changePassword")
    public UserDto changePassword(@Valid @RequestBody NewPasswordSpec newPasswordSpec){
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();

        return new UserDto(userService.changePassword(newPasswordSpec, loggedUser));
    }

    @PostMapping(value = "/auth/changeUserInfo")
    public UserDto changeUserInfo(@Valid @ModelAttribute UserSpec userSpec) throws IOException {
        UserDetails loggedUser = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();
        UserModel user = userService.findById(loggedUser.getId(), loggedUser);

        String imageName = user.getProfileImage() != null
                ? "profileImage" + loggedUser.getId() + "." + user.getProfileImage().getExtensionType() : null;

        File file = generateFile(userSpec.getProfileImage(), user.getProfileImage());
        user.setProfileImage(file == null ? user.getProfileImage() : file);

        UserModel newUser = userService.changeUserInfo(userSpec, user);

        if(file != null){
            fileService.save(file.getResourceType() + newUser.getId(), userSpec.getProfileImage());

            if(imageName != null && !imageName.equals("profileImage" + newUser.getId() + "." + file.getExtensionType())){
                fileService.deleteFromSystem(imageName);
            }
        }

        return new UserDto(newUser);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/setEnabled/{state}/{id}")
    public void setEnable(@PathVariable(name = "state") boolean state,
                          @PathVariable(name = "id") long id){
        userService.setEnabled(state, id);
    }

    public File generateFile(MultipartFile profileImage, File oldImage){
        if(profileImage == null){
            return null;
        }

        File file = fileService.generate(profileImage,"profileImage", "image");
        file.setId(oldImage != null ? oldImage.getId() : 0);

        return file;
    }

    @ExceptionHandler
    ResponseEntity<String> handleUsernameExistsException(UsernameExistsException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity<String> handleEmailExistsException(EmailExistsException e) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(e.getMessage());
    }
}
