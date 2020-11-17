package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.DTOs.PageDTO;
import java.util.List;

public interface ExtensionService {

    Extension findById(long id, UserDetails loggedUser);

    Extension update(Extension extension);

    Extension save(Extension extension);

    void delete(int id, UserDetails loggedUser);

    List<Extension> findMostRecent(Integer mostRecentCount);

    List<Extension> getFeatured();

    List<Extension> findMostDownloaded(Integer mostDownloadedCount);

    PageDTO<Extension> findPageWithCriteria(String name, String orderBy, Integer page, Integer perPage);

    long findTotalResults(String name);

    Extension setPublishedState(int id, String newState);

    Extension setFeaturedState(int id, String newState);

    List<Extension> findPending();

    void loadFeatured();

    void updateMostRecent();

    Extension reloadExtension(Extension extension);

    void reloadFile(File file);

    boolean checkName(String name);
}
