package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.HomePageDTO;
import com.tick42.quicksilver.models.DTO.PageDTO;
import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Spec.ExtensionSpec;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.GitHubService;
import com.tick42.quicksilver.services.base.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtensionServiceImpl implements ExtensionService {

    private final ExtensionRepository extensionRepository;
    private final TagService tagService;
    private final GitHubService gitHubService;
    private UserRepository userRepository;
    private Map<Integer, ExtensionDTO> featured = new LinkedHashMap<>();
    private List<ExtensionDTO> mostRecent = new LinkedList<>();
    private int mostRecentQueueLimit = 5;
    private int featuredLimit = 4;

    @Autowired
    public ExtensionServiceImpl(ExtensionRepository extensionRepository, TagService tagService,
                                GitHubService gitHubService, UserRepository userRepository) {
        this.extensionRepository = extensionRepository;
        this.tagService = tagService;
        this.gitHubService = gitHubService;
        this.userRepository = userRepository;
    }

    @Override
    public Extension create(ExtensionSpec extensionSpec, int userId) {

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Extension extension = new Extension(extensionSpec);
        extension.setOwner(user);
        extension.setTags(tagService.generateTags(extensionSpec.getTags()));
        if(extensionSpec.getGithub() != null) {
            extension.setGithub(gitHubService.generateGitHub(extensionSpec.getGithub()));
        }

        return extensionRepository.save(extension);
    }

    @Override
    public Extension findById(int extensionId, UserDetails loggedUser) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));

        checkUserAndExtension(extension, loggedUser);

        return extension;
    }

    @Override
    public Extension update(int extensionId, ExtensionSpec extensionSpec, int userId) {

        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));


        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getId() != extension.getOwner().getId() && !user.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedExtensionModificationException("You are not authorized to edit this extension.");
        }

        extension.setName(extensionSpec.getName());
        extension.setVersion(extensionSpec.getVersion());
        extension.setDescription(extensionSpec.getDescription());

        if(extensionSpec.getGithub() != null) {
            GitHubModel oldGitHub = extension.getGithub();
            GitHubModel newGitHub = gitHubService.generateGitHub(extensionSpec.getGithub());
            extension.setGithub(newGitHub);
            gitHubService.delete(oldGitHub);
        }


        extension.setTags(tagService.generateTags(extensionSpec.getTags()));

        return extensionRepository.save(extension);
    }

    @Override
    public ExtensionDTO save(Extension extension){
        return generateExtensionDTO(extensionRepository.save(extension));
    }

    @Override
    public void delete(int extensionId, int userId) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));


        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if (user.getId() != extension.getOwner().getId() && !user.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedExtensionModificationException("You are not authorized to delete this extension.");
        }

        extensionRepository.delete(extension);
    }

    @Override
    public HomePageDTO getHomeExtensions(Integer mostRecentCount, Integer mostDownloadedCount){
        List<ExtensionDTO> featuredExtensions = new ArrayList<>(featured.values());
        List<ExtensionDTO> mostDownloaded = generateExtensionDTOList(
                extensionRepository.findAllOrderedBy("", PageRequest.of(0, mostDownloadedCount, Sort.Direction.DESC, "timesDownloaded")));

        List<ExtensionDTO> mostRecentExtensions;
        if(mostRecentCount == null){
            mostRecentExtensions = new ArrayList<>(mostRecent);
        }else if(mostRecentCount > mostRecentQueueLimit){
            mostRecentExtensions = generateExtensionDTOList(
                    extensionRepository.findAllOrderedBy("",PageRequest.of(0, mostRecentCount, Sort.Direction.DESC, "uploadDate")));
        }else{
            mostRecentExtensions = new ArrayList<>(mostRecent).subList(0, mostRecentCount);
        }

        return new HomePageDTO(mostRecentExtensions, featuredExtensions, mostDownloaded);


    }

    @Override
    public PageDTO<ExtensionDTO> findAll(String name, String orderBy, Integer page, Integer pageSize) {
        if (page == null || page < 0) {
            page = 0;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        if (name == null) {
            name = "";
        }

        if (orderBy == null) {
            orderBy = "date";
        }

        Long totalResults = extensionRepository.getTotalResults(name);
        int totalPages = (int) Math.ceil(totalResults * 1.0 / pageSize);

        if (page > totalPages && totalResults != 0) {
            throw new InvalidParameterException("Page" + totalPages + " is the last page. Page " + page + " is invalid.");
        }

        List<Extension> extensions;
        switch (orderBy) {
            case "date":
                extensions = extensionRepository.findAllOrderedBy(name,PageRequest.of(page, pageSize, Sort.Direction.DESC, "uploadDate"));
                break;
            case "commits":
                extensions = extensionRepository.findAllOrderedBy(name,PageRequest.of(page, pageSize, Sort.Direction.DESC, "github.lastCommit"));
                break;
            case "name":
                extensions = extensionRepository.findAllOrderedBy(name, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
                break;
            case "downloads":
                extensions = extensionRepository.findAllOrderedBy(name, PageRequest.of(page, pageSize, Sort.Direction.DESC, "timesDownloaded"));
                break;
            default:
                throw new InvalidParameterException("\"" + orderBy + "\" is not a valid parameter. Use \"date\", \"commits\", \"name\" or \"downloads\".");
        }

        List<ExtensionDTO> extensionDTOS = generateExtensionDTOList(extensions);
        return new PageDTO<>(extensionDTOS, page, totalPages, totalResults);
    }

    @Override
    public List<ExtensionDTO> findFeatured() {
        List<Extension> extensions = extensionRepository.findByFeatured(true);
        return generateExtensionDTOList(extensions);
    }

    @Override
    public ExtensionDTO setPublishedState(int extensionId, String state) {

        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));

        switch (state) {
            case "publish":
                extension.isPending(false);
                break;
            case "unpublish":
                extension.isPending(true);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid extension state. Use \"publish\" or \"unpublish\".");
        }

        extensionRepository.save(extension);
        updateMostRecent();
        return generateExtensionDTO(extension);
    }

    @Override
    public ExtensionDTO setFeaturedState(int extensionId, String state) {

        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));


        switch (state) {
            case "feature":
                if(!extension.isFeatured() && featured.size() == featuredLimit){
                    throw new FeaturedLimitException(String.format("Only %s extensions can be featured. To free space first un-feature another extension.", featuredLimit));
                }
                extension.isFeatured(true);
                break;
            case "unfeature":
                extension.isFeatured(false);
                break;
            default:
                throw new InvalidStateException("\"" + state + "\" is not a valid featured state. Use \"feature\" or \"unfeature\".");
        }


        extensionRepository.save(extension);

        ExtensionDTO extensionDTO = generateExtensionDTO(extension);
        if(extensionDTO.isFeatured()){
            featured.put(extensionDTO.getId(), extensionDTO);
        }else{
            featured.remove(extensionId);
        }

        return extensionDTO;
    }

    @Override
    public List<ExtensionDTO> findPending() {
        return generateExtensionDTOList(extensionRepository.findByPending(true));
    }

    @Override
    public List<ExtensionDTO> generateExtensionDTOList(List<Extension> extensions) {
        return extensions.stream()
                .map(this::generateExtensionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ExtensionDTO generateExtensionDTO(Extension extension) {
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

    @Override
    public ExtensionDTO fetchGitHub(int extensionId, int userId) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));


        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if (!user.getRole().equals("ROLE_ADMIN")) {
            throw new UnauthorizedExtensionModificationException("You are not authorized to trigger a github refresh.");
        }

        gitHubService.setRemoteDetails(extension.getGithub());

        extensionRepository.save(extension);

        return generateExtensionDTO(extension);
    }

    @Override
    public ExtensionDTO increaseDownloadCount(int extensionId) {
        Extension extension = extensionRepository.findById(extensionId)
                .orElseThrow(() -> new ExtensionNotFoundException("Extension not found."));

        if(extension.isPending() || !extension.getOwner().getIsActive()) {
            throw new ExtensionUnavailableException("Download count won't increase - the extension is unavailable");
        }

        extension.setTimesDownloaded(extension.getTimesDownloaded() + 1);
        ExtensionDTO extensionDTO = generateExtensionDTO(extensionRepository.save(extension));

        if(mostRecent.contains(extensionDTO)){
            int index = mostRecent.indexOf(extensionDTO);
            mostRecent.set(index, extensionDTO);
        }

        if(featured.containsKey(extensionDTO.getId())){
            featured.replace(extensionDTO.getId(), extensionDTO);
        }
        return extensionDTO;
    }

    private void checkUserAndExtension(Extension extension, UserDetails loggedUser) {
        if (extension == null) {
            throw new ExtensionNotFoundException("Extension doesn't exist.");
        }
        boolean admin = false;
        if(loggedUser != null){
            Set<String> authorities = AuthorityUtils.authorityListToSet(loggedUser.getAuthorities());
            admin = authorities.contains("ROLE_ADMIN");
        }

        if (!extension.getOwner().getIsActive() &&
                ((loggedUser == null) || (!admin))) {
            throw new ExtensionUnavailableException("Extension is unavailable.");
        }

        if (extension.isPending() &&
                ((loggedUser == null) ||
                        (extension.getOwner().getId() != loggedUser.getId() && !admin))) {
            throw new ExtensionUnavailableException("Extension is unavailable.");
        }
    }

    @Override
    public void loadFeatured() {
        extensionRepository.findByFeatured(true).forEach(extension ->
                featured.put(extension.getId(), generateExtensionDTO(extension)));
    }

    @Override
    public void updateMostRecent(){
        mostRecent.clear();
        mostRecent.addAll(generateExtensionDTOList(
                extensionRepository.findAllOrderedBy("",PageRequest.of(0, mostRecentQueueLimit, Sort.Direction.DESC, "uploadDate"))));
    }
}
