package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Settings;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByUser(UserModel user);

    @Query(value = "SELECT * FROM settings WHERE id > IF(EXISTS(SELECT * FROM settings WHERE id > :id), :id, 0) LIMIT 1", nativeQuery = true)
    Settings getNextAvailable(@Param("id") long id);
}
