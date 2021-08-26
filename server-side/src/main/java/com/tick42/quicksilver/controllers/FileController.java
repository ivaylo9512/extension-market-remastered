package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.FileNotFoundUncheckedException;
import com.tick42.quicksilver.exceptions.FileStorageException;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping(value = "/api")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileService fileService;
    private final ExtensionService extensionService;

    @Autowired
    public FileController(FileService fileService, ExtensionService extensionService) {
        this.fileService = fileService;
        this.extensionService = extensionService;
    }


    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> getAsResource(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileService.getAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            logger.info("Could not get file type");
        }

        if (contentType == null) {
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


    @ExceptionHandler
    ResponseEntity<String> handleFileStorageException(FileStorageException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity<String> handleFileNotFoundUncheckedException(FileNotFoundUncheckedException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

}
