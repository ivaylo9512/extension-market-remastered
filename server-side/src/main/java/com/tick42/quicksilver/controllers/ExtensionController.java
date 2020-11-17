package com.tick42.quicksilver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
`import com.tick42.quicksilver.models.DTOs.ExtensionDTO;
import com.tick42.quicksilver.models.DTOs.HomePageDTO;
import com.tick42.quicksilver.models.DTOs.PageDTO;
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
    public HomePageDTO getHomeExtensions(
            @RequestParam(name = "mostRecentCount", required = false) Integer mostRecentCount,
            @RequestParam(name = "mostDownloadedCount") Integer mostDownloadedCount){

        List<ExtensionDTO> mostRecent = generateExtensionDTOList(extensionService.findMostRecent(mostRecentCount));
        List<ExtensionDTO> featured = generateExtensionDTOList(extensionService.getFeatured());
        List<ExtensionDTO> mostDownloaded = generateExtensionDTOList(extensionService.findMostDownloaded(mostDownloadedCount));
        return new HomePageDTO(mostRecent, featured, mostDownloaded);
    }

    @GetMapping("/filter")
    public PageDTO<ExtensionDTO> findPageWithCriteria(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "orderBy", required = false) String orderBy,
            @RequestParam(name = "page", required = false) Integer requestedPage,
            @RequestParam(name = "perPage", required = false) Integer perPage) {

        PageDTO<Extension> page = extensionService.findPageWithCriteria(name, orderBy, requestedPage, perPage);
        PageDTO<ExtensionDTO> pageDTO = new PageDTO<>(page);
        pageDTO.setExtensions(generateExtensionDTOList(page.getExtensions()));
        return pageDTO;
    }

    @GetMapping("/{id}")
    public ExtensionDTO findById(@PathVariable(name = "id") int extensionId, HttpServletRequest request) {
        UserDetails loggedUser;
        int rating;
        try {
            loggedUser = Jwt.validate(request.getHeader("Authorization").substring(6));
            rating = ratingService.userRatingForExtension(extensionId, loggedUser.getId());
        } catch (Exception e) {
            if(e.getMessage() != null && e.getMessage().equals("Jwt token has expired.")){
                throw new JwtException("Jwt token has expired.");
            }
            loggedUser = null;
            rating = 0;
        }
        ExtensionDTO extensionDTO = generateExtensionDTO(extensionService.findById(extensionId, loggedUser));
        extensionDTO.setCurrentUserRatingValue(rating);
        return extensionDTO;
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PostMapping("/auth/create")
    @Transactional
    public ExtensionDTO createExtension(
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
    public ExtensionDTO editExtension(
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

        ExtensionDTO extensionDTO = generateExtensionDTO(extensionService.save(extension));
        int rating = ratingService.userRatingForExtension(extension.getId(), loggedUser.getId());
        extensionDTO.setCurrentUserRatingValue(rating);

        return extensionDTO;
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
    public List<ExtensionDTO> featured() {
        return generateExtensionDTOList(extensionService.getFeatured());
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @DeleteMapping("/auth/{id}")
    public void delete(@PathVariable(name = "id") int id) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        UserModel user = userService.findById(loggedUser.getId(), null);

        ratingService.updateRatingOnExtensionDelete(id);
        extensionService.delete(id, user);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/auth/unpublished")
    public List<ExtensionDTO> getPending() {
        return generateExtensionDTOList(extensionService.findPending());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/{id}/status/{state}")
    public ExtensionDTO setPublishedState(@PathVariable(name = "id") int id, @PathVariable("state") String state) {
        return generateExtensionDTO(extensionService.setPublishedState(id, state));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/{id}/featured/{state}")
    public ExtensionDTO setFeaturedState(@PathVariable("id") int id, @PathVariable("state") String state) {
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
    private List<ExtensionDTO> generateExtensionDTOList(List<Extension> extensions) {
        return extensions.stream()
                .map(this::generateExtensionDTO)
                .collect(Collectors.toList());
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

        String base = "http://localhost:8090/api/download/";
        if (extension.getImage() != null)
            extensionDTO.setImageLocation(base + extension.getImage().getName());

        if (extension.getFile() != null)
            extensionDTO.setFileLocation(base + extension.getFile().getName());

        if (extension.getCover() != null)
            extensionDTO.setCoverLocation(base + extension.getCover().getName());

        return extensionDTO;
    }


}
