package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.FileStorageException;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.FileService;
import com.tick42.quicksilver.services.base.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping(value = "/api/files")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;
    private final ExtensionService extensionService;
    private final UserService userService;

    @Autowired
    public FileController(FileService fileService, ExtensionService extensionService, UserService userService) {
        this.fileService = fileService;
        this.extensionService = extensionService;
        this.userService = userService;
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> getAsResource(@PathVariable("fileName") String fileName, HttpServletRequest request) throws FileNotFoundException {
        Resource resource = fileService.getAsResource(fileName);
        String contentType;

        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        if(fileName.contains("file")) {
            String[] result = Arrays.stream(fileName.split("[file|.]")).filter(s -> !s.equals("")).toArray(String[]::new);

            File file = fileService.findByName("file", Long.parseLong(result[0]));
            fileService.increaseCount(file);
            extensionService.reloadFile(file);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/auth/delete/{resourceType}/{ownerId}")
    public boolean delete(@PathVariable("resourceType") String resourceType, @PathVariable("ownerId") long ownerId){
        UserDetails loggedUser = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

        return fileService.delete(resourceType, ownerId, userService.findById(loggedUser.getId(), loggedUser));
    }
}
