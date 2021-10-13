package unit;

import com.tick42.quicksilver.controllers.UserController;
import com.tick42.quicksilver.exceptions.UnauthorizedException;
import com.tick42.quicksilver.models.Dtos.PageDto;
import com.tick42.quicksilver.models.Dtos.UserDto;
import com.tick42.quicksilver.models.EmailToken;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.FileServiceImpl;
import com.tick42.quicksilver.services.UserServiceImpl;
import com.tick42.quicksilver.services.base.EmailTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserServiceImpl userService;

    @Mock
    private FileServiceImpl fileService;

    @Mock
    private EmailTokenService emailTokenService;

    @InjectMocks
    @Spy
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
    public void register() throws IOException, MessagingException {
        HttpServletResponse response = new MockHttpServletResponse();
        RegisterSpec register = new RegisterSpec("username", "email", "password", multipartFile, "Bulgaria", "info");

        userModel.setProfileImage(profileImage);
        userModel.setRole("ROLE_USER");

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        when(userService.create(any(UserModel.class))).thenReturn(userModel);
        when(fileService.generate(multipartFile, "profileImage", "image")).thenReturn(profileImage);
        doNothing().when(emailTokenService).sendVerificationEmail(userModel);

        userController.register(register, response);

        verify(fileService, times(1)).save("profileImage1", multipartFile);
        verify(emailTokenService, times(1)).sendVerificationEmail(userModel);
        verify(userService).create(captor.capture());
        UserModel passedToCreate = captor.getValue();

        assertEquals(passedToCreate.getUsername(), userModel.getUsername());
        assertEquals(passedToCreate.getProfileImage(), profileImage);
        assertEquals(passedToCreate.getProfileImage().getOwner().getUsername(), userModel.getUsername());
        assertEquals(passedToCreate.getRole(), userModel.getRole());
    }

    @Test
    public void registerAdmin() throws IOException {
        RegisterSpec register = new RegisterSpec("username", "email", "password", multipartFile, "Bulgaria", "info");

        userModel.setProfileImage(profileImage);
        userModel.setRole("ROLE_ADMIN");

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        when(userService.create(any(UserModel.class))).thenReturn(userModel);
        when(fileService.generate(multipartFile, "profileImage", "image")).thenReturn(profileImage);

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
    public void activate() throws IOException {
        MockHttpServletResponse response = Mockito.spy(new MockHttpServletResponse());
        EmailToken token = new EmailToken();
        UserModel user = new UserModel();

        user.setEnabled(false);
        token.setExpiryDate(LocalDateTime.now().plusDays(1));
        token.setUser(user);

        when(emailTokenService.findByToken("token")).thenReturn(token);

        userController.activate("token", response);

        assertTrue(user.isEnabled());
        verify(userService, times(1)).save(user);
        verify(emailTokenService, times(1)).delete(token);
        verify(response, times(1)).sendRedirect("https://localhost:4200");
    }

    @Test
    public void activate_WithExpiredToken() throws IOException {
        MockHttpServletResponse response = Mockito.spy(new MockHttpServletResponse());
        EmailToken token = new EmailToken();
        UserModel user = new UserModel();

        user.setEnabled(false);
        token.setExpiryDate(LocalDateTime.now().minusDays(1));
        token.setUser(user);

        when(emailTokenService.findByToken("token")).thenReturn(token);

        UnauthorizedException thrown = assertThrows(UnauthorizedException.class,
                () -> userController.activate("token", response));

        assertEquals(thrown.getMessage(), "Token has expired. Repeat your registration.");
        assertFalse(user.isEnabled());
        verify(userService, times(0)).save(user);
        verify(userService, times(1)).delete(user);
        verify(emailTokenService, times(1)).delete(token);
        verify(response, times(0)).sendRedirect("https://localhost:4200");
    }

    @Test
    public void findAll_IsActiveTrue(){
        UserModel user = new UserModel(3, "username", "email", "password", "role", "info", "country");
        UserModel user1 = new UserModel(5);
        List<UserModel> users = List.of(user, user1);

        when(userService.findByActive(true, "name", "lastName", 5)).thenReturn(new PageImpl<>(users));

        PageDto<UserDto> page = userController.findAll(true, "name", 5, "lastName");

        UserDto foundUser = page.getData().get(0);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(1).getId(), 5);
        assertEquals(foundUser.getId(), 3);
        assertEquals(foundUser.getUsername(), user.getUsername());
        assertEquals(foundUser.getEmail(), user.getEmail());
        assertEquals(foundUser.getCountry(), user.getCountry());
        assertEquals(foundUser.getRole(), user.getRole());
        assertEquals(foundUser.getInfo(), user.getInfo());
    }

    @Test
    public void findAll_IsActiveFalse(){
        UserModel user = new UserModel(3, "username", "email", "password", "role", "info", "country");
        UserModel user1 = new UserModel(5);
        List<UserModel> users = List.of(user, user1);

        when(userService.findByActive(false, "name", "lastName", 5)).thenReturn(new PageImpl<>(users));

        PageDto<UserDto> page = userController.findAll(false, "name", 5, "lastName");

        UserDto foundUser = page.getData().get(0);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(1).getId(), 5);
        assertEquals(foundUser.getId(), 3);
        assertEquals(foundUser.getUsername(), user.getUsername());
        assertEquals(foundUser.getEmail(), user.getEmail());
        assertEquals(foundUser.getCountry(), user.getCountry());
        assertEquals(foundUser.getRole(), user.getRole());
        assertEquals(foundUser.getInfo(), user.getInfo());
    }

    @Test
    public void findAll_IsActiveNull(){
        UserModel user = new UserModel(3, "username", "email", "password", "role", "info", "country");
        UserModel user1 = new UserModel(5);
        List<UserModel> users = List.of(user, user1);

        when(userService.findByName("name", "lastName", 5)).thenReturn(new PageImpl<>(users));

        PageDto<UserDto> page = userController.findAll(null, "name", 5, "lastName");

        UserDto foundUser = page.getData().get(0);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(1).getId(), 5);
        assertEquals(foundUser.getId(), 3);
        assertEquals(foundUser.getUsername(), user.getUsername());
        assertEquals(foundUser.getEmail(), user.getEmail());
        assertEquals(foundUser.getCountry(), user.getCountry());
        assertEquals(foundUser.getRole(), user.getRole());
        assertEquals(foundUser.getInfo(), user.getInfo());
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
    public void delete(){
        userModel.setProfileImage(profileImage);
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userService.findById(user.getId(), user)).thenReturn(userModel);

        userController.delete(user.getId());

        verify(userService, times(1)).delete(userModel);
        verify(fileService, times(1)).deleteFromSystem("profileImage" + userModel.getId() + "." + userModel.getProfileImage().getExtensionType());
    }

    @Test
    public void changeUserInfo() throws IOException {
        userModel.setProfileImage(profileImage);
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockMultipartFile newFile = new MockMultipartFile("imageTest", "imageTest.svg", "image/svg", "imageTest".getBytes());
        File file = new File();
        file.setExtensionType("svg");
        file.setResourceType("profileImage");

        UserSpec userSpec = new UserSpec();
        userSpec.setProfileImage(newFile);

        when(userService.findById(user.getId(), user)).thenReturn(userModel);
        when(userService.changeUserInfo(userSpec, userModel)).thenReturn(userModel);
        when(fileService.generate(newFile, "profileImage", "image")).thenReturn(file);

        UserDto userDto = userController.changeUserInfo(userSpec);

        assertUser(userDto, userModel);

        verify(fileService, times(1)).save(file.getResourceType() + user.getId(), newFile);
        verify(fileService, times(1)).deleteFromSystem("profileImage" + user.getId() + "." + profileImage.getExtensionType());
    }

    @Test
    public void changeUserInfo_WithoutNewFile() throws IOException {
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserSpec userSpec = new UserSpec();

        when(userService.findById(user.getId(), user)).thenReturn(userModel);
        when(userService.changeUserInfo(userSpec, userModel)).thenReturn(userModel);
        when(userController.generateFile(userSpec.getProfileImage(), userModel.getProfileImage())).thenReturn(null);

        UserDto userDto = userController.changeUserInfo(userSpec);

        assertUser(userDto, userModel);

        verify(fileService, times(0)).save(any(String.class), any(MultipartFile.class));
        verify(fileService, times(0)).deleteFromSystem(any(String.class));
    }

    @Test
    public void changeUserInfo_withNullImageName() throws IOException {
        userModel.setProfileImage(null);
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockMultipartFile newFile = new MockMultipartFile("imageTest", "imageTest.svg", "image/svg", "imageTest".getBytes());
        File file = new File();
        file.setExtensionType("svg");
        file.setResourceType("profileImage");

        UserSpec userSpec = new UserSpec();
        userSpec.setProfileImage(newFile);

        when(userService.findById(user.getId(), user)).thenReturn(userModel);
        when(userService.changeUserInfo(userSpec, userModel)).thenReturn(userModel);
        when(fileService.generate(newFile, "profileImage", "image")).thenReturn(file);

        UserDto userDto = userController.changeUserInfo(userSpec);

        assertUser(userDto, userModel);

        verify(fileService, times(1)).save(file.getResourceType() + user.getId(), newFile);
        verify(fileService, times(0)).deleteFromSystem(any(String.class));
    }

    @Test
    public void changeUserInfo_withSameImageName() throws IOException {
        userModel.setProfileImage(profileImage);
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockMultipartFile newFile = new MockMultipartFile("imageTest", "imageTest.png", "image/png", "imageTest".getBytes());
        File file = new File();
        file.setExtensionType("png");
        file.setResourceType("profileImage");

        UserSpec userSpec = new UserSpec();
        userSpec.setProfileImage(newFile);

        when(userService.findById(user.getId(), user)).thenReturn(userModel);
        when(userService.changeUserInfo(userSpec, userModel)).thenReturn(userModel);
        when(fileService.generate(newFile, "profileImage", "image")).thenReturn(file);

        UserDto userDto = userController.changeUserInfo(userSpec);

        assertUser(userDto, userModel);

        verify(fileService, times(1)).save(file.getResourceType() + user.getId(), newFile);
        verify(fileService, times(0)).deleteFromSystem(any(String.class));
    }

    @Test
    public void generateFile(){
        File file = new File();
        file.setId(5);

        File newFile = new File();

        when(fileService.generate(multipartFile, "profileImage", "image")).thenReturn(newFile);

        userController.generateFile(multipartFile, file);

        assertEquals(newFile.getId(), 5);
    }

    @Test
    public void generateFile_WithNullFile(){
        File newFile = new File();

        when(fileService.generate(multipartFile, "profileImage", "image")).thenReturn(newFile);

        userController.generateFile(multipartFile, null);

        assertEquals(newFile.getId(), 0);
    }

    @Test
    public void generateFile_WithNullMultipartFile(){
        File file = userController.generateFile(null, new File());

        assertNull(file);
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
        when(userService.setActive(1L, true)).thenReturn(userModel);

        UserDto userDto = userController.setActive(true, 1L);

        assertUser(userDto, userModel);
    }
}
