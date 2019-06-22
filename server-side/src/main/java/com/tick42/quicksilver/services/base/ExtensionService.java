package com.tick42.quicksilver.services.base;


import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.HomePageDTO;
import com.tick42.quicksilver.models.DTO.PageDTO;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Spec.ExtensionSpec;
import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;

import java.util.List;
import java.util.Set;

public interface ExtensionService {

    Extension create(ExtensionSpec model, UserModel user, Set<Tag> tags);

    Extension findById(int id, UserDetails loggedUser);

    Extension update(ExtensionSpec extension, UserModel user, Set<Tag> tags);

    Extension save(Extension extension);

    void delete(int id, UserModel userModel);

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
