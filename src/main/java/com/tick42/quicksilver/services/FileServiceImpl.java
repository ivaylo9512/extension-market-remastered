package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.FileRepository;
import com.tick42.quicksilver.services.base.FileService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService {
    private final Path fileLocation;
    private final FileRepository fileRepository;
    private final String uploadPath;

    public FileServiceImpl(FileRepository fileRepository, @Value("${uploadPath}") String uploadPath) throws IOException {
        this.fileRepository = fileRepository;
        this.uploadPath = uploadPath;
        this.fileLocation = Paths.get(uploadPath)
                .toAbsolutePath().normalize();
        Files.createDirectories(this.fileLocation);
    }

    @Override
    public void save(String name, MultipartFile receivedFile) throws IOException {
        String extension = FilenameUtils.getExtension(receivedFile.getOriginalFilename());
        String fileName = name + "." + extension;

        Path targetLocation = this.fileLocation.resolve(fileName);
        Files.copy(receivedFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource getAsResource(String fileName) throws MalformedURLException{
        Path filePath = this.fileLocation.resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new EntityNotFoundException("File not found");
        }

        return resource;
    }

    @Override
    public void deleteById(long id, UserModel loggedUser){
        File file = fileRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("File not found."));

        Extension extension = file.getExtension();
        delete(file, extension != null ? extension.getId() : file.getOwner().getId(), loggedUser);
    }

    @Override
    public void delete(File file, long sourceId, UserModel loggedUser) {
        if(file.getOwner().getId() != loggedUser.getId()
                && !loggedUser.getRole().equals("ROLE_ADMIN")){
            throw new UnauthorizedException("Unauthorized.");
        }

        new java.io.File(uploadPath + "/" + file.getResourceType() + sourceId + "." + file.getExtensionType()).delete();
        fileRepository.delete(file);
    }

    @Override
    public void deleteFromSystem(String name){
        new java.io.File(uploadPath + "/" + name).delete();
    }

    @Override
    public File increaseCount(File file){
        file.setDownloadCount(file.getDownloadCount() + 1);
        return fileRepository.save(file);
    }

    public File findByOwner(String resourceType, UserModel owner) {
        return fileRepository.findByOwner(resourceType, owner).orElseThrow(() ->
                new EntityNotFoundException("File not found."));
    }

    @Override
    public File findByExtension(String resourceType, Extension extension){
        return fileRepository.findByExtension(resourceType, extension).orElseThrow(() ->
                new EntityNotFoundException("File not found."));
    }

    @Override
    public File generate(MultipartFile receivedFile, String resourceType, String fileType) {
        String extension = FilenameUtils.getExtension(receivedFile.getOriginalFilename());
        String contentType = receivedFile.getContentType();

        if (contentType == null || !contentType.startsWith(fileType)) {
            throw new FileFormatException("File should be of type " + fileType);
        }

        return new File(resourceType, receivedFile.getSize(), receivedFile.getContentType(), extension);
    }
}
