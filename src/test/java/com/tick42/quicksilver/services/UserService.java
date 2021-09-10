package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
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
    public void findById_WithNonExistingUser_EntityNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> userService.findById(1, any(UserDetails.class)));

        assertEquals(thrown.getMessage(), "User not found.");
    }

    @Test
    public void findById_WithNotEnabledUser() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("test", "test", authorities, 2);

        UserModel user = new UserModel();
        user.setId(1);
        user.setActive(true);
        user.setEnabled(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        DisabledUserException thrown = assertThrows(DisabledUserException.class,
                () -> userService.findById(user.getId(), loggedUser));

        assertEquals(thrown.getMessage(), "You must complete the registration. Check your email.");
    }

    @Test
    public void findById_WithNotEnabledUser_AndLoggedAdminUser() {
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails loggedUser = new UserDetails("test", "test", authorities, 2);

        UserModel user = new UserModel();
        user.setId(1);
        user.setActive(true);
        user.setEnabled(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserModel foundUser = userService.findById(user.getId(), loggedUser);

        assertEquals(foundUser, user);
    }

    @Test
    public void findById_WhenUserProfileIsNotActive_AndCurrentUserIsNotAdmin_Unauthorized(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("test", "test", authorities, 2);

        UserModel blockedUser = new UserModel();
        blockedUser.setId(1);
        blockedUser.setActive(false);

        when(userRepository.findById(blockedUser.getId())).thenReturn(Optional.of(blockedUser));

        UnauthorizedException thrown = assertThrows(UnauthorizedException.class,
                () -> userService.findById(blockedUser.getId(), loggedUser));

        assertEquals(thrown.getMessage(), "User is unavailable.");
    }
    @Test()
    public void findById_whenUserProfileIsNotActive_andCurrentUserIsAdmin(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UserDetails loggedUser = new UserDetails("test", "test", authorities, 1);

        UserModel blockedUser = new UserModel();
        blockedUser.setId(1);
        blockedUser.setActive(false);
        blockedUser.setUsername("test");
        blockedUser.setRole("ROLE_USER");

        when(userRepository.findById(blockedUser.getId())).thenReturn(Optional.of(blockedUser));

        UserModel user = userService.findById(blockedUser.getId(), loggedUser);
        assertEquals(user.getUsername(), blockedUser.getUsername());
    }

    @Test
    public void findBlockedUsers() {
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setActive(false);
        userModel1.setActive(false);
        List<UserModel> userModels = List.of(userModel, userModel1);

        when(userRepository.findByIsActive(false)).thenReturn(userModels);

        List<UserModel> usersDTO = userService.findAll("blocked");

        assertEquals(2, usersDTO.size());
        assertFalse(usersDTO.get(0).isActive());
        assertFalse(usersDTO.get(1).isActive());
    }

    @Test
    public void findActiveUsers() {
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setActive(true);
        userModel1.setActive(true);
        List<UserModel> userModels = Arrays.asList(userModel, userModel1);

        when(userRepository.findByIsActive(true)).thenReturn(userModels);

        List<UserModel> usersDTO = userService.findAll("active");

        assertEquals(2, usersDTO.size());
        assertTrue(usersDTO.get(0).isActive());
        assertTrue(usersDTO.get(1).isActive());
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

        assertEquals(thrown.getMessage(), "\"invalid\" is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");

    }

    @Test()
    public void setUserState_Enable(){
        UserModel userModel = new UserModel();
        userModel.setActive(false);
        userModel.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserModel user = userService.setState(1,"enable");

        assertTrue(user.isActive());
    }

    @Test()
    public void setUserState_Block(){
        UserModel userModel = new UserModel();
        userModel.setActive(true);
        userModel.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserModel user = userService.setState(1,"block");

        assertFalse(user.isActive());
    }

    @Test
    public void setUserState_WithWrongState_InvalidState(){
        UserModel userModel = new UserModel();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> userService.setState(1,"invalid")
        );

        assertEquals(thrown.getMessage(), "\"invalid\" is not a valid userModel state. Use \"enable\" or \"block\".");
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
        userModel.setUsername("username");
        userModel.setPassword("password");

        UserModel foundUserModel = new UserModel();
        foundUserModel.setPassword(BCrypt.hashpw("password",BCrypt.gensalt(4)));
        foundUserModel.setActive(false);
        when(userRepository.findByUsername("username")).thenReturn(foundUserModel);

        BlockedUserException thrown = assertThrows(BlockedUserException.class,
                () -> userService.loadUserByUsername("username"));

        assertEquals(thrown.getMessage(), "User is disabled.");
    }

    @Test
    public void loadUserByUsername(){
        UserModel foundUser = new UserModel("username", "test@gmail.com", "password", "ROLE_ADMIN");

        UserDetails userDetails = new UserDetails(foundUser, List.of(
                new SimpleGrantedAuthority(foundUser.getRole())));

        when(userRepository.findByUsername("username")).thenReturn(foundUser);

        UserDetails loggedUser = userService.loadUserByUsername("username");

        assertEquals(userDetails, loggedUser);
        assertEquals(foundUser.getUsername(), loggedUser.getUsername());
    }

    @Test
    public void RegisterUser_WithAlreadyTakenUsername_UsernameExists() {
        UserModel user = new UserModel("test", "test@gmail.com", "test", "ROLE_ADMIN");

        when(userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail())).thenReturn(user);

        UsernameExistsException thrown = assertThrows(UsernameExistsException.class,
                () -> userService.create(user));

        assertEquals(thrown.getMessage(), "Username is already taken.");
    }

    @Test
    public void registerUser() {
        UserModel userModel = new UserModel("test", "testEmail@gmail.com", "test", "ROLE_USER");

        when(userRepository.findByUsernameOrEmail(userModel.getUsername(), userModel.getEmail())).thenReturn(null);
        when(userRepository.save(userModel)).thenReturn(userModel);

        UserModel user = userService.create(userModel);

        assertEquals(user, userModel);
    }

    @Test
    public void ChangePassword(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("test", "test", authorities, 1);

        NewPasswordSpec passwordSpec = new NewPasswordSpec("user",
                "currentPassword", "newTestPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword("currentPassword");
        userModel.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(userModel)).thenReturn(userModel);

        UserModel user = userService.changePassword(passwordSpec, loggedUser);
        assertTrue(BCrypt.checkpw("newTestPassword", user.getPassword()));

    }

    @Test
    public void ChangePassword_WithWrongPassword_BadCredentials(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("test", "test", authorities, 2);

        NewPasswordSpec passwordSpec = new NewPasswordSpec("user",
                "incorrect", "newTestPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword("currentPassword");
        userModel.setEnabled(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(userModel));

        BadCredentialsException thrown = assertThrows(BadCredentialsException.class,
                () -> userService.changePassword(passwordSpec, loggedUser));

        assertEquals(thrown.getMessage(), "Invalid current password.");
    }
}

