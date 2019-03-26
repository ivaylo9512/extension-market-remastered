package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.UserModel;

import java.util.List;

public interface UserRepository {
    UserModel create(UserModel userModel);

    UserModel update(UserModel userModel);

    UserModel findById(int id);

    UserModel findByUsername(String username);

    List<UserModel> findAll();

    List<UserModel> findUsersByState(boolean state);
}
