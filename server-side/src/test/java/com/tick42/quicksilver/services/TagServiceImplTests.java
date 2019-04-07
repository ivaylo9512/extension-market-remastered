package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.repositories.base.TagRepository;
import com.tick42.quicksilver.services.base.TagService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.HashSet;
import java.util.Set;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TagServiceImplTests {

    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private TagServiceImpl tagService;


    @Test
    public void normalizeTags(){
        //Arrange
        String tag = "test tag normalize";
        String expectedTag = "test-tag-normalize";
        //Act
        tag = tagService.normalize(tag);

        //Assert
        Assert.assertEquals(tag,expectedTag);

    }



    @Test
    public void generateTags_WhenTagsAreNonexistent_ShouldReturnTags(){
        //Arrange
        Tag test = new Tag("test");
        Tag string = new Tag("string");
        Tag tag = new Tag("tags");
        String tagsString = "test,string,tags";

        //Act
        Set<Tag> tags = new HashSet<>(tagService.generateTags(tagsString));

        //Assert
        Assert.assertEquals(tags.size(), 3);

    }

    @Test
    public void generateTagsWhenTagIsEmptyString(){

        //Arrange
        String tagString = "";

        //Act
        Set<Tag> tags = new HashSet<>(tagService.generateTags(tagString));

        //Assert
        Assert.assertEquals(tags.size(), 0);
    }
}
