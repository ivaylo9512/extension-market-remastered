package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Integer> {
}
