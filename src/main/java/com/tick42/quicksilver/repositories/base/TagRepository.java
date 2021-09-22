package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, String> {
}
