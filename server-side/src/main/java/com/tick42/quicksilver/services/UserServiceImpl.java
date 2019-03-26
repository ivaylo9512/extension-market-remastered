package com.tick42.quicksilver.services;
import com.tick42.quicksilver.exceptions.InvalidCredentialsException;
import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.DTO.UserDTO;
import com.tick42.quicksilver.models.Spec.ChangeUserPasswordSpec;
import com.tick42.quicksilver.models.Spec.UserSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public UserDTO setState(int id, String state) {
        UserModel userModel = userRepository.findById(id);

        if (userModel == null) {
            throw new UserNotFoundException("UserModel not found.");
        }

        switch (state) {
            case "enable":
                userModel.setIsActive(true);
                break;
            case "block":
                userModel.setIsActive(false);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid userModel state. Use \"enable\" or \"block\".");
        }
        return new UserDTO(userRepository.update(userModel));
    }

    @Override
    public List<UserDTO> findAll(String state) {
        List<UserModel> userModels = new ArrayList<>();

        if (state == null) {
            state = "";
        }

        switch (state) {
            case "active":
                userModels = userRepository.findUsersByState(true);
                break;
            case "blocked":
                userModels = userRepository.findUsersByState(false);
                break;
            case "all":
                userModels = userRepository.findAll();
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid user state. Use \"active\" , \"blocked\" or \"all\".");
        }

        List<UserDTO> usersDto = userModels
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return usersDto;
    }

    @Override
    public UserDTO findById(int id, UserDetails loggedUser) {
        UserModel userModel = userRepository.findById(id);

        if (userModel == null) {
            throw new UserNotFoundException("UserModel doesn't exist.");
        }
        boolean admin = false;
        if(loggedUser != null) {
            Set<String> authorities = AuthorityUtils.authorityListToSet(loggedUser.getAuthorities());
            admin = authorities.contains("ROLE_ADMIN");
        }

        if (!userModel.getIsActive() && !admin) {
            throw new UserProfileUnavailableException("UserModel profile is disabled.");
        }

        return new UserDTO(userModel);
    }

    @Override
    public UserModel register(UserSpec userSpec, String role) {
        UserModel userModel = userRepository.findByUsername(userSpec.getUsername());

        if (userModel != null) {
            throw new UsernameExistsException("Username is already taken.");
        }

        if (!userSpec.getPassword().equals(userSpec.getRepeatPassword())) {
            throw new PasswordsMissMatchException("Passwords must match.");
        }

        userModel = new UserModel(userSpec, role);
        userModel.setPassword(BCrypt.hashpw(userModel.getPassword(),BCrypt.gensalt(4)));
        return userRepository.create(userModel);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserModel foundUser = userRepository.findByUsername(username);
        if(foundUser == null){
            throw new BadCredentialsException("Bad credentials");
        }
        if (!foundUser.getIsActive()) {
            throw new BlockedUserException("User is disabled.");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(foundUser.getRole()));

        return new UserDetails(foundUser, authorities);
    }

    @Override
    public UserDTO changePassword(int id, ChangeUserPasswordSpec changePasswordSpec){
        UserModel userModel = userRepository.findById(id);
        if (!changePasswordSpec.getNewPassword().equals(changePasswordSpec.getRepeatNewPassword())){
            throw new PasswordsMissMatchException("passwords don't match");
        }

        if (!userModel.getPassword().equals(changePasswordSpec.getCurrentPassword())){
            throw new InvalidCredentialsException("Invalid current password.");
        }
        userModel.setPassword(changePasswordSpec.getNewPassword());
        userRepository.update(userModel);
        return new UserDTO(userModel);

    }
}
