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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserModel setState(long userId, String state) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        switch (state) {
            case "enable" -> user.setActive(true);
            case "block" -> user.setActive(false);
            default -> throw new InvalidInputException("\"" + state + "\" is not a valid userModel state. Use \"enable\" or \"block\".");
        }

        return userRepository.save(user);
    }

    @Override
    public UserModel create(UserModel user) {
        UserModel existingUser = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail());
        if (existingUser != null) {
            if(existingUser.getUsername().equals(user.getUsername())){
                throw new UsernameExistsException("Username is already taken.");
            }
            throw new EmailExistsException("Email is already taken.");
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(4)));
        return userRepository.save(user);
    }

    @Override
    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }

    @Override
    public List<UserModel> findAll(String state) {
        List<UserModel> user;

        if (state == null) {
            throw new InvalidInputException("State is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
        }

        user = switch (state) {
            case "active" -> userRepository.findByIsActive(true);
            case "blocked" -> userRepository.findByIsActive(false);
            case "all" -> userRepository.findAll();
            default -> throw new InvalidInputException("\"" + state + "\" is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
        };

        return user;
    }

    @Override
    public UserModel findById(long userId, UserDetails loggedUser) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        if(loggedUser != null && AuthorityUtils.authorityListToSet(loggedUser.getAuthorities()).contains("ROLE_ADMIN")){
            return user;
        }

        if (!user.isActive()) {
            throw new UnauthorizedException("User is unavailable.");
        }

        if(!user.isEnabled()){
            throw new DisabledUserException("You must complete the registration. Check your email.");
        }

        return user;
    }

    @Override
    public UserModel getById(long id){
        return userRepository.getById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel foundUser = userRepository.findByUsername(username);
        if(foundUser == null){
            throw new BadCredentialsException("Invalid username or password.");
        }
        if (!foundUser.isActive()) {
            throw new BlockedUserException("User is disabled.");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(foundUser.getRole()));

        return new UserDetails(foundUser, authorities);
    }

    @Override
    public UserModel changePassword(NewPasswordSpec passwordSpec, UserDetails loggedUser){
        UserModel user = this.findById(loggedUser.getId(), loggedUser);

        if (!BCrypt.checkpw(passwordSpec.getCurrentPassword(), user.getPassword())){
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
        user.setEmail(userSpec.getEmail());
        user.setCountry(userSpec.getCountry());
        user.setInfo(userSpec.getInfo());

        return userRepository.save(user);
    }

    @Override
    public void setEnabled(boolean state, long id){
        UserModel user = userRepository.getById(id);
        user.setEnabled(true);

        userRepository.save(user);
    }
}
