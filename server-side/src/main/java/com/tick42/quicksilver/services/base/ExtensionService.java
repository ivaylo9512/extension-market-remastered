package com.tick42.quicksilver.services.base;


import com.tick42.quicksilver.models.DTO.ExtensionDTO;
import com.tick42.quicksilver.models.DTO.HomePageDTO;
import com.tick42.quicksilver.models.DTO.PageDTO;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Spec.ExtensionSpec;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

public interface ExtensionService {

    ExtensionDTO create(ExtensionSpec model, int id);

    ExtensionDTO findById(int id, UserDetails loggedUser);

    ExtensionDTO update(int extensionId, ExtensionSpec extension, int userId);

    void delete(int id, int userId);

    HomePageDTO getHomeExtensions(Integer mostRecentCount, Integer mostDownloadedCount);

    PageDTO<ExtensionDTO> findAll(String name, String orderBy, Integer page, Integer perPage);

    List<ExtensionDTO> findFeatured();

    ExtensionDTO setPublishedState(int id, String newState);

    ExtensionDTO setFeaturedState(int id, String newState);

    List<ExtensionDTO> findPending();

    List<ExtensionDTO> generateExtensionDTOList(List<Extension> extensions);

    ExtensionDTO fetchGitHub(int extensionId, int userId);

    ExtensionDTO increaseDownloadCount(int id);

    @EventListener
    void loadFeatured(ApplicationReadyEvent event);

    @EventListener
    void loadMostRecent(ApplicationReadyEvent event);
}
