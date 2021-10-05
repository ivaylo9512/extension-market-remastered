package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.repositories.base.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    @Spy
    private TagServiceImpl tagService;

    @Test
    public void generateTags_WhenTagsDoNotExist(){
        String tagsString = "test, string, tags";
        Set<String> tags = tagService.generateTags(tagsString);

        assertEquals(tags.size(), 3);
        assertEquals(tags, Arrays.stream(tagsString.split(", "))
                .collect(Collectors.toSet()));
    }

    @Test
    public void generateTagsWhenTagIsEmptyString(){
        Set<Tag> tags = tagService.saveTags("");
        assertNull(tags);
    }

    @Test
    public void saveTags(){
        String tagString = "tag, tag1, tag2";

        Tag tag = new Tag("tag");
        Tag tag1 = new Tag("tag1");
        Tag tag2 = new Tag("tag2");

        when(tagRepository.save(eq(tag))).thenReturn(tag);
        when(tagRepository.save(eq(tag1))).thenReturn(tag1);
        when(tagRepository.save(eq(tag2))).thenReturn(tag2);

        Set<Tag> tags = tagService.saveTags(tagString);


        ArgumentCaptor<Tag> tagCaptor = ArgumentCaptor.forClass(Tag.class);

        verify(tagRepository, times(3)).save(tagCaptor.capture());
        verify(tagService, times(1)).generateTags(tagString);

        List<Tag> savedTags = tagCaptor.getAllValues();
        assertTrue(savedTags.contains(new Tag("tag")));
        assertTrue(savedTags.contains(new Tag("tag1")));
        assertTrue(savedTags.contains(new Tag("tag2")));

        assertEquals(3, tags.size());
        assertTrue(tags.contains(tag));
        assertTrue(tags.contains(tag1));
        assertTrue(tags.contains(tag2));
    }

    @Test
    public void saveTags_WithNull() {
        Set<Tag> tags = tagService.saveTags(null);

        assertNull(tags);
    }

    @Test
    public void saveTags_WithEmptyString() {
        Set<Tag> tags = tagService.saveTags("");

        assertNull(tags);
    }
}
