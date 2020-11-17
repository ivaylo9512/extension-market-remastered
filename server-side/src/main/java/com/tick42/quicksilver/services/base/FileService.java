package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    Resource getAsResource(String fileName);

    File create(MultipartFile receivedFile, String name);

    File increaseCount(File file);

    File findByName(String fileName);
}
