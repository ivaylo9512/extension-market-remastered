package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.TagDTO;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public TagDTO findByName(@PathVariable(name = "tag") String tagName) {
        Tag tag = tagService.findByName(tagName);
        TagDTO tagDTO = new TagDTO(tag);
        tagDTO.setExtensions(tag.getExtensions()
                .stream()
                .map(this::generateExtensionDTO)
                .collect(Collectors.toList()));
        return tagDTO;
    }

    private ExtensionDTO generateExtensionDTO(Extension extension) {
        ExtensionDTO extensionDTO = new ExtensionDTO(extension);
        if (extension.getGithub() != null) {
            extensionDTO.setGitHubLink(extension.getGithub().getLink());
            extensionDTO.setOpenIssues(extension.getGithub().getOpenIssues());
            extensionDTO.setPullRequests(extension.getGithub().getPullRequests());
            extensionDTO.setGithubId(extension.getGithub().getId());

            if (extension.getGithub().getLastCommit() != null)
                extensionDTO.setLastCommit(extension.getGithub().getLastCommit());

            if (extension.getGithub().getLastSuccess() != null)
                extensionDTO.setLastSuccessfulPullOfData(extension.getGithub().getLastSuccess());

            if (extension.getGithub().getLastFail() != null) {
                extensionDTO.setLastFailedAttemptToCollectData(extension.getGithub().getLastFail());
                extensionDTO.setLastErrorMessage(extension.getGithub().getFailMessage());
            }
        }
        if (extension.getImage() != null)
            extensionDTO.setImageLocation(extension.getImage().getLocation());

        if (extension.getFile() != null)
            extensionDTO.setFileLocation(extension.getFile().getLocation());

        if (extension.getCover() != null)
            extensionDTO.setCoverLocation(extension.getCover().getLocation());

        return extensionDTO;
    }
}
