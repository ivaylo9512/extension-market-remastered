package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.TagDto;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/tag")
public class TagController {

    private final TagService tagService;
    private final ExtensionService extensionService;

    @Autowired
    public TagController(TagService tagService, ExtensionService extensionService) {
        this.tagService = tagService;
        this.extensionService = extensionService;
    }

    @GetMapping(value = "/{tag}")
    public TagDto findByName(@PathVariable(name = "tag") String tagName) {
        Tag tag = tagService.findByName(tagName);
        TagDto tagDto = new TagDto(tag);
        tagDto.setExtensions(tag.getExtensions()
                .stream()
                .map(this::generateExtensionDTO)
                .collect(Collectors.toList()));
        return tagDto;
    }

    private ExtensionDto generateExtensionDTO(Extension extension) {
        ExtensionDto extensionDto = new ExtensionDto(extension);
        if (extension.getGithub() != null) {
            extensionDto.setGitHubLink(extension.getGithub().getLink());
            extensionDto.setOpenIssues(extension.getGithub().getOpenIssues());
            extensionDto.setPullRequests(extension.getGithub().getPullRequests());
            extensionDto.setGithubId(extension.getGithub().getId());

            if (extension.getGithub().getLastCommit() != null)
                extensionDto.setLastCommit(extension.getGithub().getLastCommit());

            if (extension.getGithub().getLastSuccess() != null)
                extensionDto.setLastSuccessfulPullOfData(extension.getGithub().getLastSuccess());

            if (extension.getGithub().getLastFail() != null) {
                extensionDto.setLastFailedAttemptToCollectData(extension.getGithub().getLastFail());
                extensionDto.setLastErrorMessage(extension.getGithub().getFailMessage());
            }
        }

        String base = "http://localhost:8090/api/download/";
        if (extension.getImage() != null)
            extensionDto.setImageLocation(extension.getImage());

        if (extension.getFile() != null)
            extensionDto.setFileLocation(extension.getFile());

        if (extension.getCover() != null)
            extensionDto.setCoverLocation(extension.getCover());

        return extensionDto;
    }
}
