package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Settings findByUser(UserModel user);
}
