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
import java.io.IOException;

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
    public ResponseEntity<Resource> getAsResource(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileService.getAsResource(fileName);
        String contentType;

        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        if(fileName.contains("file")) {
            File file = fileService.findByName(fileName);
            fileService.increaseCount(file);
            extensionService.reloadFile(file);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/auth/delete/{name}")
    public boolean delete(@PathVariable("name") String name){
        UserDetails loggedUser = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();

      return fileService.delete(name, userService.findById(loggedUser.getId(), loggedUser));
    }
}
