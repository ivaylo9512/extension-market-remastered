package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.EmailToken;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {

    EmailToken findByToken(String token);

    EmailToken findByUser(UserModel user);
}