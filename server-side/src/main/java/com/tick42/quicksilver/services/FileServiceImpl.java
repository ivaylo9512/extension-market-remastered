package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.File;
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

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
        this.fileLocation = Paths.get("./uploads")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileLocation);
        } catch (Exception e) {
            throw new FileStorageException("Couldn't create directory");
        }
    }

    @Override
    public File update(MultipartFile receivedFile, String name, long id, String type){
        File file = generate(receivedFile, name, type);
        file.setId(id);

        try {
            save(file, receivedFile);
        } catch (IOException e) {
            throw new FileStorageException("Couldn't store the image.");
        }

        return fileRepository.save(file);
    }

    @Override
    public File create(MultipartFile receivedFile, String name, String type) {
        File file = generate(receivedFile, name, type);

        try {
            save(file, receivedFile);
        } catch (IOException e) {
            throw new FileStorageException("Couldn't store the image.");
        }

        return fileRepository.save(file);
    }

    void save(File image, MultipartFile receivedFile) throws IOException {
        Path targetLocation = this.fileLocation.resolve(image.getName());
        Files.copy(receivedFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource getAsResource(String fileName){
        try {
            Path filePath = this.fileLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new EntityNotFoundException("File not found");
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new FileFormatException(e.getMessage());
        }
    }

    @Override
    public File increaseCount(File file){
        file.setDownloadCount(file.getDownloadCount() + 1);
        return fileRepository.save(file);
    }

    @Override
    public File findByName(String fileName){
        return fileRepository.findByName(fileName);
    }

    File generate(MultipartFile receivedFile, String name, String type) {
        String fileType = FilenameUtils.getExtension(receivedFile.getOriginalFilename());
        String fileName = name + "." + fileType;
        File file = new File(fileName, receivedFile.getSize(), receivedFile.getContentType());

        if (!file.getType().startsWith(type)) {
            throw new FileFormatException("File should be of type" + type);
        }

        return file;
    }
}
