package com.tick42.quicksilver.services;


import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.DTO.UserDTO;
import com.tick42.quicksilver.models.Spec.ChangeUserPasswordSpec;
import com.tick42.quicksilver.models.Spec.UserSpec;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserModelServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test(expected = UserNotFoundException.class)
    public void findById_withNonExistingUser_shouldThrow() {
        //Arrange
        UserModel userModel = new UserModel();
        when(userRepository.findById(1)).thenReturn(null);

        //Act
        userService.findById(1, userModel);
    }

    @Test(expected = UserProfileUnavailableException.class)
    public void findById_whenUserProfileIsNotActive_andCurrentUserIsNotAdmin_shouldThrow(){
        //Arrange
        UserModel loggedUserModel = new UserModel();
        loggedUserModel.setUsername("test");
        loggedUserModel.setRole("ROLE_USER");
        loggedUserModel.setIsActive(true);
        UserModel blockedUserModel = new UserModel();
        blockedUserModel.setIsActive(false);
        blockedUserModel.setUsername("test");
        blockedUserModel.setRole("ROLE_USER");
        when(userRepository.findById(1)).thenReturn(blockedUserModel);

        //Act
        userService.findById(1, loggedUserModel);
    }
    @Test()
    public void findById_whenUserProfileIsNotActive_andCurrentUserIsAdmin_ShouldReturnUser(){
        //Arrange
        UserModel loggedUserModel = new UserModel();
        loggedUserModel.setUsername("test");
        loggedUserModel.setRole("ROLE_ADMIN");
        loggedUserModel.setIsActive(true);
        UserModel blockedUserModel = new UserModel();
        blockedUserModel.setIsActive(false);
        blockedUserModel.setUsername("test");
        blockedUserModel.setRole("ROLE_USER");
        when(userRepository.findById(1)).thenReturn(blockedUserModel);

        //Act
        userService.findById(1, loggedUserModel);
    }

    @Test
    public void findBlockedUsers_ShouldReturnBlockedUsers() {
        //Arrange
        UserModel userModel = new UserModel();
        UserModel userModel1 = new UserModel();
        userModel.setIsActive(false);
        userModel1.setIsActive(false);
        List<UserModel> userModels = Arrays.asList(userModel, userModel1);

        when(userRepository.findUsersByState(false)).thenReturn(userModels);

        //Act
        List<UserDTO> usersDTO = userService.findAll("blocked");

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

        when(userRepository.findUsersByState(true)).thenReturn(userModels);

        //Act
        List<UserDTO> usersDTO = userService.findAll("active");

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
        List<UserDTO> usersDTO = userService.findAll("all");

        //Assert
        Assert.assertEquals(2, usersDTO.size());
        Assert.assertTrue(usersDTO.get(0).getIsActive());
        Assert.assertFalse(usersDTO.get(1).getIsActive());
    }

    @Test(expected = InvalidStateException.class)
    public void findAll_whenStateNull_ShouldThrow() {
        //Act
        List<UserDTO> usersDTO = userService.findAll(null);
    }

    @Test(expected = InvalidStateException.class)
    public void findAllUsers_WithWrongState_ShouldThrow(){
        //Act
        List<UserDTO> usersDTO = userService.findAll("ActiveUsersInvalidInput");
    }

    @Test()
    public void setUserState_Enable_ShouldReturnEnable(){
        //Arrange
        UserModel userModel = new UserModel();
        userModel.setIsActive(false);
        userModel.setId(1);

        UserDTO userDTO = new UserDTO(userModel);

        when(userRepository.findById(1)).thenReturn(userModel);
        when(userRepository.update(any(UserModel.class))).thenReturn(userModel);
        //act
        userDTO = userService.setState(1,"enable");

        //Assert
        Assert.assertEquals(userDTO.getIsActive(),true);
    }

    @Test()
    public void setUserState_block_ShouldReturnBlocked(){
        //Arrange
        UserModel userModel = new UserModel();
        userModel.setIsActive(true);
        userModel.setId(1);

        UserDTO userDTO = new UserDTO(userModel);

        when(userRepository.findById(1)).thenReturn(userModel);
        when(userRepository.update(any(UserModel.class))).thenReturn(userModel);
        //act
        userDTO = userService.setState(1,"block");

        //Assert
        Assert.assertFalse(userDTO.getIsActive());
    }

    @Test(expected = InvalidStateException.class)
    public void setUserState_WithWrongState_ShouldThrow(){

        //Arrange
        UserModel userModel = new UserModel();

        when(userRepository.findById(1)).thenReturn(userModel);

        //Act
        UserDTO usersDTO = userService.setState(1,"InvalidState");
    }

    @Test(expected = UserNotFoundException.class)
    public void setUserState_WithNonExistingUser_ShouldThrow(){

        when(userRepository.findById(1)).thenReturn(null);

        //Act
        UserDTO usersDTO = userService.setState(1,"Active");
    }

    @Test(expected = InvalidCredentialsException.class)
    public void LoginUserWithWrongPassword_InvalidCredentialsException_shouldThrow() {

        //Arrange
        UserModel userModel = new UserModel();
        userModel.setUsername("test");
        userModel.setPassword(BCrypt.hashpw("k",BCrypt.gensalt(4)));
        UserModel userModelInvalid = new UserModel();
        userModelInvalid.setUsername("test");
        userModelInvalid.setPassword(BCrypt.hashpw("wrong password)",BCrypt.gensalt(4)));

        when(userRepository.findByUsername("test")).thenReturn(userModel);

        //Act
        userService.login(userModelInvalid);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void LoginUserWithWrongUsername_InvalidCredentialsException_shouldThrow() {

        //Arrange
        UserModel userModelInvalid = new UserModel();
        userModelInvalid.setUsername("test");

        when(userRepository.findByUsername("test")).thenReturn(null);

        //Act
        userService.login(userModelInvalid);
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
        userService.login(userModel);
    }

    @Test
    public void LoginUser_ShouldReturnLoggedUser(){
        //Arrange
        UserModel userModel = new UserModel();
        userModel.setUsername("test");
        userModel.setPassword("password");
        userModel.setIsActive(true);

        UserModel foundUserModel = new UserModel();
        foundUserModel.setUsername("test");
        foundUserModel.setPassword(BCrypt.hashpw("password",BCrypt.gensalt(4)));
        foundUserModel.setIsActive(true);
        when(userRepository.findByUsername("test")).thenReturn(foundUserModel);

        //Act
        UserModel loggedUserModel = userService.login(userModel);

        //Assert
        Assert.assertEquals(foundUserModel, loggedUserModel);
    }

    @Test(expected = UsernameExistsException.class)
    public void RegisterUser_WithAlreadyTakenUsername_shouldThrow() {

        //Arrange
        UserModel registeredUserModel = new UserModel();
        registeredUserModel.setUsername("Test");

        UserSpec newRegistration = new UserSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("TestPassword");
        newRegistration.setRepeatPassword("TestPassword");

        when(userRepository.findByUsername("Test")).thenReturn(registeredUserModel);

        //Act
        userService.register(newRegistration, "ROLE_USER");
    }

    @Test(expected = PasswordsMissMatchException.class)
    public void RegisterUser_WithNotMatchingPasswords_shouldThrow() {

        //Arrange
        UserSpec newRegistration = new UserSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("TestPassword");
        newRegistration.setRepeatPassword("TestPasswordMissMatch");

        when(userRepository.findByUsername("Test")).thenReturn(null);

        //Act
        userService.register(newRegistration, "ROLE_USER");
    }

    @Test()
    public void SuccessfulRegistration_withAvailable() {

        //Arrange

        UserSpec newRegistration = new UserSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("testPassword");
        newRegistration.setRepeatPassword("testPassword");
        when(userRepository.findByUsername("Test")).thenReturn(null);

        //Act
        userService.register(newRegistration, "ROLE_USER");
    }

    @Test()
    public void SuccessfulRegistration_Role_User() {
        //Arrange
        UserSpec newRegistration = new UserSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("testPassword");
        newRegistration.setRepeatPassword("testPassword");

        UserModel newUserModel = new UserModel();
        newUserModel.setUsername("Test");
        newUserModel.setPassword("testPassword");
        newUserModel.setRole("USER_ROLE");

        when(userRepository.findByUsername("Test")).thenReturn(null);
        when(userRepository.create(any(UserModel.class))).thenReturn(newUserModel);
        //Act
        UserModel userModel = userService.register(newRegistration, "ROLE_USER");

        //Assert
        Assert.assertEquals(userModel.getRole(),"USER_ROLE");
    }

    @Test()
    public void SuccessfulRegistration_Role_Admin_ShouldReturnAdminUser() {
        //Arrange
        UserSpec newRegistration = new UserSpec();
        newRegistration.setUsername("Test");
        newRegistration.setPassword("testPassword");
        newRegistration.setRepeatPassword("testPassword");

        UserModel newUserModel = new UserModel();
        newUserModel.setUsername("Test");
        newUserModel.setPassword("testPassword");
        newUserModel.setRole("ADMIN_ROLE");

        when(userRepository.findByUsername("Test")).thenReturn(null);
        when(userRepository.create(any(UserModel.class))).thenReturn(newUserModel);
        //Act
        UserModel userModel = userService.register(newRegistration, "ADMIN_ROLE");

        //Assert
        Assert.assertEquals(userModel.getRole(),"ADMIN_ROLE");
    }

    @Test
    public void ChangePasswordState(){

        ChangeUserPasswordSpec passwordSpec = new ChangeUserPasswordSpec();
        passwordSpec.setCurrentPassword("currentPassword");
        passwordSpec.setNewPassword("newTestPassword1");
        passwordSpec.setRepeatNewPassword("newTestPassword1");

        UserModel userModel = new UserModel();
        userModel.setPassword("currentPassword");

        when(userRepository.findById(1)).thenReturn(userModel);

        userService.changePassword(1,passwordSpec);


        //Assert
        Assert.assertEquals(userModel.getPassword(),"newTestPassword1");

    }

    @Test(expected = InvalidCredentialsException.class)
    public void ChangePasswordState_WithWrongPassword_ShouldThrow(){

        ChangeUserPasswordSpec passwordSpec = new ChangeUserPasswordSpec();
        passwordSpec.setCurrentPassword("InvalidPassword");
        passwordSpec.setNewPassword("newTestPassword1");
        passwordSpec.setRepeatNewPassword("newTestPassword1");

        UserModel userModel = new UserModel();
        userModel.setPassword("currentPassword");

        when(userRepository.findById(1)).thenReturn(userModel);

        userService.changePassword(1,passwordSpec);
    }

    @Test(expected = PasswordsMissMatchException.class)
    public void ChangePasswordState_WithNotMatchingPasswords_ShouldThrow(){

        ChangeUserPasswordSpec passwordSpec = new ChangeUserPasswordSpec();
        passwordSpec.setCurrentPassword("Current");
        passwordSpec.setNewPassword("newTestPassword1");
        passwordSpec.setRepeatNewPassword("InvalidPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword("current");

        when(userRepository.findById(1)).thenReturn(userModel);

        userService.changePassword(1,passwordSpec);
    }


}

