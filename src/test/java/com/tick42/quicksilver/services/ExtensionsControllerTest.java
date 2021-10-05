package com.tick42.quicksilver.services;

import com.tick42.quicksilver.controllers.ExtensionController;
import com.tick42.quicksilver.models.Dtos.*;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExtensionsControllerTest {
    @InjectMocks
    private ExtensionController extensionController;

    @Mock
    private ExtensionServiceImpl extensionService;

    @Mock
    private FileServiceImpl fileService;

    @Mock
    private RatingServiceImpl ratingService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private TagServiceImpl tagService;

    @Mock
    private GitHubServiceImpl gitHubService;

    @Test
    public void getHomeExtensions(){
        UserModel owner = new UserModel();
        List<Extension> mostRecent = List.of(new Extension(2, owner), new Extension(3, owner));
        List<Extension> featured = List.of(new Extension(4, owner), new Extension(5, owner));
        List<Extension> mostDownloaded = List.of(new Extension(6, owner), new Extension(8, owner));

        when(extensionService.findMostRecent(5)).thenReturn(mostRecent);
        when(extensionService.findFeatured()).thenReturn(featured);
        when(extensionService.findAllByDownloaded(6, Integer.MAX_VALUE, "", 0)).thenReturn(new PageImpl<>(mostDownloaded));

        HomePageDto pageDto = extensionController.getHomeExtensions(5, 6);
        List<ExtensionDto> featuredDto = pageDto.getFeatured();
        List<ExtensionDto> mostRecentDto = pageDto.getMostRecent();
        List<ExtensionDto> mostDownloadedDto = pageDto.getMostDownloaded();

        assertEquals(featuredDto.get(0).getId(), featured.get(0).getId());
        assertEquals(featuredDto.get(1).getId(), featured.get(1).getId());
        assertEquals(mostRecentDto.get(0).getId(), mostRecent.get(0).getId());
        assertEquals(mostRecentDto.get(1).getId(), mostRecent.get(1).getId());
        assertEquals(mostDownloadedDto.get(0).getId(), mostDownloaded.get(0).getId());
        assertEquals(mostDownloadedDto.get(1).getId(), mostDownloaded.get(1).getId());
    }

    @Test
    public void findAllByCommitDate(){
        LocalDateTime dateTime = LocalDateTime.now();

        UserModel owner = new UserModel();
        List<Extension> extensions = List.of(new Extension(2, owner), new Extension(3, owner));

        when(extensionService.findAllByCommitDate(dateTime, 7, "name", 5)).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findAllByCommitDate("name", dateTime, 5, 7);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(0).getId(), 2);
        assertEquals(page.getData().get(1).getId(), 3);
    }
    @Test
    public void findAllByUploadDate(){
        LocalDateTime dateTime = LocalDateTime.now();

        UserModel owner = new UserModel();
        List<Extension> extensions = List.of(new Extension(2, owner), new Extension(3, owner));

        when(extensionService.findAllByUploadDate(dateTime, 10, "name", 5)).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findAllByUploadDate("name", dateTime, 5, 10);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(0).getId(), 2);
        assertEquals(page.getData().get(1).getId(), 3);
    }

    @Test
    public void findAllByName() {
        UserModel owner = new UserModel();
        List<Extension> extensions = List.of(new Extension(2, owner), new Extension(3, owner));

        when(extensionService.findAllByName("lastName", 10, "name")).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findAllByName("name", "lastName", 10);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(0).getId(), 2);
        assertEquals(page.getData().get(1).getId(), 3);
    }
}
