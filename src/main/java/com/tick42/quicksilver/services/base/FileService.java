package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;

public interface FileService {
    void delete(File file, long sourceId, UserModel loggedUser);

    void deleteById(long id, UserModel loggedUser);

    Resource getAsResource(String fileName) throws MalformedURLException;

    File findByOwner(String resourceType, UserModel owner);

    File findByExtension(String resourceType, Extension extension);

    void save(String name, MultipartFile receivedFile) throws IOException;

    File generate(MultipartFile receivedFile, String resourceType, String fileType);

    void deleteFromSystem(String name);

    File increaseCount(File file);
}
