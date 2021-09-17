package com.tick42.quicksilver.services;

import com.tick42.quicksilver.controllers.UserController;
import com.tick42.quicksilver.models.Dtos.UserDto;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.security.Jwt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserServiceImpl userService;

    @Mock
    private FileServiceImpl fileService;

    @InjectMocks
    private UserController userController;

    private final MockMultipartFile multipartFile = new MockMultipartFile("imageTest", "imageTest.png", "image/png", "imageTest".getBytes());
    private final UserModel userModel = new UserModel(1, "username", "email", "password", "ROLE_ADMIN", "info", "Bulgaria");
    private final File profileImage = new File("profileImage", 32_000, "image/png", "png", userModel);
    private final UserDetails user = new UserDetails(userModel, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    private final String token = "Token " + Jwt.generate(user);
    private final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getId());

    private void assertUser(UserDto userDto, UserModel userModel) {
        assertEquals(userDto.getId(), userModel.getId());
        assertEquals(userDto.getUsername(), userModel.getUsername());
        assertEquals(userDto.getEmail(), userModel.getEmail());
        assertEquals(userDto.getRole(), userModel.getRole());
        assertEquals(userDto.getCountry(), userModel.getCountry());
        assertEquals(userDto.getInfo(), userModel.getInfo());
    }

    @Test
    public void login(){
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDto loggedUser = userController.login();

        assertUser(loggedUser, userModel);
    }

    @Test
    public void register() throws IOException {
        HttpServletResponse response = new MockHttpServletResponse();
        RegisterSpec register = new RegisterSpec("username", "email", "password", multipartFile, "Bulgaria", "info");

        userModel.setProfileImage(profileImage);
        userModel.setRole("ROLE_USER");

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        when(userService.create(any(UserModel.class))).thenReturn(userModel);
        when(fileService.generate(multipartFile, "profileImage", "image/png")).thenReturn(profileImage);

        UserDto registeredUser = userController.register(register, response);

        assertEquals(registeredUser.getId(), userModel.getId());
        assertEquals(registeredUser.getUsername(), userModel.getUsername());
        assertEquals(registeredUser.getCountry(), userModel.getCountry());
        assertEquals(registeredUser.getRole(), userModel.getRole());
        assertEquals(registeredUser.getEmail(), userModel.getEmail());

        verify(fileService, times(1)).save("profileImage1", multipartFile);
        verify(userService).create(captor.capture());
        UserModel passedToCreate = captor.getValue();

        assertEquals(passedToCreate.getUsername(), userModel.getUsername());
        assertEquals(passedToCreate.getProfileImage(), profileImage);
        assertEquals(passedToCreate.getProfileImage().getOwner().getUsername(), userModel.getUsername());
        assertEquals(passedToCreate.getRole(), userModel.getRole());

        String token = response.getHeader("Authorization");
        assertNotNull(token);

        UserDetails userDetails = Jwt.validate(token.substring(6));
        assertEquals(userDetails.getId(), userModel.getId());
        assertEquals(userDetails.getAuthorities().stream().toList().get(0), new SimpleGrantedAuthority(userModel.getRole()));
    }

    @Test
    public void registerAdmin() throws IOException {
        RegisterSpec register = new RegisterSpec("username", "email", "password", multipartFile, "Bulgaria", "info");

        userModel.setProfileImage(profileImage);
        userModel.setRole("ROLE_ADMIN");

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        when(userService.create(any(UserModel.class))).thenReturn(userModel);
        when(fileService.generate(multipartFile, "profileImage", "image/png")).thenReturn(profileImage);

        UserDto registeredUser = userController.registerAdmin(register);

        assertUser(registeredUser, userModel);

        verify(fileService, times(1)).save("profileImage1", multipartFile);
        verify(userService).create(captor.capture());
        UserModel passedToCreate = captor.getValue();

        assertEquals(passedToCreate.getUsername(), userModel.getUsername());
        assertEquals(passedToCreate.getProfileImage(), profileImage);
        assertEquals(passedToCreate.getProfileImage().getOwner().getUsername(), userModel.getUsername());
        assertEquals(passedToCreate.getRole(), userModel.getRole());
    }

    @Test
    public void findById(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", token);

        when(userService.findById(eq(1L), any(UserDetails.class))).thenReturn(userModel);

        UserDto user = userController.findById(1L, request);

        assertUser(user, userModel);
        verify(userService, times(1)).findById(eq(1L), any(UserDetails.class));
    }

    @Test
    public void findByIdWithoutToken(){
        MockHttpServletRequest request = new MockHttpServletRequest();

        when(userService.findById(1L, null)).thenReturn(userModel);

        UserDto user = userController.findById(1L, request);

        assertEquals(user.getId(), userModel.getId());
        assertEquals(user.getRole(), userModel.getRole());
        verify(userService, times(1)).findById(1L, null);
    }

    @Test
    public void changeUserInfo(){
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserSpec userSpec = new UserSpec();

        when(userService.changeUserInfo(userSpec, user)).thenReturn(userModel);

        UserDto userDto = userController.changeUserInfo(userSpec);

        assertUser(userDto, userModel);
    }

    @Test
    public void setEnabled(){
        userController.setEnable(true, 1L);
        verify(userService, times(1)).setEnabled(true, 1L);
    }

    @Test
    public void changePassword(){
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
        NewPasswordSpec passwordSpec = new NewPasswordSpec("username", "password", "newPassword");

        when(userService.changePassword(passwordSpec, user)).thenReturn(userModel);

        UserDto userDto = userController.changePassword(passwordSpec);

        assertUser(userDto, userModel);
    }

    @Test
    public void setState(){
        when(userService.setState(1L, "state")).thenReturn(userModel);

        UserDto userDto = userController.setState("state", 1L);

        assertUser(userDto, userModel);
    }
}
