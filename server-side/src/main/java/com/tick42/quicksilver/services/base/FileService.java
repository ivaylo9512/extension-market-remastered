package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    File storeFile(MultipartFile file, long extensionId, UserModel user);

    Resource getAsResource(String fileName);

    File storeImage(MultipartFile receivedFile, long extensionId, String type);

    File storeUserLogo(MultipartFile receivedFile, UserModel user, String type);

    File increaseCount(File file);

    File findByName(String fileName);
}
