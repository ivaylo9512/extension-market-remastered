package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {
    Optional<EmailToken> findByToken(String token);
}