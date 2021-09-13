package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import org.h2.engine.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public interface FileService {
    boolean delete(String resourceType, UserModel owner, UserModel loggedUser);

    Resource getAsResource(String fileName) throws MalformedURLException;

    File findByName(String resourceType, UserModel owner);

    void save(String name, MultipartFile receivedFile) throws IOException;

    File generate(MultipartFile receivedFile, String resourceType, String fileType);

    File increaseCount(File file);
}
