package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.specs.NewPasswordSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.UserSpec;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserModel setActive(long id, boolean state);

    UserModel create(UserModel user);

    UserModel save(UserModel user);

    UserModel findById(long id, UserDetails loggedUser);

    UserModel getById(long id);

    UserModel changePassword(NewPasswordSpec changePasswordSpec, UserDetails loggedUser);

    UserModel changeUserInfo(UserSpec userSpec, UserDetails loggedUser);

    void setEnabled(boolean state, long id);

    void delete(long id, UserDetails loggedUser);

    Page<UserModel> findByName(String name, String lastName, int pageSize);

    Page<UserModel> findByActive(boolean isActive, String name, String lastName, int pageSize);

    void delete(UserModel user);
}
