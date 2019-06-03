package com.tick42.quicksilver.services;
import com.tick42.quicksilver.exceptions.InvalidCredentialsException;
import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.UserDTO;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Spec.ChangeUserPasswordSpec;
import com.tick42.quicksilver.models.Spec.UserSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.ExtensionService;
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
    public UserDTO setState(int userId, String state) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


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
        return generateUserDTO(userRepository.save(user));
    }

    @Override
    public UserDTO save(UserModel user) {
        return generateUserDTO(userRepository.save(user));
    }

    @Override
    public List<UserDTO> findAll(String state) {
        List<UserModel> user;

        if (state == null) {
            state = "";
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

        return user.stream()
                .map(this::generateUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO findById(int userId, UserDetails loggedUser) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean admin = false;
        if(loggedUser != null) {
            Set<String> authorities = AuthorityUtils.authorityListToSet(loggedUser.getAuthorities());
            admin = authorities.contains("ROLE_ADMIN");
        }

        if (!user.getIsActive() && !admin) {
            throw new UserProfileUnavailableException("UserModel profile is unavailable.");
        }

        UserDTO userDTO = generateUserDTO(user);
        userDTO.setExtensions(user.getExtensions()
                .stream()
                .map(this::generateExtensionDTO)
                .collect(Collectors.toList()));
        return userDTO;
    }

    @Override
    public UserModel register(UserSpec userSpec, String role) {
        UserModel user = userRepository.findByUsername(userSpec.getUsername());

        if (user != null) {
            throw new UsernameExistsException("Username is already taken.");
        }

        if (!userSpec.getPassword().equals(userSpec.getRepeatPassword())) {
            throw new PasswordsMissMatchException("Passwords must match.");
        }

        user = new UserModel(userSpec, role);
        user.setPassword(BCrypt.hashpw(user.getPassword(),BCrypt.gensalt(4)));
        return userRepository.save(user);
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
    public UserDTO changePassword(int userId, ChangeUserPasswordSpec changePasswordSpec){
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!changePasswordSpec.getNewPassword().equals(changePasswordSpec.getRepeatNewPassword())){
            throw new PasswordsMissMatchException("passwords don't match");
        }

        if (!user.getPassword().equals(changePasswordSpec.getCurrentPassword())){
            throw new InvalidCredentialsException("Invalid current password.");
        }
        user.setPassword(changePasswordSpec.getNewPassword());
        userRepository.save(user);
        return generateUserDTO(user);

    }
    private UserDTO generateUserDTO(UserModel user){
        UserDTO userDTO = new UserDTO(user);
        if(user.getProfileImage() != null){
            userDTO.setProfileImage(user.getProfileImage().getLocation());
        }
        return userDTO;
    }

    private ExtensionDTO generateExtensionDTO(Extension extension) {
        ExtensionDTO extensionDTO = new ExtensionDTO(extension);
        if (extension.getGithub() != null) {
            extensionDTO.setGitHubLink(extension.getGithub().getLink());
            if (extension.getGithub().getLastCommit() != null) {
                extensionDTO.setLastCommit(extension.getGithub().getLastCommit());
            }
            extensionDTO.setOpenIssues(extension.getGithub().getOpenIssues());
            extensionDTO.setPullRequests(extension.getGithub().getPullRequests());
            if (extension.getGithub().getLastSuccess() != null) {
                extensionDTO.setLastSuccessfulPullOfData(extension.getGithub().getLastSuccess());
            }
            if (extension.getGithub().getLastFail() != null) {
                extensionDTO.setLastFailedAttemptToCollectData(extension.getGithub().getLastFail());
                extensionDTO.setLastErrorMessage(extension.getGithub().getFailMessage());
            }
        }
        if (extension.getImage() != null) {
            extensionDTO.setImageLocation(extension.getImage().getLocation());
        }
        if (extension.getFile() != null) {
            extensionDTO.setFileLocation(extension.getFile().getLocation());
        }
        if (extension.getCover() != null) {
            extensionDTO.setCoverLocation(extension.getCover().getLocation());
        }
        return extensionDTO;
    }
}
