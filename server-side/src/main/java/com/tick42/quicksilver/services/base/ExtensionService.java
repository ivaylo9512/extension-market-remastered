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

    ExtensionDTO save(Extension extension);

    void delete(int id, int userId);

    HomePageDTO getHomeExtensions(Integer mostRecentCount, Integer mostDownloadedCount);

    PageDTO<ExtensionDTO> findAll(String name, String orderBy, Integer page, Integer perPage);

    List<ExtensionDTO> findFeatured();

    ExtensionDTO setPublishedState(int id, String newState);

    ExtensionDTO setFeaturedState(int id, String newState);

    List<ExtensionDTO> findPending();

    List<ExtensionDTO> generateExtensionDTOList(List<Extension> extensions);

    ExtensionDTO generateExtensionDTO(Extension extension);

    ExtensionDTO fetchGitHub(int extensionId, int userId);

    ExtensionDTO increaseDownloadCount(int id);

    void loadFeatured();

    void updateMostRecent();

    ExtensionDTO reloadExtension(Extension extension);
}
