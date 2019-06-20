package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.DTO.UserDTO;
import com.tick42.quicksilver.models.Spec.ChangeUserPasswordSpec;
import com.tick42.quicksilver.models.Spec.UserSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;


import java.util.List;

public interface UserService {

    UserModel setState(int id, String state);

    UserModel save(UserModel user);

    List<UserModel> findAll(String state);

    UserModel findById(int id, UserDetails loggedUser);

    UserModel register(UserSpec userSpec, String role);

    UserModel changePassword(int id, ChangeUserPasswordSpec changePasswordSpec);
}
