package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.repositories.base.TagRepository;
import com.tick42.quicksilver.services.base.TagService;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Set<Tag> saveTags(String tagString){
        if(tagString == null || tagString.equals("")){
            return null;
        }

        return generateTags(tagString).stream().map(tagName -> tagRepository.save(new Tag(tagName)))
                .collect(Collectors.toSet());
    }

    public Set<String> generateTags(String tagString) {
        tagString = tagString.replace(" ", "");

        return Arrays.stream(tagString.split(","))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
