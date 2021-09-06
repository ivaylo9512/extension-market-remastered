package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.HomePageDto;
import com.tick42.quicksilver.models.Dtos.PageDto;
import com.tick42.quicksilver.models.specs.ExtensionSpec;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.*;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/extensions")
public class ExtensionController {
    private final ExtensionService extensionService;
    private final FileService fileService;
    private final RatingService ratingService;
    private final UserService userService;
    private final TagService tagService;
    private final GitHubService gitHubService;

    public ExtensionController(ExtensionService extensionService, FileService fileService, RatingService ratingService, UserService userService, TagService tagService, GitHubService gitHubService) {
        this.extensionService = extensionService;
        this.fileService = fileService;
        this.ratingService = ratingService;
        this.userService = userService;
        this.tagService = tagService;
        this.gitHubService = gitHubService;
    }

    @GetMapping("/getHomeExtensions")
    public HomePageDto getHomeExtensions(
            @RequestParam(name = "mostRecentCount", required = false) Integer mostRecentCount,
            @RequestParam(name = "mostDownloadedCount") Integer mostDownloadedCount){

        List<ExtensionDto> mostRecent = generateExtensionDTOList(extensionService.findMostRecent(mostRecentCount));
        List<ExtensionDto> featured = generateExtensionDTOList(extensionService.findFeatured());
        List<ExtensionDto> mostDownloaded = generateExtensionDTOList(extensionService.findMostDownloaded(mostDownloadedCount));
        return new HomePageDto(mostRecent, featured, mostDownloaded);
    }

    @GetMapping("/filter")
    public PageDto<ExtensionDto> findPageWithCriteria(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "orderBy", required = false) String orderBy,
            @RequestParam(name = "page", required = false) Integer requestedPage,
            @RequestParam(name = "perPage", required = false) Integer perPage) {

        PageDto<Extension> page = extensionService.findPageWithCriteria(name, orderBy, requestedPage, perPage);
        PageDto<ExtensionDto> pageDto = new PageDto<>(page);
        pageDto.setExtensions(generateExtensionDTOList(page.getExtensions()));
        return pageDto;
    }

    @GetMapping("/{id}")
    public ExtensionDto findById(@PathVariable(name = "id") long extensionId, HttpServletRequest request) {
        UserDetails loggedUser = null;
        int rating = 0;
        String token = request.getHeader("Authorization");

        if(token != null){
            loggedUser = Jwt.validate(request.getHeader("Authorization").substring(6));
            rating = ratingService.userRatingForExtension(extensionId, loggedUser.getId());
        }

        ExtensionDto extensionDto = new ExtensionDto(extensionService.findById(extensionId, loggedUser));
        extensionDto.setCurrentUserRatingValue(rating);
        return extensionDto;
    }

    @PostMapping("/auth/create")
    @Transactional
    public ExtensionDto createExtension(@ModelAttribute ExtensionSpec extensionSpec) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        Set<Tag> tags = tagService.saveTags(extensionSpec.getTags());

        Extension extension = extensionService.save(new Extension(extensionSpec, user, tags));
        extension.setGithub(gitHubService.generateGitHub(extensionSpec.getGithub()));
        setFiles(extensionSpec, extension);

        return new ExtensionDto(extensionService.save(extension));
    }

    @PostMapping("/auth/edit")
    @Transactional
    public ExtensionDto editExtension(@ModelAttribute ExtensionSpec extensionSpec) throws IOException, BindException {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        Set<Tag> tags = tagService.saveTags(extensionSpec.getTags());

        Extension extension = new Extension(extensionSpec, user, tags);
        extension.setGithub(gitHubService.generateGitHub(extensionSpec.getGithub()));
        setFiles(extensionSpec, extension);
        if(extensionSpec.getGithub() != null)
            extension.setGithub(gitHubService.updateGithub(extensionSpec.getGithubId(), extensionSpec.getGithub()));

        ExtensionDto extensionDto = new ExtensionDto(extensionService.update(extension, user));
        int rating = ratingService.userRatingForExtension(extension.getId(), loggedUser.getId());
        extensionDto.setCurrentUserRatingValue(rating);

        return extensionDto;
    }


    private void setFiles(ExtensionSpec extensionSpec, Extension extension) {
        long id = extension.getId();
        MultipartFile image = extensionSpec.getImage();
        MultipartFile file = extensionSpec.getFile();
        MultipartFile cover = extensionSpec.getCover();

        if(image != null){
            extension.setImage(fileService.create(image, id + "image", "image"));
        }
        if(file != null){
            extension.setFile(fileService.create(file, String.valueOf(id), ""));
        }
        if(cover != null){
            extension.setCover(fileService.create(cover, id +"cover", "image"));
        }
    }

    @GetMapping("/featured")
    public List<ExtensionDto> featured() {
        return generateExtensionDTOList(extensionService.findFeatured());
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @DeleteMapping("/auth/{id}")
    public void delete(@PathVariable(name = "id") long id) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();

        UserModel userModel = userService.findById(loggedUser.getId(), loggedUser);

        Extension extension = extensionService.delete(id, userModel);
        ratingService.updateRatingOnExtensionDelete(extension);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/auth/unpublished")
    public List<ExtensionDto> getPending() {
        return generateExtensionDTOList(extensionService.findPending());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/{id}/status/{state}")
    public ExtensionDto setPublishedState(@PathVariable(name = "id") long id, @PathVariable("state") String state) {
        return new ExtensionDto(extensionService.setPublishedState(id, state));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/{id}/featured/{state}")
    public ExtensionDto setFeaturedState(@PathVariable("id") long id, @PathVariable("state") String state) {
        return new ExtensionDto(extensionService.setFeaturedState(id, state));
    }

    @GetMapping(value = "/checkName")
    public boolean isNameAvailable(@RequestParam(name = "name") String name){
        return extensionService.checkName(name);
    }

    private List<ExtensionDto> generateExtensionDTOList(List<Extension> extensions) {
        return extensions.stream()
                .map(ExtensionDto::new)
                .collect(Collectors.toList());
    }

    @ExceptionHandler
    ResponseEntity<String> handleFeaturedLimitException(FeaturedLimitException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
