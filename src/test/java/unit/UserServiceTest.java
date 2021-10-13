package unit;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void findById_WithNonExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> userService.findById(1, any(UserDetails.class)));

        assertEquals(thrown.getMessage(), "User not found.");
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
    public void findById_WhenUserProfileIsNotActive_AndCurrentUserIsNotAdmin(){
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

    @Test()
    public void setActive_True(){
        UserModel userModel = new UserModel();
        userModel.setActive(false);
        userModel.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserModel user = userService.setActive(1,true);

        assertTrue(user.isActive());
    }

    @Test()
    public void setActive_False(){
        UserModel userModel = new UserModel();
        userModel.setActive(true);
        userModel.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(any(UserModel.class))).thenReturn(userModel);

        UserModel user = userService.setActive(1,false);

        assertFalse(user.isActive());
    }

    @Test()
    public void setEnabled(){
        UserModel user = new UserModel();
        user.setEnabled(false);
        user.setId(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.setEnabled(true, 1);

        assertTrue(user.isEnabled());
        verify(userRepository, times(1)).save(user);
    }

    @Test()
    public void setEnabled_withNonExistent(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> userService.setEnabled(true, 1));

        assertEquals(thrown.getMessage(), "UserModel not found.");
    }

    @Test
    public void setActive_WithNonExistingUser(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> userService.setActive(1,true));

        assertEquals(thrown.getMessage(), "User not found.");
    }

    @Test
    public void loadUserByUsername_WithBlockedUser() {
        UserModel foundUser = new UserModel();
        foundUser.setPassword(BCrypt.hashpw("password",BCrypt.gensalt(4)));
        foundUser.setActive(false);
        foundUser.setEnabled(true);

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(foundUser));

        BlockedUserException thrown = assertThrows(BlockedUserException.class,
                () -> userService.loadUserByUsername("username"));

        assertEquals(thrown.getMessage(), "User is disabled.");
    }

    @Test
    public void loadUserByUsername_WithNotEnabledUser() {
        UserModel user = new UserModel();
        user.setEnabled(false);
        user.setActive(true);

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        DisabledUserException thrown = assertThrows(DisabledUserException.class,
                () -> userService.loadUserByUsername("username"));

        assertEquals(thrown.getMessage(), "You must complete the registration. Check your email.");
    }

    @Test
    public void loadUserByUsername(){
        UserModel foundUser = new UserModel("username", "test@gmail.com", "password", "ROLE_ADMIN");
        foundUser.setEnabled(true);

        UserDetails userDetails = new UserDetails(foundUser, List.of(
                new SimpleGrantedAuthority(foundUser.getRole())));

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(foundUser));

        UserDetails loggedUser = userService.loadUserByUsername("username");

        assertEquals(userDetails, loggedUser);
        assertEquals(foundUser.getUsername(), loggedUser.getUsername());
    }

    @Test
    public void loadUserByUsername_WithNonExistentUsername(){
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        BadCredentialsException thrown = assertThrows(
                BadCredentialsException.class,
                () -> userService.loadUserByUsername("username")
        );

        assertEquals(thrown.getMessage(), "Invalid username or password.");
    }

    @Test
    public void register_WithAlreadyTakenUsername() {
        UserModel user = new UserModel("test", "test@gmail.com", "test", "ROLE_ADMIN");

        when(userRepository.findFirstByUsernameOrEmail(user.getUsername(), user.getEmail())).thenReturn(Optional.of(user));

        UsernameExistsException thrown = assertThrows(UsernameExistsException.class,
                () -> userService.create(user));

        assertEquals(thrown.getMessage(), "{ \"username\": \"Username is already taken.\" }");
    }

    @Test
    public void register_WithAlreadyTakenEmail() {
        UserModel existingUser = new UserModel("test", "test@gmail.com", "test", "ROLE_ADMIN");
        UserModel user = new UserModel("nonexistent", "test@gmail.com", "test", "ROLE_ADMIN");

        when(userRepository.findFirstByUsernameOrEmail(user.getUsername(), user.getEmail())).thenReturn(Optional.of(existingUser));

        EmailExistsException thrown = assertThrows(EmailExistsException.class,
                () -> userService.create(user)
        );

        assertEquals(thrown.getMessage(), "{ \"email\": \"Email is already taken.\" }");
    }

    @Test
    public void register() {
        UserModel userModel = new UserModel("test", "testEmail@gmail.com", "test", "ROLE_USER");

        when(userRepository.findFirstByUsernameOrEmail(userModel.getUsername(), userModel.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(userModel)).thenReturn(userModel);

        UserModel user = userService.create(userModel);

        assertEquals(user, userModel);
    }

    @Test
    public void changePassword(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("test", "test", authorities, 1);

        NewPasswordSpec passwordSpec = new NewPasswordSpec("user",
                "currentPassword", "newTestPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword(BCrypt.hashpw(passwordSpec.getCurrentPassword(),BCrypt.gensalt(4)));
        userModel.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(userRepository.save(userModel)).thenReturn(userModel);

        UserModel user = userService.changePassword(passwordSpec, loggedUser);
        assertTrue(BCrypt.checkpw("newTestPassword", user.getPassword()));

    }

    @Test
    public void changePassword_WithWrongPassword_BadCredentials(){
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails loggedUser = new UserDetails("test", "test", authorities, 2);

        NewPasswordSpec passwordSpec = new NewPasswordSpec("user",
                "incorrect", "newTestPassword");

        UserModel userModel = new UserModel();
        userModel.setPassword(BCrypt.hashpw("password",BCrypt.gensalt(4)));
        userModel.setEnabled(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(userModel));

        BadCredentialsException thrown = assertThrows(BadCredentialsException.class,
                () -> userService.changePassword(passwordSpec, loggedUser));

        assertEquals(thrown.getMessage(), "Invalid current password.");
    }

    @Test()
    public void delete_WithNonExistentUsername(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> userService.delete(1L, any(UserDetails.class))
        );

        assertEquals(thrown.getMessage(), "UserModel not found.");
    }

    @Test()
    public void delete_WithDifferentLoggedId_ThatIsNotAdmin(){
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new UserDetails("username", "password", authorities, 2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserModel()));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> userService.delete(1L, userDetails)
        );

        assertEquals(thrown.getMessage(), "You are not allowed to modify the user.");
    }

    @Test
    public void delete_WithDifferentLoggedId_ThatIsAdmin(){
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails userDetails = new UserDetails("username", "password", authorities, 2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserModel()));

        userService.delete(1L, userDetails);
    }

    @Test
    public void delete_WithSameLoggedId(){
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new UserDetails("username", "password", authorities, 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new UserModel()));

        userService.delete(1L, userDetails);
    }

    @Test()
    public void changeUserInfo(){
        UserSpec newUser = new UserSpec(1, "newUsername", "nonexistent@gmail.com", "Country", "info");

        UserModel oldUser = new UserModel("username", "username@gmail.com", "password", "ROLE_USER");
        oldUser.setId(1);

        when(userRepository.save(oldUser)).thenReturn(oldUser);
        when(userRepository.findFirstByUsernameOrEmail(newUser.getUsername(), newUser.getEmail())).thenReturn(Optional.empty());

        userService.changeUserInfo(newUser, oldUser);

        assertEquals(oldUser.getUsername(), newUser.getUsername());
        assertEquals(oldUser.getEmail(), newUser.getEmail());
        assertEquals(oldUser.getCountry(), newUser.getCountry());
        assertEquals(oldUser.getInfo(), newUser.getInfo());
    }

    @Test()
    public void changeUserInfo_WhenUsernameIsTaken(){
        UserSpec newUser = new UserSpec(1, "username", "email@gmail.com", "Country", "info");

        UserModel oldUser = new UserModel("oldUsername", "email@gmail.com", "password", "ROLE_USER");
        oldUser.setId(1);

        UserModel existingUser = new UserModel();
        existingUser.setId(2);
        existingUser.setUsername("username");

        when(userRepository.findFirstByUsernameOrEmail(newUser.getUsername(), null)).thenReturn(Optional.of(existingUser));

        UsernameExistsException thrown = assertThrows(
                UsernameExistsException.class,
                () -> userService.changeUserInfo(newUser, oldUser)
        );

        assertEquals(thrown.getMessage(), "{ \"username\": \"Username is already taken.\" }");
    }

    @Test()
    public void changeUserInfo_WhenUsernameAndEmailsAreTheSame(){
        UserSpec newUser = new UserSpec(1, "oldUsername", "email@gmail.com", "Country", "info");

        UserModel oldUser = new UserModel("oldUsername", "email@gmail.com", "password", "ROLE_USER");
        oldUser.setId(1);

        when(userRepository.save(oldUser)).thenReturn(oldUser);

        userService.changeUserInfo(newUser, oldUser);

        assertEquals(oldUser.getUsername(), newUser.getUsername());
        assertEquals(oldUser.getEmail(), newUser.getEmail());
        assertEquals(oldUser.getCountry(), newUser.getCountry());
        assertEquals(oldUser.getInfo(), newUser.getInfo());

        verify(userRepository, times(0)).findFirstByUsernameOrEmail(any(), any());
    }

    @Test()
    public void changeUserInfo_WhenEmailIsTaken(){
        UserSpec newUser = new UserSpec(1, "oldUsername", "taken@gmail.com", "Country", "info");

        UserModel oldUser = new UserModel("oldUsername", "oldEmail@gmail.com", "password", "ROLE_USER");
        oldUser.setId(1);

        UserModel existingUser = new UserModel();
        existingUser.setId(2);
        existingUser.setUsername("username");
        existingUser.setUsername("taken@gmail.com");

        when(userRepository.findFirstByUsernameOrEmail(null, newUser.getEmail())).thenReturn(Optional.of(existingUser));

        EmailExistsException thrown = assertThrows(EmailExistsException.class,
                () -> userService.changeUserInfo(newUser, oldUser)
        );

        assertEquals(thrown.getMessage(), "{ \"email\": \"Email is already taken.\" }");
    }

    @Test
    public void save() {
        UserModel user = new UserModel();

        when(userRepository.save(user)).thenReturn(user);

        UserModel savedUser = userService.save(user);

        assertEquals(user, savedUser);
    }

    @Test
    public void findByName() {
        List<UserModel> users = List.of(new UserModel(), new UserModel());
        Page<UserModel> page = new PageImpl<>(users);

        when(userRepository.findByName("name", "lastName", PageRequest.of(0, 5, Sort.Direction.ASC, "username"))).thenReturn(page);

        Page<UserModel> foundPage = userService.findByName("name", "lastName", 5);

        assertEquals(users, foundPage.getContent());
        assertEquals(2, foundPage.getTotalElements());
    }

    @Test
    public void findByActive() {
        List<UserModel> users = List.of(new UserModel(), new UserModel());
        Page<UserModel> page = new PageImpl<>(users);

        when(userRepository.findByActive(true, "name", "lastName",
                PageRequest.of(0, 5, Sort.Direction.ASC, "username"))).thenReturn(page);

        Page<UserModel> foundPage = userService.findByActive(true, "name", "lastName", 5);

        assertEquals(users, foundPage.getContent());
        assertEquals(2, foundPage.getTotalElements());
    }

    @Test
    public void delete() {
        UserModel user = new UserModel();

        userService.delete(user);

        verify(userRepository, times(1)).delete(user);
    }
}

