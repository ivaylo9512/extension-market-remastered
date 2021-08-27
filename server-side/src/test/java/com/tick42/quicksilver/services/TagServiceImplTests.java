package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.repositories.base.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class TagServiceImplTests {

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

        Set<Tag> tags = new HashSet<>(tagService.generateTags(tagsString));
        assertEquals(tags.size(), 3);
        assertEquals(tags, Arrays.stream(tagsString.split(" ")).map(Tag::new).collect(Collectors.toSet()));
    }

    @Test
    public void generateTagsWhenTagIsEmptyString(){
        String tagString = "";
        Set<Tag> tags = new HashSet<>(tagService.generateTags(tagString));

        assertEquals(tags.size(), 0);
    }
}
