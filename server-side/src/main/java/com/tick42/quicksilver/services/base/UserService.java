package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;

import java.util.List;

public interface UserService {
    UserModel setState(int id, String state);

    UserModel create(UserModel user);

    List<UserModel> findAll(String state);

    UserModel findById(int id, UserDetails loggedUser);

    UserModel register(UserSpec userSpec, String role);

    UserModel changePassword(NewPasswordSpec changePasswordSpec);
}
