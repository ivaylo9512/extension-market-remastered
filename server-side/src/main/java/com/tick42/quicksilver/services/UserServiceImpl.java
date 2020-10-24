package com.tick42.quicksilver.services;
import com.tick42.quicksilver.exceptions.InvalidCredentialsException;
import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserModel setState(int userId, String state) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        switch (state) {
            case "enable":
                user.setIsActive(true);
                break;
            case "block":
                user.setIsActive(false);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid userModel state. Use \"enable\" or \"block\".");
        }
        return userRepository.save(user);
    }

    @Override
    public UserModel create(UserModel user) {
        return userRepository.save(user);
    }

    @Override
    public List<UserModel> findAll(String state) {
        List<UserModel> user;

        if (state == null) {
            throw new InvalidStateException("\"" + state + "\" is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
        }

        switch (state) {
            case "active":
                user = userRepository.findByActive(true);
                break;
            case "blocked":
                user = userRepository.findByActive(false);
                break;
            case "all":
                user = userRepository.findAll();
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
        }

        return user;
    }

    @Override
    public UserModel findById(int userId, UserDetails loggedUser) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        if (!user.getIsActive() && (loggedUser == null ||
                !AuthorityUtils.authorityListToSet(loggedUser.getAuthorities()).contains("Role_ADMIN"))) {
            throw new UserProfileUnavailableException("User is unavailable.");
        }
        return user;
    }

    @Override
    public UserModel register(RegisterSpec newUser, String role) {
        UserModel user = userRepository.findByUsername(newUser.getUsername());

        if (user != null) {
            throw new UsernameExistsException("Username is already taken.");
        }

        user = new UserModel(newUser, role);
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt(4)));

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel foundUser = userRepository.findByUsername(username);
        if(foundUser == null){
            throw new BadCredentialsException("Invalid username or password.");
        }
        if (!foundUser.getIsActive()) {
            throw new BlockedUserException("User is disabled.");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(foundUser.getRole()));

        return new UserDetails(foundUser, authorities);
    }

    @Override
    public UserModel changePassword(NewPasswordSpec passwordSpec){
        UserModel user = userRepository.findByUsername(passwordSpec.getUsername());

        if(user == null){
            throw new EntityNotFoundException("User with" + passwordSpec.getUsername() + "is not found.");
        }

        if (!user.getPassword().equals(passwordSpec.getCurrentPassword())){
            throw new BadCredentialsException("Invalid current password.");
        }
        user.setPassword(passwordSpec.getNewPassword());
        return userRepository.save(user);

    }
}
