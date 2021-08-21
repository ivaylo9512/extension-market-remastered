package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test(expected = NullPointerException.class)
    public void findById_withNonExistingUser_shouldThrow() {
        //Arrange
        UserDetails user = new UserDetails("Test", "Test", new ArrayList<>(), 1);
        when(userRepository.findById(1L)).thenReturn(null);

        //Act
        userService.findById(1, user);
    }

    @Test(expected = UserProfileUnavailableException.class)
    public void findById_whenUserProfileIsNotActive_andCurrentUserIsNotAdmin_shouldThrow(){
        //Arrange
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails loggedUser = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel blockedUser = new UserModel();
        blockedUser.setIsActive(false);
        blockedUser.setUsername("test");
        blockedUser.setRole("ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(blockedUser));

        //Act
        userService.findById(1, loggedUser);
    }
    @Test()
    public void findById_whenUserProfileIsNotActive_andCurrentUserIsAdmin_ShouldReturnUser(){
        //Arrange
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UserDetails loggedUser = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel blockedUser = new UserModel();
        blockedUser.setIsActive(false);
        blockedUser.setUsername("test");
        blockedUser.setRole("ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(blockedUser));

        //Act
        userService.findById(1, loggedUser);
    }

    @Test
    public void findBlockedUsers_ShouldReturnBlockedUsers() {
        //Arrange
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setIsActive(false);
        userModel1.setIsActive(false);
        List<UserModel> userModels = Arrays.asList(userModel, userModel1);

        when(userRepository.findByActive(false)).thenReturn(userModels);

        //Act
        List<UserModel> usersDTO = userService.findAll("blocked");

        //Assert
        Assert.assertEquals(2, usersDTO.size());
        Assert.assertFalse(usersDTO.get(0).getIsActive());
        Assert.assertFalse(usersDTO.get(1).getIsActive());
    }

    @Test
    public void findActiveUsers_ShouldReturnBlockedUsers() {
        //Arrange
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setIsActive(true);
        userModel1.setIsActive(true);
        List<UserModel> userModels = Arrays.asList(userModel, userModel1);

        when(userRepository.findByActive(true)).thenReturn(userModels);

        //Act
        List<UserModel> usersDTO = userService.findAll("active");

        //Assert
        Assert.assertEquals(2, usersDTO.size());
        Assert.assertTrue(usersDTO.get(0).getIsActive());
        Assert.assertTrue(usersDTO.get(1).getIsActive());
    }

    @Test
    public void findAllUsers_ShouldReturnAllUsers() {
        //Arrange
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setIsActive(true);
        userModel1.setIsActive(false);
        List<UserModel> userModels = Arrays.asList(userModel, userModel1);

        when(userRepository.findAll()).thenReturn(userModels);

        //Act
        List<UserModel> usersDTO = userService.findAll("all");

        //Assert
        Assert.assertEquals(2, usersDTO.size());
        Assert.assertTrue(usersDTO.get(0).getIsActive());
        Assert.assertFalse(usersDTO.get(1).getIsActive());
    }

    @Test(expected = InvalidStateException.class)
    public void findAll_whenStateNull_ShouldThrow() {
        //Act
        List<UserModel> usersDTO = userService.findAll(null);
    }

    @Test(expected = InvalidStateException.class)
    public void findAllUsers_WithWrongState_ShouldThrow(){
        //Act
        List<UserModel> usersDTO = userService.findAll("ActiveUsersInvalidInput");
    }

    @Test()
    public void setUserState_Enable_ShouldReturnEnable(){
        //Arrange
        UserModel userModel = new UserModel();
        userModel.setIsActive(false);
        userModel.setId(1);


        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);
        //act
        UserModel user = userService.setState(1,"enable");

        //Assert
        Assert.assertTrue("active", user.getIsActive());
    }

    @Test()
    public void setUserState_block_ShouldReturnBlocked(){
        //Arrange
        UserModel userModel = new UserModel();
        userModel.setIsActive(true);
        userModel.setId(1);


        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);
        //act
        UserModel user = userService.setState(1,"block");

        //Assert
        Assert.assertFalse(user.getIsActive());
    }

    @Test(expected = InvalidStateException.class)
    public void setUserState_WithWrongState_ShouldThrow(){

        //Arrange
        UserModel userModel = new UserModel();

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));

        //Act
        userService.setState(1,"InvalidState");
    }

    @Test(expected = NullPointerException.class)
    public void setUserState_WithNonExistingUser_ShouldThrow(){

        when(userRepository.findById(1L)).thenReturn(null);

        //Act
        userService.setState(1,"Active");
    }

    @Test(expected = BlockedUserException.class)
    public void LoginUserWithBlockedUser_shouldThrow() {

        //Arrange
        UserModel userModel = new UserModel();
        userModel.setUsername("test");
        userModel.setPassword("password");

        UserModel foundUserModel = new UserModel();
        foundUserModel.setPassword(BCrypt.hashpw("password",BCrypt.gensalt(4)));
        foundUserModel.setIsActive(false);
        when(userRepository.findByUsername("test")).thenReturn(foundUserModel);

        //Act
        userService.loadUserByUsername("test");
    }

    @Test
    public void LoginUser_ShouldReturnLoggedUser(){
        //Arrange
        UserModel foundUser = new UserModel();
        foundUser.setUsername("test");
        foundUser.setPassword(BCrypt.hashpw("password",BCrypt.gensalt(4)));
        foundUser.setIsActive(true);
        foundUser.setRole("ROLE_ADMIN");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UserDetails userDetails = new UserDetails(foundUser,authorities);
        when(userRepository.findByUsername("test")).thenReturn(foundUser);

        //Act
        UserDetails loggedUser = userService.loadUserByUsername("test");

        //Assert
        Assert.assertEquals(userDetails, loggedUser);
    }

    @Test(expected = UsernameExistsException.class)
    public void RegisterUser_WithAlreadyTakenUsername_shouldThrow() {
        //Arrange
        RegisterSpec newRegistration = new RegisterSpec();
        newRegistration.setUsername("Test");
        UserModel registeredUserModel = new UserModel(newRegistration, "ROLE_USER");

        when(userRepository.findByUsername("Test")).thenReturn(registeredUserModel);

        //Act
        userService.create(registeredUserModel);
    }

    @Test(expected = PasswordsMissMatchException.class)
    public void RegisterUser_WithNotMatchingPasswords_shouldThrow() {
        //Arrange
        RegisterSpec newRegistration = new RegisterSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("TestPassword");
        newRegistration.setRepeatPassword("TestPasswordMissMatch");

        UserModel userModel = new UserModel(newRegistration, "ROLE_USER");

        when(userRepository.findByUsername("Test")).thenReturn(null);

        //Act
        userService.create(userModel);
    }

    @Test()
    public void SuccessfulRegistration_withAvailable() {
        //Arrange
        RegisterSpec newRegistration = new RegisterSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("testPassword");
        newRegistration.setRepeatPassword("testPassword");

        UserModel userModel = new UserModel(newRegistration, "ROLE_USER");

        when(userRepository.findByUsername("Test")).thenReturn(null);

        //Act
        userService.create(userModel);
    }

    @Test()
    public void SuccessfulRegistration_Role_User() {
        //Arrange
        RegisterSpec newRegistration = new RegisterSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("testPassword");
        newRegistration.setRepeatPassword("testPassword");

        UserModel userModel = new UserModel(newRegistration, "ROLE_USER");

        when(userRepository.findByUsername("Test")).thenReturn(null);
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);
        //Act

        UserModel registeredUser = userService.create(userModel);

        //Assert
        Assert.assertEquals(registeredUser.getRole(),"ROLE_USER");
    }

    @Test()
    public void SuccessfulRegistration_Role_Admin_ShouldReturnAdminUser() {
        //Arrange
        RegisterSpec newRegistration = new RegisterSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("testPassword");
        newRegistration.setRepeatPassword("testPassword");

        UserModel userModel = new UserModel(newRegistration, "ROLE_ADMIN");

        when(userRepository.findByUsername("Test")).thenReturn(null);
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        //Act
        UserModel registeredUser = userService.create(userModel);

        //Assert
        Assert.assertEquals(registeredUser.getRole(),"ROLE_ADMIN");
    }

    @Test
    public void ChangePasswordState(){
        String password = "currentPassword";
        String newPassword = "newTestPassword1";

        NewPasswordSpec passwordSpec = new NewPasswordSpec();
        passwordSpec.setUsername("user");
        passwordSpec.setCurrentPassword(password);
        passwordSpec.setNewPassword(newPassword);
        passwordSpec.setRepeatNewPassword(newPassword);

        UserModel userModel = new UserModel();
        userModel.setPassword(password);

        when(userRepository.findByUsername("user")).thenReturn(userModel);

        userService.changePassword(passwordSpec);

        //Assert
        Assert.assertEquals(userModel.getPassword(),newPassword);

    }

    @Test(expected = BadCredentialsException.class)
    public void ChangePasswordState_WithWrongPassword_ShouldThrow(){

        NewPasswordSpec passwordSpec = new NewPasswordSpec();
        passwordSpec.setUsername("user");
        passwordSpec.setCurrentPassword("InvalidPassword");
        passwordSpec.setNewPassword("newTestPassword1");
        passwordSpec.setRepeatNewPassword("newTestPassword1");

        UserModel userModel = new UserModel();
        userModel.setPassword("currentPassword");

        when(userRepository.findByUsername("user")).thenReturn(userModel);

        userService.changePassword(passwordSpec);
    }

    @Test(expected = PasswordsMissMatchException.class)
    public void ChangePasswordState_WithNotMatchingPasswords_ShouldThrow(){
        String name = "name";

        NewPasswordSpec passwordSpec = new NewPasswordSpec();
        passwordSpec.setUsername(name);
        passwordSpec.setCurrentPassword("Current");
        passwordSpec.setNewPassword("newTestPassword1");
        passwordSpec.setRepeatNewPassword("InvalidPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword("current");

        when(userRepository.findByUsername(name)).thenReturn(userModel);

        userService.changePassword(passwordSpec);
    }

}

