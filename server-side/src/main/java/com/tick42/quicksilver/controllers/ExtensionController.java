package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.HomePageDTO;
import com.tick42.quicksilver.models.DTO.PageDTO;
import com.tick42.quicksilver.models.Spec.ExtensionSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.ExtensionService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.tick42.quicksilver.services.base.FileService;
import com.tick42.quicksilver.services.base.RatingService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api")
public class ExtensionController {

    private final ExtensionService extensionService;
    private final FileService fileService;
    private RatingService ratingService;

    @GetMapping("/extensions/getHomeExtensions")
    public HomePageDTO getHomeExtensions(
            @RequestParam(name = "mostRecentCount", required = false) Integer mostRecentCount,
            @RequestParam(name = "mostDownloadedCount") Integer mostDownloadedCount){
        return extensionService.getHomeExtensions(mostRecentCount, mostDownloadedCount);
    }

    @GetMapping("/extensions/filter")
    public PageDTO<ExtensionDTO> findAll(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "orderBy", required = false) String orderBy,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "perPage", required = false) Integer perPage) {

        return extensionService.findAll(name, orderBy, page, perPage);
    }

    @Autowired
    public ExtensionController(ExtensionService extensionService, FileService fileService, RatingService ratingService) {
        this.extensionService = extensionService;
        this.fileService = fileService;
        this.ratingService = ratingService;
    }

    @GetMapping("/extensions/{id}")
    public ExtensionDTO get(@PathVariable(name = "id") int extensionId, HttpServletRequest request) {
        UserDetails loggedUser;
        int rating;
        try {
            loggedUser = Jwt.validate(request.getHeader("Authorization").substring(6));
            rating = ratingService.userRatingForExtension(extensionId, loggedUser.getId());
        } catch (Exception e) {
            if(e.getMessage().equals("Jwt token has expired.")){
                throw new JwtException("Jwt token has expired.");
            }

            loggedUser = null;
            rating = 0;
        }
        ExtensionDTO extensionDTO = extensionService.findById(extensionId, loggedUser);
        extensionDTO.setCurrentUserRatingValue(rating);
        return extensionDTO;
    }


    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PostMapping("/auth/extensions/create")
    public ExtensionDTO createExtension(@Valid @RequestBody ExtensionSpec extension) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        return extensionService.create(extension, userId);
    }

    @GetMapping("/extensions/featured")
    public List<ExtensionDTO> featured() {
        return extensionService.findFeatured();
    }

    @GetMapping("/auth/extensions/download/{id}")
    public ExtensionDTO download(@PathVariable(name = "id") int id) {
        return extensionService.increaseDownloadCount(id);
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @PatchMapping("/auth/extensions/{id}")
    public ExtensionDTO update(@PathVariable int id, @Valid @RequestBody ExtensionSpec extension) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        return extensionService.update(id, extension, userId);
    }

    @PreAuthorize("hasRole('ROLE_USER') OR hasRole('ROLE_ADMIN')")
    @DeleteMapping("/auth/extensions/{id}")
    public void delete(@PathVariable(name = "id") int id) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        ratingService.userRatingOnExtensionDelete(id);
        extensionService.delete(id, userId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = "/auth/extensions/unpublished")
    public List<ExtensionDTO> pending() {
        return extensionService.findPending();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/extensions/{id}/status/{state}")
    public ExtensionDTO setPublishedState(@PathVariable(name = "id") int id, @PathVariable("state") String state) {
        return extensionService.setPublishedState(id, state);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/extensions/{id}/featured/{state}")
    public ExtensionDTO setFeaturedState(@PathVariable("id") int id, @PathVariable("state") String state) {
        return extensionService.setFeaturedState(id, state);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = "/auth/extensions/{id}/github")
    public ExtensionDTO fetchGitHubData(@PathVariable("id") int id) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        int userId = loggedUser.getId();

        return extensionService.fetchGitHub(id, userId);
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
                        .map(x -> x.getDefaultMessage())
                        .toArray());
    }

    @ExceptionHandler
    ResponseEntity handleExtensionNotFoundException(ExtensionNotFoundException e) {
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
                .status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());

    }
}
