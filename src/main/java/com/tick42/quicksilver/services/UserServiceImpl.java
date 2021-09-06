package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.UserService;
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

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
    }

    @Override
    public UserModel setState(long userId, String state) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        switch (state) {
            case "enable" -> user.setIsActive(true);
            case "block" -> user.setIsActive(false);
            default -> throw new InvalidInputException("\"" + state + "\" is not a valid userModel state. Use \"enable\" or \"block\".");
        }

        return userRepository.save(user);
    }

    @Override
    public UserModel create(UserModel user) {
        UserModel existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser != null) {
            throw new UsernameExistsException("Username is already taken.");
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt(4)));
        return userRepository.save(user);
    }

    @Override
    public List<UserModel> findAll(String state) {
        List<UserModel> user;

        if (state == null) {
            throw new InvalidInputException("State is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
        }

        user = switch (state) {
            case "active" -> userRepository.findByActive(true);
            case "blocked" -> userRepository.findByActive(false);
            case "all" -> userRepository.findAll();
            default -> throw new InvalidInputException("\"" + state + "\" is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
        };

        return user;
    }

    @Override
    public UserModel findById(long userId, UserDetails loggedUser) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        if (!user.getIsActive() && (loggedUser == null ||
                !AuthorityUtils.authorityListToSet(loggedUser.getAuthorities()).contains("ROLE_ADMIN"))) {
            throw new UnauthorizedException("User is unavailable.");
        }

        return user;
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
    public UserModel changePassword(NewPasswordSpec passwordSpec, UserDetails loggedUser){
        UserModel user = this.findById(loggedUser.getId(), loggedUser);

        if (!user.getPassword().equals(passwordSpec.getCurrentPassword())){
            throw new BadCredentialsException("Invalid current password.");
        }

        user.setPassword(BCrypt.hashpw(passwordSpec.getNewPassword(),BCrypt.gensalt(4)));
        return userRepository.save(user);
    }

    @Override
    public UserModel changeUserInfo(UserSpec userSpec, UserDetails loggedUser){
        if(userSpec.getId() != loggedUser.getId() &&
                !loggedUser.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))){
            throw new UnauthorizedException("Unauthorized");
        }

        UserModel user = userRepository.findById(userSpec.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        if(!user.getUsername().equals(userSpec.getUsername())){
            UserModel existingUser = userRepository.findByUsername(userSpec.getUsername());

            if(existingUser != null){
                throw new UsernameExistsException("Username is already taken.");
            }
        }

        user.setUsername(userSpec.getUsername());
        user.setCountry(userSpec.getCountry());
        user.setInfo(userSpec.getInfo());

        return userRepository.save(user);
    }
}
