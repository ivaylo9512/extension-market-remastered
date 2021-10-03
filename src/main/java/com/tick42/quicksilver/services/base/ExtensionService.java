package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.PageDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface ExtensionService {

    Extension findById(long id, UserDetails loggedUser);

    Extension update(Extension extension, UserModel loggedUser);

    Extension save(Extension extension);

    Extension delete(long id, UserModel loggedUser);

    List<Extension> findMostRecent(Integer mostRecentCount);

    Extension getById(long id);

    List<Extension> findFeatured();

    Page<Extension> findAllByDownloaded(int lastDownloadCount, int pageSize, String name, long lastId);

    Page<Extension> findAllByCommitDate(LocalDateTime lastDate, int pageSize, String name, long lastId);

    Page<Extension> findAllByUploadDate(LocalDateTime lastDate, int pageSize, String name, long lastId);

    Page<Extension> findAllByName(String lastName, int pageSize, String name);

    Extension setPending(long id, boolean state);

    Extension setFeatured(long id, boolean state);

    Page<Extension> findByPending(boolean state, int pageSize, long lastId);

    void loadFeatured();

    void updateMostRecent();

    Extension reloadExtension(Extension extension);

    void reloadFile(File file);

    boolean isNameAvailable(String name);

    Page<Extension> findUserExtensions(int pageSize, long lastId, UserModel user);

    Page<Extension> findByTag(String name, int pageSize, long lastId);
}
