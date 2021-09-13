package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.TagDto;
import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.services.base.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/tag")
public class TagController {
    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping(value = "/{tag}")
    public TagDto findByName(@PathVariable(name = "tag") String tagName) {
        Tag tag = tagService.findByName(tagName);
        TagDto tagDto = new TagDto(tag);
        tagDto.setExtensions(tag.getExtensions()
                .stream()
                .map(ExtensionDto::new)
                .collect(Collectors.toList()));
        return tagDto;
    }
}
