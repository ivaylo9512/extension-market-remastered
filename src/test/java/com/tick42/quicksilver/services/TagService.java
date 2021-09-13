package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.InvalidInputException;
import com.tick42.quicksilver.repositories.base.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TagService {
    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private TagServiceImpl tagService;


    @Test
    public void normalizeTags(){
        String tag = "test tag normalize";
        String expectedTag = "test-tag-normalize";
        tag = tagService.normalize(tag);

        assertEquals(tag, expectedTag);
    }

    @Test
    public void generateTags_WhenTagsDoNotExist(){
        String tagsString = "test, string, tags";
        Set<String> tags = tagService.generateTags(tagsString);

        assertEquals(tags.size(), 3);
        assertEquals(tags, Arrays.stream(tagsString.split(", "))
                .collect(Collectors.toSet()));
    }

    @Test
    public void generateTagsWhenTagIsEmptyString_InvalidInput(){
        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> tagService.saveTags(""));

        assertEquals(thrown.getMessage(), "Tags can't be an empty string.");
    }
}
