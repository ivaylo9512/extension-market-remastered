package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.UserSpec;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserModel setState(long id, String state);

    UserModel create(UserModel user);

    List<UserModel> findAll(String state);

    UserModel save(UserModel user);

    UserModel findById(long id, UserDetails loggedUser);

    UserModel changePassword(NewPasswordSpec changePasswordSpec, UserDetails loggedUser);

    UserModel changeUserInfo(UserSpec userSpec, UserDetails loggedUser);

    void setEnabled(boolean state, long id);
}
