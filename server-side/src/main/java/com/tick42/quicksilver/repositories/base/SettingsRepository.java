package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Settings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<Settings, Integer> {
    Settings findByUser(int user);
}
