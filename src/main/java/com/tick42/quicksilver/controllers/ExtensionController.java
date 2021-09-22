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
import javax.validation.Valid;
import org.springframework.data.domain.Page;
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
        pageDto.setData(generateExtensionDTOList(page.getData()));
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
    public ExtensionDto createExtension(@Valid @ModelAttribute ExtensionSpec extensionSpec) throws IOException {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        Set<Tag> tags = tagService.saveTags(extensionSpec.getTags());

        Extension newExtension = new Extension(extensionSpec, user, tags);
        generateFiles(extensionSpec, newExtension, user);

        Extension extension = extensionService.save(newExtension);

        saveFiles(extensionSpec, extension);
        extension.setGithub(gitHubService.generateGitHub(extensionSpec.getGithub()));

        return new ExtensionDto(extensionService.save(extension));
    }

    @PostMapping("/auth/edit")
    @Transactional
    public ExtensionDto editExtension(@Valid @ModelAttribute ExtensionSpec extensionSpec) throws IOException, BindException {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        Set<Tag> tags = tagService.saveTags(extensionSpec.getTags());

        Extension extension = new Extension(extensionSpec, user, tags);
        extension.setGithub(gitHubService.generateGitHub(extensionSpec.getGithub()));

        generateFiles(extensionSpec, extension, user);
        saveFiles(extensionSpec, extension);

        if(extensionSpec.getGithub() != null)
            extension.setGithub(gitHubService.updateGitHub(extensionSpec.getGithubId(), extensionSpec.getGithub()));

        ExtensionDto extensionDto = new ExtensionDto(extensionService.update(extension, user));
        int rating = ratingService.userRatingForExtension(extension.getId(), loggedUser.getId());
        extensionDto.setCurrentUserRatingValue(rating);

        return extensionDto;
    }

    private void generateFiles(ExtensionSpec extensionSpec, Extension extension, UserModel owner) {
        MultipartFile image = extensionSpec.getImage();
        MultipartFile file = extensionSpec.getFile();
        MultipartFile cover = extensionSpec.getCover();

        if(image != null){
            File imageModel = fileService.generate(image, "image", "image");

            imageModel.setOwner(owner);
            imageModel.setExtension(extension);
            extension.setImage(imageModel);
        }
        if(file != null){
            File fileModel = fileService.generate(file, "file", "");

            fileModel.setOwner(owner);
            fileModel.setExtension(extension);
            extension.setFile(fileModel);
        }
        if(cover != null){
            File coverModel = fileService.generate(cover, "cover", "image");

            coverModel.setOwner(owner);
            coverModel.setExtension(extension);
            extension.setCover(coverModel);
        }
    }

    private void saveFiles(ExtensionSpec extensionSpec, Extension extension) throws IOException {
        long id = extension.getId();
        MultipartFile image = extensionSpec.getImage();
        MultipartFile file = extensionSpec.getFile();
        MultipartFile cover = extensionSpec.getCover();

        if(image != null){
            fileService.save("image" + id, image);
        }
        if(file != null){
            fileService.save("file" + id, file);
            extension.setCover(fileService.generate(cover, "file", ""));
        }
        if(cover != null){
            fileService.save("cover" + id, cover);
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

    @GetMapping(value = { "/auth/findUserExtensions/{pageSize}/{lastId}", "/auth/findUserExtensions/{pageSize}" })
    public PageDto<ExtensionDto> findUserExtensions(@PathVariable("pageSize") int pageSize,
                                                    @PathVariable(value = "lastId", required = false) Long lastId){
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();

        Page<Extension> page = extensionService.findUserExtensions(pageSize, lastId == null
                ? 0 : lastId, userService.getById(loggedUser.getId()));

        return new PageDto<>(page.getTotalElements(), generateExtensionDTOList(page.getContent()));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = { "/auth/findPending/{state}/{pageSize}/", "/auth/findPending/{state}/{pageSize}/{lastId}" })
    public PageDto<ExtensionDto> findByPending(@PathVariable("state") boolean state,
                                         @PathVariable("pageSize") int pageSize,
                                         @PathVariable(name = "lastId", required = false) Long lastId) {
        Page<Extension> page = extensionService.findByPending(state, pageSize, lastId == null ? 0 : lastId);

        return new PageDto<>(page.getTotalElements(), generateExtensionDTOList(page.getContent()));
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

    @GetMapping(value = { "/findByTag/{name}/{pageSize}/{lastId}", "/findByTag/{name}/{pageSize}/" })
    public PageDto<ExtensionDto> findByName(@PathVariable(name = "name") String name,
                                            @PathVariable(name = "pageSize") int pageSize,
                                            @PathVariable(name = "lastId", required = false) Long lastId) {
        Page<Extension> page = extensionService.findByTag(name, pageSize, lastId == null ? 0 : lastId);

        return new PageDto<>(page.getTotalElements(), generateExtensionDTOList(page.getContent()));
    }

    @GetMapping(value = "/checkName")
    public boolean isNameAvailable(@RequestParam(name = "name") String name){
        return extensionService.isNameAvailable(name);
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
