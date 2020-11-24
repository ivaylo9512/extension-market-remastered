package com.tick42.quicksilver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.HomePageDto;
import com.tick42.quicksilver.models.Dtos.PageDto;
import com.tick42.quicksilver.models.specs.ExtensionSpec;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.*;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import com.tick42.quicksilver.validators.ExtensionValidator;
import io.jsonwebtoken.JwtException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    private RatingService ratingService;
    private UserService userService;
    private TagService tagService;
    private GitHubService gitHubService;

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

        ExtensionDto extensionDto = generateExtensionDTO(extensionService.findById(extensionId, loggedUser));
        extensionDto.setCurrentUserRatingValue(rating);
        return extensionDto;
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PostMapping("/auth/create")
    @Transactional
    public ExtensionDto createExtension(
            @RequestParam(name = "image", required = false) MultipartFile extensionImage ,
            @RequestParam(name = "file", required = false) MultipartFile extensionFile,
            @RequestParam(name = "cover", required = false) MultipartFile extensionCover,
            @RequestParam(name = "extension") String extensionJson) throws IOException, BindException {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, null);
        ExtensionSpec extensionSpec = validateExtension(extensionJson);
        Set<Tag> tags = tagService.generateTags(extensionSpec.getTags());

        Extension extension = new Extension(extensionSpec, user, tags);

        if(extensionSpec.getGithub() != null)
            extension.setGithub(gitHubService.generateGitHub(extensionSpec.getGithub()));


        extensionService.save(extension);

        setFiles(extensionImage, extensionFile, extensionCover, extension);

        return generateExtensionDTO(extensionService.save(extension));
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PostMapping("/auth/edit")
    @Transactional
    public ExtensionDto editExtension(
            @RequestParam(name = "image", required = false) MultipartFile extensionImage ,
            @RequestParam(name = "file", required = false) MultipartFile extensionFile,
            @RequestParam(name = "cover", required = false) MultipartFile extensionCover,
            @RequestParam(name = "extension") String extensionJson) throws IOException, BindException {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        UserModel user = userService.findById(userId, null);
        ExtensionSpec extensionSpec = validateExtension(extensionJson);
        Set<Tag> tags = tagService.generateTags(extensionSpec.getTags());

        Extension extension = extensionService.update(new Extension(extensionSpec, user, tags));

        if(extensionSpec.getGithub() != null)
            extension.setGithub(gitHubService.updateGithub(extensionSpec.getGithubId(), extensionSpec.getGithub()));


        setFiles(extensionImage, extensionFile, extensionCover, extension);

        ExtensionDto extensionDto = generateExtensionDTO(extensionService.save(extension));
        int rating = ratingService.userRatingForExtension(extension.getId(), loggedUser.getId());
        extensionDto.setCurrentUserRatingValue(rating);

        return extensionDto;
    }

    private ExtensionSpec validateExtension(String extensionJson) throws BindException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        ExtensionSpec extensionSpec = mapper.readValue(extensionJson, ExtensionSpec.class);

        Validator validator = new ExtensionValidator();
        BindingResult bindingResult = new DataBinder(extensionSpec).getBindingResult();
        validator.validate(extensionSpec, bindingResult);

        if(bindingResult.hasErrors())
            throw new BindException(bindingResult);

        return extensionSpec;
    }

    private void setFiles(MultipartFile extensionImage, MultipartFile extensionFile, MultipartFile extensionCover, Extension extension) {
        long extensionId = extension.getId();

        if(extensionImage != null){
            File image = fileService.create(extensionImage, extensionId + "image");
            extension.setImage(image);
        }
        if(extensionFile != null){
            File file = fileService.create(extensionFile, String.valueOf(extensionId));
            extension.setFile(file);
        }
        if(extensionCover != null){
            File cover = fileService.create(extensionCover, extensionId +"cover");
            extension.setCover(cover);
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

        Extension extension = extensionService.delete(id, loggedUser);
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
        return generateExtensionDTO(extensionService.setPublishedState(id, state));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/{id}/featured/{state}")
    public ExtensionDto setFeaturedState(@PathVariable("id") long id, @PathVariable("state") String state) {
        return generateExtensionDTO(extensionService.setFeaturedState(id, state));
    }

    @GetMapping(value = "/checkName")
    public boolean isNameAvailable(@RequestParam(name = "name") String name){
        return extensionService.checkName(name);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity handleInvalidExtensionSpecException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .toArray());
    }

    @ExceptionHandler
    ResponseEntity handleBadCredentialsException(BadCredentialsException e){
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleExtensionNotFoundException(EntityNotFoundException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleExtensionUnavailable(ExtensionUnavailableException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleInvalidStateException(InvalidStateException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleInvalidParameterException(InvalidParameterException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleGitHubRepositoryException(GitHubRepositoryException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleUnauthorizedExtensionModificationException(UnauthorizedExtensionModificationException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleJwtException(JwtException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleBindException(BindException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getBindingResult().getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getCode)
                        .toArray());
    }
    @ExceptionHandler
    ResponseEntity handleFileFormatException(FileFormatException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleFeaturedLimitException(FeaturedLimitException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
    private List<ExtensionDto> generateExtensionDTOList(List<Extension> extensions) {
        return extensions.stream()
                .map(this::generateExtensionDTO)
                .collect(Collectors.toList());
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
