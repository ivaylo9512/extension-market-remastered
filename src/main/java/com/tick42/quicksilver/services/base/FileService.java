package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Resource getAsResource(String fileName);

    File update(MultipartFile receivedFile, String name, long id, String type);

    File create(MultipartFile receivedFile, String name, String type);

    boolean delete(String fileName, UserModel loggedUser);

    File increaseCount(File file);

    File findByName(String fileName);
}
