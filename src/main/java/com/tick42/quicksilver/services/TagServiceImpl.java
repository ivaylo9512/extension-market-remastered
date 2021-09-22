package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.InvalidInputException;
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
    public String normalize(String name) {
        name = name.trim().replaceAll(" +", "-");
        name = name.toLowerCase();
        return name;
    }

    @Override
    public Set<Tag> saveTags(String tagString){
        Set<String> tags = generateTags(tagString);

        if(tagString.equals("")){
            throw new InvalidInputException("Tags can't be an empty string.");
        }
        return tags.stream().map(tagName -> tagRepository.save(new Tag(tagName)))
                .collect(Collectors.toSet());
    }

    public Set<String> generateTags(String tagString) {
        tagString = tagString.replace(" ", "");

        return Arrays.stream(tagString.split(","))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
