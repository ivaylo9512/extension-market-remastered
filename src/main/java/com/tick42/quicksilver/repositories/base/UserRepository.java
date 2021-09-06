package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    UserModel findByUsername(String username);

    List<UserModel> findByActive(boolean state);
}
