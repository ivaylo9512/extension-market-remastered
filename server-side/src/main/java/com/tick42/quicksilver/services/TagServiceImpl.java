package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.TagDTO;
import com.tick42.quicksilver.models.DTO.UserDTO;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.TagRepository;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public TagDTO findByName(String name) {
        Tag tag = tagRepository.findById(name).orElseThrow(() -> new RuntimeException("Tag not found."));
        TagDTO tagDTO = new TagDTO(tag);
        tagDTO.setExtensions(tag.getExtensions()
                .stream()
                .map(this::generateExtensionDTO)
                .collect(Collectors.toList()));
        return new TagDTO();
    }

    @Override
    public String normalize(String name) {
        name = name.trim().replaceAll(" +", "-");
        name = name.toLowerCase();
        return name;
    }

    @Override
    public List<Tag> prepareTags(List<Tag> tags) {
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            Tag existingTag = tagRepository.findById(tag.getName()).orElse(null);
            if (existingTag != null) {
                tags.set(i, existingTag);
            }
        }
        return tags;
    }

    @Override
    public Set<Tag> generateTags(String tagString) {
        tagString = tagString.trim();
        Set<Tag> tags = new HashSet<>();

        if (tagString.equals("")) {
            return tags;
        }

        List<String> tagNames =
                Arrays.stream(tagString.split(","))
                        .map(String::toLowerCase)
                        .map(String::trim)
                        .distinct()
                        .collect(Collectors.toList());
        tagNames.forEach(tagName -> tags.add(tagRepository.save(new Tag(tagName))));
        return tags;
    }

    private ExtensionDTO generateExtensionDTO(Extension extension) {
        ExtensionDTO extensionDTO = new ExtensionDTO(extension);
        if (extension.getGithub() != null) {
            extensionDTO.setGitHubLink(extension.getGithub().getLink());
            if (extension.getGithub().getLastCommit() != null) {
                extensionDTO.setLastCommit(extension.getGithub().getLastCommit());
            }
            extensionDTO.setOpenIssues(extension.getGithub().getOpenIssues());
            extensionDTO.setPullRequests(extension.getGithub().getPullRequests());
            if (extension.getGithub().getLastSuccess() != null) {
                extensionDTO.setLastSuccessfulPullOfData(extension.getGithub().getLastSuccess());
            }
            if (extension.getGithub().getLastFail() != null) {
                extensionDTO.setLastFailedAttemptToCollectData(extension.getGithub().getLastFail());
                extensionDTO.setLastErrorMessage(extension.getGithub().getFailMessage());
            }
        }
        if (extension.getImage() != null) {
            extensionDTO.setImageLocation(extension.getImage().getLocation());
        }
        if (extension.getFile() != null) {
            extensionDTO.setFileLocation(extension.getFile().getLocation());
        }
        return extensionDTO;
    }

}
