package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.PageDto;
import java.util.List;

public interface ExtensionService {

    Extension findById(long id, UserDetails loggedUser);

    Extension update(Extension extension);

    Extension save(Extension extension);

    Extension delete(long id, UserDetails loggedUser);

    List<Extension> findMostRecent(Integer mostRecentCount);

    List<Extension> getFeatured();

    List<Extension> findMostDownloaded(Integer mostDownloadedCount);

    PageDto<Extension> findPageWithCriteria(String name, String orderBy, Integer page, Integer perPage);

    long findTotalResults(String name);

    Extension setPublishedState(long id, String newState);

    Extension setFeaturedState(long id, String newState);

    List<Extension> findPending();

    void loadFeatured();

    void updateMostRecent();

    Extension reloadExtension(Extension extension);

    void reloadFile(File file);

    boolean checkName(String name);
}
