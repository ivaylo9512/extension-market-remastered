package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.PageDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ExtensionService {

    Extension findById(long id, UserDetails loggedUser);

    Extension update(Extension extension, UserModel loggedUser);

    Extension save(Extension extension);

    Extension delete(long id, UserModel loggedUser);

    List<Extension> findMostRecent(Integer mostRecentCount);

    List<Extension> findFeatured();

    List<Extension> findMostDownloaded(Integer mostDownloadedCount);

    PageDto<Extension> findPageWithCriteria(String name, String orderBy, Integer page, Integer perPage);

    long findTotalResults(String name);

    Extension setPublishedState(long id, String newState);

    Extension setFeaturedState(long id, String newState);

    Page<Extension> findByPending(boolean state, int pageSize, long lastId);

    void loadFeatured();

    void updateMostRecent();

    Extension reloadExtension(Extension extension);

    void reloadFile(File file);

    boolean isNameAvailable(String name);

    Page<Extension> findUserExtensions(int pageSize, long lastId, UserModel user);

    Page<Extension> findByTag(String name, int pageSize, long lastId);
}
