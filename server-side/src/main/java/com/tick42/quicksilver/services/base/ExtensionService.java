package com.tick42.quicksilver.services.base;


import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.HomePageDTO;
import com.tick42.quicksilver.models.DTO.PageDTO;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Spec.ExtensionSpec;
import com.tick42.quicksilver.models.UserDetails;

import java.util.List;

public interface ExtensionService {

    Extension create(ExtensionSpec model, int id);

    Extension findById(int id, UserDetails loggedUser);

    Extension update(int extensionId, ExtensionSpec extension, int userId);

    Extension save(Extension extension);

    void delete(int id, int userId);

    List<Extension> findMostRecent(Integer mostRecentCount);

    List<Extension> getFeatured();

    List<Extension> findMostDownloaded(Integer mostDownloadedCount);

    PageDTO<Extension> findPageWithCriteria(String name, String orderBy, Integer page, Integer perPage);

    long findTotalResults(String name);

    Extension setPublishedState(int id, String newState);

    Extension setFeaturedState(int id, String newState);

    List<Extension> findPending();

    Extension increaseDownloadCount(int id);

    void loadFeatured();

    void updateMostRecent();

    Extension reloadExtension(Extension extension);

    boolean checkName(String name);
}
