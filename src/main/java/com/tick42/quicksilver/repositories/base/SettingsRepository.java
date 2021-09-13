package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByUser(UserModel user);
}
