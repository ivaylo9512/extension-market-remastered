package com.tick42.quicksilver.repositories.base;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, String> {
}
