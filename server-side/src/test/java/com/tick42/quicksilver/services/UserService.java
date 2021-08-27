package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserService {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void findById_withNonExistingUser_Unauthorized() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> userService.findById(1, any(UserDetails.class)));

        assertEquals(thrown.getMessage(), "User not found.");
    }

    @Test
    public void findById_whenUserProfileIsNotActive_andCurrentUserIsNotAdmin_Unauthorized(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("TEST", "TEST", authorities, 2);

        UserModel blockedUser = new UserModel();
        blockedUser.setIsActive(false);
        blockedUser.setRole("ROLE_USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(blockedUser));

        UnauthorizedException thrown = assertThrows(UnauthorizedException.class,
                () -> userService.findById(1, loggedUser));

        assertEquals(thrown.getMessage(), "User is unavailable.");
    }
    @Test()
    public void findById_whenUserProfileIsNotActive_andCurrentUserIsAdmin(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UserDetails loggedUser = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel blockedUser = new UserModel();
        blockedUser.setIsActive(false);
        blockedUser.setUsername("test");
        blockedUser.setRole("ROLE_USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(blockedUser));

        UserModel user = userService.findById(1, loggedUser);
        assertEquals(user.getUsername(), blockedUser.getUsername());
    }

    @Test
    public void findBlockedUsers() {
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setIsActive(false);
        userModel1.setIsActive(false);
        List<UserModel> userModels = List.of(userModel, userModel1);

        when(userRepository.findByActive(false)).thenReturn(userModels);

        List<UserModel> usersDTO = userService.findAll("blocked");

        assertEquals(2, usersDTO.size());
        assertFalse(usersDTO.get(0).getIsActive());
        assertFalse(usersDTO.get(1).getIsActive());
    }

    @Test
    public void findActiveUsers() {
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setIsActive(true);
        userModel1.setIsActive(true);
        List<UserModel> userModels = Arrays.asList(userModel, userModel1);

        when(userRepository.findByActive(true)).thenReturn(userModels);

        List<UserModel> usersDTO = userService.findAll("active");

        assertEquals(2, usersDTO.size());
        assertTrue(usersDTO.get(0).getIsActive());
        assertTrue(usersDTO.get(1).getIsActive());
    }

    @Test
    public void findAll_WhenStateNull_InvalidInput() {
        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> userService.findAll(null)
        );

        assertEquals(thrown.getMessage(), "State is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
    }

    @Test
    public void findAllUsers_WithWrongState_ShouldThrow(){
        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> userService.findAll("invalid")
        );

        assertEquals(thrown.getMessage(), "\"active\" is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");

    }

    @Test()
    public void setUserState_Enable(){
        UserModel userModel = new UserModel();
        userModel.setIsActive(false);
        userModel.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserModel user = userService.setState(1,"enable");

        assertTrue(user.getIsActive());
    }

    @Test()
    public void setUserState_Block(){
        UserModel userModel = new UserModel();
        userModel.setIsActive(true);
        userModel.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserModel user = userService.setState(1,"block");

        assertFalse(user.getIsActive());
    }

    @Test
    public void setUserState_WithWrongState_InvalidState(){
        UserModel userModel = new UserModel();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> userService.setState(1,"invalid")
        );

        assertEquals(thrown.getMessage(), "invalid\" is not a valid userModel state. Use \"enable\" or \"block\".");
    }

    @Test
    public void setUserState_WithNonExistingUser_EntityNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> userService.setState(1,"Active"));

        assertEquals(thrown.getMessage(), "User not found.");
    }

    @Test
    public void loadUserByUsername_WithBlockedUser_BlockedUser() {
        UserModel userModel = new UserModel();
        userModel.setUsername("test");
        userModel.setPassword("password");

        UserModel foundUserModel = new UserModel();
        foundUserModel.setPassword(BCrypt.hashpw("password",BCrypt.gensalt(4)));
        foundUserModel.setIsActive(false);
        when(userRepository.findByUsername("test")).thenReturn(foundUserModel);

        BlockedUserException thrown = assertThrows(BlockedUserException.class,
                () -> userService.loadUserByUsername("test"));

        assertEquals(thrown.getMessage(), "User is disabled.");
    }

    @Test
    public void loadUserByUsername(){
        UserModel foundUser = new UserModel();
        foundUser.setIsActive(true);
        foundUser.setRole("ROLE_USER");
        foundUser.setUsername("username");

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new UserDetails(foundUser, authorities);

        when(userRepository.findByUsername("test")).thenReturn(foundUser);

        UserDetails loggedUser = userService.loadUserByUsername("test");

        assertEquals(userDetails, loggedUser);
        assertEquals(foundUser.getUsername(), loggedUser.getUsername());
    }

    @Test
    public void RegisterUser_WithAlreadyTakenUsername_UsernameExists() {
        RegisterSpec newRegistration = new RegisterSpec();
        newRegistration.setUsername("Test");
        UserModel registeredUserModel = new UserModel(newRegistration, "ROLE_USER");

        when(userRepository.findByUsername("Test")).thenReturn(registeredUserModel);

        UsernameExistsException thrown = assertThrows(UsernameExistsException.class,
                () -> userService.create(registeredUserModel));

        assertEquals(thrown.getMessage(), "Username is already taken.");
    }

    @Test
    public void RegisterUser_WithPasswordMissMatch() {
        RegisterSpec newRegistration = new RegisterSpec("Test", "TestPassword", "TestPasswordMissMatch");
        UserModel userModel = new UserModel(newRegistration, "ROLE_USER");

        when(userRepository.findByUsername("Test")).thenReturn(null);

        PasswordsMissMatchException thrown = assertThrows(PasswordsMissMatchException.class,
                () -> userService.create(userModel));

        assertEquals(thrown.getMessage(), "Username is already taken.");
    }

    @Test
    public void SuccessfulRegistration() {
        RegisterSpec newRegistration = new RegisterSpec("Test", "testPassword", "testPassword");
        UserModel userModel = new UserModel(newRegistration, "ROLE_USER");

        when(userRepository.findByUsername("Test")).thenReturn(null);
        when(userRepository.save(userModel)).thenReturn(userModel);

        UserModel user = userService.create(userModel);

        assertEquals(user, userModel);
    }

    @Test
    public void ChangePassword(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("TEST", "TEST", authorities, 2);

        NewPasswordSpec passwordSpec = new NewPasswordSpec("user",
                "currentPassword", "newTestPassword", "newTestPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword("currentPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(userModel)).thenReturn(userModel);

        UserModel user = userService.changePassword(passwordSpec, loggedUser);
        assertTrue(BCrypt.checkpw("newTestPassword", user.getPassword()));

    }

    @Test
    public void ChangePassword_WithWrongPassword_BadCredentials(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("TEST", "TEST", authorities, 2);

        NewPasswordSpec passwordSpec = new NewPasswordSpec("user",
                "incorrect", "newTestPassword", "newTestPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword("currentPassword");

        when(userRepository.findById(2L)).thenReturn(Optional.of(userModel));

        BadCredentialsException thrown = assertThrows(BadCredentialsException.class,
                () -> userService.changePassword(passwordSpec, loggedUser));

        assertEquals(thrown.getMessage(), "Invalid current password.");
    }

    @Test
    public void ChangePassword_WithPasswordMissMatch(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("TEST", "TEST", authorities, 2);

        NewPasswordSpec passwordSpec = new NewPasswordSpec("user",
                "incorrect", "newTestPassword", "newTestPassword");

        when(userRepository.findById(2L)).thenReturn(Optional.of(new UserModel()));

        PasswordsMissMatchException thrown = assertThrows(PasswordsMissMatchException.class,
                () -> userService.changePassword(passwordSpec, loggedUser));

        assertEquals(thrown.getMessage(), "Passwords don't match");
    }

}

