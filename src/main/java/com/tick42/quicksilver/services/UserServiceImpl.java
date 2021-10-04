package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public UserModel setActive(long userId, boolean state) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        user.setActive(state);
        return userRepository.save(user);
    }

    @Override
    public UserModel create(UserModel user) {
        UserModel existingUser = userRepository.findFirstByUsernameOrEmail(user.getUsername(), user.getEmail());
        if (existingUser != null) {
            if(existingUser.getUsername().equals(user.getUsername())){
                throw new UsernameExistsException("{ \"username\": \"Username is already taken.\" }");
            }
            throw new EmailExistsException("{ \"email\": \"Email is already taken.\" }");
        }

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(4)));
        return userRepository.save(user);
    }

    @Override
    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }

    @Override
    public Page<UserModel> findByName(String name, String lastName, int pageSize) {
        return userRepository.findByName(name, lastName, PageRequest.of(0, pageSize, Sort.Direction.ASC, "username"));
    }

    @Override
    public Page<UserModel> findByActive(boolean isActive, String name, String lastName, int pageSize) {
        return userRepository.findByActive(isActive, name, lastName, PageRequest.of(0, pageSize, Sort.Direction.ASC, "username"));
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
                .orElseThrow(() -> new EntityNotFoundException("UserModel not found."));

        if(!user.getUsername().equals(userSpec.getUsername()) || !user.getEmail().equals(userSpec.getEmail())){
            UserModel existingUser = userRepository.findFirstByUsernameOrEmail(userSpec.getUsername(), userSpec.getEmail());

            if(existingUser != null){
                if(existingUser.getUsername().equals(userSpec.getUsername())){
                    throw new UsernameExistsException("{ \"username\": \"Username is already taken.\" }");
                }
                throw new EmailExistsException("{ \"email\": \"Email is already taken.\" }");
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
        UserModel user = userRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("UserModel not found."));
        user.setEnabled(state);

        userRepository.save(user);
    }

    @Override
    public void delete(long id, UserDetails loggedUser) {
        UserModel user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("UserModel not found."));

        if(id != loggedUser.getId() &&
                !AuthorityUtils.authorityListToSet(loggedUser.getAuthorities()).contains("ROLE_ADMIN")){
            throw new UnauthorizedException("You are not allowed to modify the user.");
        }

        userRepository.delete(user);
    }

    @Override
    public void delete(UserModel user) {
        userRepository.delete(user);
    }
}
