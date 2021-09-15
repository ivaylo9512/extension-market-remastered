package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.FileRepository;
import com.tick42.quicksilver.services.base.FileService;
import org.apache.commons.io.FilenameUtils;
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

    public FileServiceImpl(FileRepository fileRepository) throws IOException {
        this.fileRepository = fileRepository;
        this.fileLocation = Paths.get("./uploads")
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
    public boolean delete(String resourceType, UserModel owner, UserModel loggedUser) {
        if(owner.getId() != loggedUser.getId()
                && !loggedUser.getRole().equals("ROLE_ADMIN")){
            throw new UnauthorizedException("Unauthorized");
        }

        File file = findByType(resourceType, owner);

        boolean isDeleted = new java.io.File("./uploads/" + resourceType + owner.getId() + "." + file.getExtensionType()).delete();
        if(isDeleted){
            fileRepository.delete(file);
            return true;
        }

        return false;
    }

    @Override
    public File increaseCount(File file){
        file.setDownloadCount(file.getDownloadCount() + 1);
        return fileRepository.save(file);
    }

    @Override
    public File findByType(String resourceType, UserModel owner){
        return fileRepository.findByType(resourceType, owner).orElseThrow(() ->
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
