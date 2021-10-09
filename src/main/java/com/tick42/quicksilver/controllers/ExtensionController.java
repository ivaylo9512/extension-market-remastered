package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.HomePageDto;
import com.tick42.quicksilver.models.Dtos.PageDto;
import com.tick42.quicksilver.models.specs.ExtensionCreateSpec;
import com.tick42.quicksilver.models.specs.ExtensionSpec;
import com.tick42.quicksilver.models.specs.ExtensionUpdateSpec;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.*;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
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

    @GetMapping("/findHomeExtensions/{mostRecentCount}/{mostDownloadedCount}")
    public HomePageDto findHomeExtensions(@PathVariable("mostRecentCount") int mostRecentCount,
                                          @PathVariable("mostDownloadedCount") int mostDownloadedCount){

        List<ExtensionDto> mostRecent = generateExtensionDTOList(extensionService.findMostRecent(mostRecentCount));
        List<ExtensionDto> featured = generateExtensionDTOList(extensionService.findFeatured());
        List<ExtensionDto> mostDownloaded = generateExtensionDTOList(extensionService.findAllByDownloaded(Integer.MAX_VALUE, mostDownloadedCount, "", 0).getContent());

        return new HomePageDto(mostRecent, featured, mostDownloaded);
    }

    @GetMapping("/findAllByCommitDate")
    public PageDto<ExtensionDto> findAllByCommitDate(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "lastDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
            @RequestParam(name = "lastId", required = false, defaultValue = "0") int lastId,
            @RequestParam(name = "pageSize") int pageSize
            ){

        Page<Extension> page = extensionService.findAllByCommitDate(lastDate == null ? LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59) : lastDate, pageSize, name, lastId);

        return new PageDto<>(generateExtensionDTOList(page.getContent()), page.getTotalPages(), page.getTotalElements());
    }

    @GetMapping("/findAllByUploadDate")
    public PageDto<ExtensionDto> findAllByUploadDate(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "lastDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastDate,
            @RequestParam(name = "lastId", required = false, defaultValue = "0") int lastId,
            @RequestParam(name = "pageSize") int pageSize
    ){

        Page<Extension> page = extensionService.findAllByUploadDate(lastDate == null ? LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59) : lastDate, pageSize, name, lastId);

        return new PageDto<>(generateExtensionDTOList(page.getContent()), page.getTotalPages(), page.getTotalElements());
    }

    @GetMapping("/findAllByName")
    public PageDto<ExtensionDto> findAllByName(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "lastName", required = false, defaultValue = "") String lastName,
            @RequestParam(name = "pageSize") int pageSize
    ){
        Page<Extension> page = extensionService.findAllByName(lastName, pageSize, name);

        return new PageDto<>(generateExtensionDTOList(page.getContent()), page.getTotalPages(), page.getTotalElements());
    }

    @GetMapping("/findAllByDownloadCount")
    public PageDto<ExtensionDto> findAllByDownloadCount(
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "lastDownloadCount", required = false, defaultValue = Integer.MAX_VALUE + "") int lastDownloadCount,
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "lastId", required = false, defaultValue = "0") int lastId
    ){
        Page<Extension> page = extensionService.findAllByDownloaded(lastDownloadCount, pageSize, name, lastId);

        return new PageDto<>(generateExtensionDTOList(page.getContent()), page.getTotalPages(), page.getTotalElements());
    }

    @GetMapping("/findById/{id}")
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
    public ExtensionDto createExtension(@Valid @ModelAttribute ExtensionCreateSpec extensionCreateSpec) throws IOException {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        Set<Tag> tags = tagService.saveTags(extensionCreateSpec.getTags());
        Extension newExtension = new Extension(extensionCreateSpec, user, tags);
        generateFiles(extensionCreateSpec, newExtension, user);
        newExtension.setGithub(gitHubService.generateGitHub(extensionCreateSpec.getGithub()));

        Extension extension = extensionService.save(newExtension);
        saveFiles(extensionCreateSpec, extension);

        return new ExtensionDto(extension);
    }

    @PostMapping("/auth/edit")
    @Transactional
    public ExtensionDto editExtension(@Valid @ModelAttribute ExtensionUpdateSpec extensionUpdateSpec) throws IOException, BindException {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, loggedUser);
        Set<Tag> tags = tagService.saveTags(extensionUpdateSpec.getTags());

        Extension extension = new Extension(extensionUpdateSpec, user, tags);

        generateFiles(extensionUpdateSpec, extension, user);
        saveFiles(extensionUpdateSpec, extension);

        if(extensionUpdateSpec.getGithub() != null)
            extension.setGithub(gitHubService.updateGitHub(extensionUpdateSpec.getGithubId(), extensionUpdateSpec.getGithub()));

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
        }
        if(cover != null){
            fileService.save("cover" + id, cover);
        }
    }

    @GetMapping("/featured")
    public List<ExtensionDto> findFeatured() {
        return generateExtensionDTOList(extensionService.findFeatured());
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @DeleteMapping("/auth/delete/{id}")
    @Transactional
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

        return new PageDto<>(generateExtensionDTOList(page.getContent()), page.getTotalPages(), page.getTotalElements());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = { "/auth/findPending/{state}/{pageSize}/", "/auth/findPending/{state}/{pageSize}/{lastId}" })
    public PageDto<ExtensionDto> findByPending(@PathVariable("state") boolean state,
                                         @PathVariable("pageSize") int pageSize,
                                         @PathVariable(name = "lastId", required = false) Long lastId) {
        Page<Extension> page = extensionService.findByPending(state, pageSize, lastId == null ? 0 : lastId);

        return new PageDto<>(generateExtensionDTOList(page.getContent()), page.getTotalPages(), page.getTotalElements());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/setPending/{id}/{state}")
    @Transactional
    public ExtensionDto setPending(@PathVariable(name = "id") long id, @PathVariable("state") boolean state) {
        return new ExtensionDto(extensionService.setPending(id, state));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/setFeatured/{id}/{state}")
    public ExtensionDto setFeatured(@PathVariable("id") long id, @PathVariable("state") boolean state) {
        return new ExtensionDto(extensionService.setFeatured(id, state));
    }

    @GetMapping(value = { "/findByTag/{name}/{pageSize}/{lastId}", "/findByTag/{name}/{pageSize}/" })
    public PageDto<ExtensionDto> findByTag(@PathVariable(name = "name") String name,
                                            @PathVariable(name = "pageSize") int pageSize,
                                            @PathVariable(name = "lastId", required = false) Long lastId) {
        Page<Extension> page = extensionService.findByTag(name, pageSize, lastId == null ? 0 : lastId);

        return new PageDto<>(generateExtensionDTOList(page.getContent()), page.getTotalPages(), page.getTotalElements());
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
