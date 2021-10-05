package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.Tag;
import java.util.Set;

public interface TagService {
    Set<Tag> saveTags(String tags);
}
