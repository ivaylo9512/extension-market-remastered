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
    public File storeFile(MultipartFile receivedFile, long extensionId, UserModel user) {

        File file = generateFile(receivedFile, "file", extensionId);

        try {
            saveFile(file, receivedFile);
            return file;
        } catch (IOException e) {
            throw new FileStorageException("Couldn't store file");
        }
    }


    @Override
    public File storeImage(MultipartFile receivedFile, long extensionId, String type) {

        File image = generateFile(receivedFile, type, extensionId);

        try {
            if (!image.getType().startsWith("image/")) {
                throw new FileFormatException("File should be of type IMAGE.");
            }

            saveFile(image, receivedFile);

            return image;

        } catch (IOException e) {
            throw new FileStorageException("Couldn't store image.");
        }
    }
    @Override
    public File storeUserLogo(MultipartFile receivedFile, UserModel user, String type) {


        File image = generateFile(receivedFile, "logo", user.getId());

        try {
            if (!image.getType().startsWith("image/")) {
                throw new FileFormatException("File should be of type IMAGE.");
            }

            saveFile(image, receivedFile);

            return image;

        } catch (IOException e) {
            throw new FileStorageException("Couldn't store image.");
        }
    }

    private void saveFile(File image, MultipartFile receivedFile) throws IOException {
        Path targetLocation = this.fileLocation.resolve(image.getName());
        Files.copy(receivedFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource getAsResource(String fileName){
        try {
            Path filePath = this.fileLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundUncheckedException("File not found");
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundUncheckedException("File not found " + e);
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

    private File generateFile(MultipartFile receivedFile, String type, long extensionId) {
        String fileType = FilenameUtils.getExtension(receivedFile.getOriginalFilename());
        String fileName = extensionId + "_" + type + "." + fileType;
        return new File(fileName, receivedFile.getSize(), receivedFile.getContentType());
    }
}
