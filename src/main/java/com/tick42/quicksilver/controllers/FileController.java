package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.services.base.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "upload/file/{id}")
    @ResponseBody
    public File uploadFile(@RequestParam("file") MultipartFile receivedFile, @PathVariable(name = "id") int extensionId) {
        return fileService.storeFile(receivedFile, extensionId);
    }

    @PostMapping("upload/images/{id}")
    public List<File> uploadImages(@RequestParam("images") MultipartFile[] images, @PathVariable(name = "id") int extensionId) {
        return Arrays.asList(images)
                .stream()
                .map(image -> fileService.storeImage(image, extensionId))
                .collect(Collectors.toList());
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileService.loadFileAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            logger.info("Could not get file type");
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                 resource.getFilename() + "\"")
                .body(resource);
    }
}
