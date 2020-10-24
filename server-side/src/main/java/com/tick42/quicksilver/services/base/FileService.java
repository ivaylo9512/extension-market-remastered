package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    File storeFile(MultipartFile file, int extensionId, UserModel user);

    Resource loadFileAsResource(String fileName);

    File storeImage(MultipartFile receivedFile, int extensionId, UserModel user, String type);

    File storeUserLogo(MultipartFile receivedFile, UserModel user, String type);

    File increaseCount(File file);

    File findByName(String fileName);
}
