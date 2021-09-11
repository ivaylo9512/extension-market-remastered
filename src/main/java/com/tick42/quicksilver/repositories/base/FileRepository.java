package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FileRepository extends JpaRepository<File, Long> {
    @Query("from File where resource_type LIKE :resourceType AND owner LIKE :owner")
    File findByName(String resourceType, long owner);
}
