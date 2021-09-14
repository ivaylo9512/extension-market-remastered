package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    @Query("from File where resource_type LIKE :resourceType AND owner LIKE :owner")
    Optional<File> findByName(String resourceType, @Param("owner") UserModel owner);
}
