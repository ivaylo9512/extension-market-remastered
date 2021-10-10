package com.tick42.quicksilver.services;

import com.tick42.quicksilver.controllers.ExtensionController;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.*;
import com.tick42.quicksilver.security.Jwt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

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

    private final UserModel userModel = new UserModel(1, "username", "email", "password", "ROLE_ADMIN", "info", "Bulgaria");
    private final UserDetails user = new UserDetails(userModel, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    private final String token = "Token " + Jwt.generate(user);
    private final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getId());

    private final Extension extension = createExtension();
    private final ExtensionDto extensionDto = new ExtensionDto(extension);



    @Test
    public void setPending() {
        when(extensionService.setPending(extension.getId(), true)).thenReturn(extension);

        ExtensionDto extensionDto = extensionController.setPending(extension.getId(), true);

        assertExtensions(extensionDto);
    }

    @Test
    public void setFeatured() {
        when(extensionService.setFeatured(extension.getId(), true)).thenReturn(extension);

        ExtensionDto extensionDto = extensionController.setFeatured(extension.getId(), true);

        assertExtensions(extensionDto);
    }

    @Test
    public void findByTag() {
        List<Extension> extensions = List.of(extension, new Extension(3, userModel));

        when(extensionService.findByTag("name", 10, 5)).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findByTag("name", 10, 5L);

        assertExtensions(page.getData().get(0));
        assertEquals(page.getData().get(1).getId(), extensions.get(1).getId());
        assertEquals(page.getTotalResults(), 2);
    }

    @Test
    public void isNameAvailable(){
        when(extensionService.isNameAvailable("name")).thenReturn(true);

        boolean isAvailable = extensionController.isNameAvailable("name");

        assertTrue(isAvailable);
    }
    @Test
    public void findById(){
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", token);

        when(ratingService.userRatingForExtension(extension.getId(), user.getId())).thenReturn(5);
        when(extensionService.findById(eq(extension.getId()), eq(user))).thenReturn(extension);

        ExtensionDto extensionDto = extensionController.findById(extension.getId(), request);

        assertEquals(extensionDto.getCurrentUserRatingValue(), 5);
        assertExtensions(extensionDto);
        verify(ratingService, times(1)).userRatingForExtension(extension.getId(), user.getId());
    }

    @Test
    public void findById_WithoutLogged(){
        when(extensionService.findById(extension.getId(), null)).thenReturn(extension);

        ExtensionDto extensionDto = extensionController.findById(extension.getId(), new MockHttpServletRequest());

        assertEquals(extensionDto.getCurrentUserRatingValue(), 0);
        assertExtensions(extensionDto);
        verify(ratingService, times(0)).userRatingForExtension(extension.getId(), user.getId());
    }

    @Test
    public void delete() {
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userService.findById(user.getId(), user)).thenReturn(userModel);
        when(extensionService.delete(extension.getId(), userModel)).thenReturn(extension);

        extensionController.delete(extension.getId());

        verify(ratingService, times(1)).updateRatingOnExtensionDelete(extension);
    }

    @Test
    public void findFeatured() {
        List<Extension> featured = List.of(extension, new Extension(3, userModel));

        when(extensionService.findFeatured()).thenReturn(featured);

        List<ExtensionDto> featuredDto = extensionController.findFeatured();

        assertExtensions(featuredDto.get(0));
        assertEquals(featuredDto.get(1).getId(), featured.get(1).getId());
    }

    @Test
    public void findHomeExtensions(){
        List<Extension> mostRecent = List.of(new Extension(2, userModel), new Extension(3, userModel));
        List<Extension> featured = List.of(new Extension(4, userModel), new Extension(5, userModel));
        List<Extension> mostDownloaded = List.of(new Extension(6, userModel), new Extension(8, userModel));

        when(extensionService.findMostRecent(5)).thenReturn(mostRecent);
        when(extensionService.findFeatured()).thenReturn(featured);
        when(extensionService.findAllByDownloaded(Integer.MAX_VALUE, 6, "", 0)).thenReturn(new PageImpl<>(mostDownloaded));

        HomePageDto pageDto = extensionController.findHomeExtensions(5, 6);
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

        List<Extension> extensions = List.of(new Extension(2, userModel), new Extension(3, userModel));

        when(extensionService.findAllByCommitDate(dateTime, 7, "name", 5)).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findAllByCommitDate("name", dateTime, 5, 7);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(0).getId(), 2);
        assertEquals(page.getData().get(1).getId(), 3);
    }
    @Test
    public void findAllByUploadDate(){
        LocalDateTime dateTime = LocalDateTime.now();

        List<Extension> extensions = List.of(new Extension(2, userModel), new Extension(3, userModel));

        when(extensionService.findAllByUploadDate(dateTime, 10, "name", 5)).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findAllByUploadDate("name", dateTime, 5, 10);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(0).getId(), 2);
        assertEquals(page.getData().get(1).getId(), 3);
    }

    @Test
    public void findAllByName() {
        List<Extension> extensions = List.of(new Extension(2, userModel), new Extension(3, userModel));

        when(extensionService.findAllByName("lastName", 10, "name")).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findAllByName("name", "lastName", 10);

        assertEquals(page.getTotalResults(), 2);
        assertEquals(page.getData().get(0).getId(), 2);
        assertEquals(page.getData().get(1).getId(), 3);
    }

    @Test
    public void findUserExtensions(){
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        List<Extension> extensions = List.of(extension, new Extension(3, userModel));

        when(extensionService.findUserExtensions(3, 5, userModel)).thenReturn(new PageImpl<>(extensions));
        when(userService.getById(user.getId())).thenReturn(userModel);

        PageDto<ExtensionDto> page = extensionController.findUserExtensions(3, 5L);

        assertExtensions(page.getData().get(0));
        assertEquals(page.getData().get(1).getId(), extensions.get(1).getId());
        assertEquals(page.getTotalResults(), 2);
    }

    @Test
    public void findByPending() {
        List<Extension> extensions = List.of(extension, new Extension(3, userModel));

        when(extensionService.findByPending(true, 3, 5L)).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findByPending(true, 3, 5L);

        assertExtensions(page.getData().get(0));
        assertEquals(page.getData().get(1).getId(), extensions.get(1).getId());
        assertEquals(page.getTotalResults(), 2);
    }

    @Test
    public void findByPending_WithNullLastId() {
        List<Extension> extensions = List.of(extension, new Extension(3, userModel));

        when(extensionService.findByPending(true, 3, 0)).thenReturn(new PageImpl<>(extensions));

        PageDto<ExtensionDto> page = extensionController.findByPending(true, 3, null);

        assertExtensions(page.getData().get(0));
        assertEquals(page.getData().get(1).getId(), extensions.get(1).getId());
        assertEquals(page.getTotalResults(), 2);
    }

    private Extension createExtension(){
        Set<Tag> tags = Set.of(new Tag("app"), new Tag("c"), new Tag("auto"), new Tag("repo"));

        GitHubModel github = new GitHubModel("ivaylo9512", "extension-market-remastered");
        github.setId(1);
        github.setPullRequests(0);
        github.setOpenIssues(0);
        github.setLastCommit(LocalDateTime.of(2020, Month.SEPTEMBER, 7, 5, 23, 44));
        github.setLastSuccess(LocalDateTime.of(2020, Month.SEPTEMBER, 10, 5, 37, 17));

        File file = new File();
        File image = new File();
        File cover = new File();

        file.setId(10);
        file.setDownloadCount(30);
        file.setExtensionType("txt");
        file.setResourceType("file");

        image.setId(3);
        image.setExtensionType("png");
        image.setResourceType("logo");

        cover.setId(4);
        cover.setExtensionType("png");
        cover.setResourceType("cover");

        Extension extension = new Extension(1, "Extension Market", "Extension market application.", "1", tags, github, userModel);
        extension.setRating(5);
        extension.setTimesRated(1);
        extension.setUploadDate(LocalDateTime.of(2021, Month.FEBRUARY, 1, 22, 32, 46));
        extension.setFile(file);
        extension.setImage(image);
        extension.setCover(cover);
        extension.setPending(false);
        extension.setFeatured(true);

        return extension;
    }

    private void assertExtensions(ExtensionDto extension){
        assertEquals(extension.getId(), extensionDto.getId());
        assertEquals(extension.getCoverName(), extensionDto.getCoverName());
        assertEquals(extension.getImageName(), extensionDto.getImageName());
        assertEquals(extension.getFileName(), extensionDto.getFileName());
        assertEquals(extension.getDescription(), extensionDto.getDescription());
        assertEquals(extension.getTags(), extensionDto.getTags());
        assertEquals(extension.getUploadDate(), extensionDto.getUploadDate());
        assertEquals(extension.getName(), extensionDto.getName());
        assertEquals(extension.getRating(), extensionDto.getRating());
        assertEquals(extension.getTimesRated(), extensionDto.getTimesRated());
        assertEquals(extension.getTimesDownloaded(), extensionDto.getTimesDownloaded());
        assertEquals(extension.getOwnerId(), extensionDto.getOwnerId());
        assertEquals(extension.getOwnerName(), extensionDto.getOwnerName());
        assertEquals(extension.getVersion(), extensionDto.getVersion());

        GitHubDto gitHub = extension.getGithub();
        assertEquals(gitHub.getId(), extensionDto.getGithub().getId());
        assertEquals(gitHub.getUser(), extensionDto.getGithub().getUser());
        assertEquals(gitHub.getRepo(), extensionDto.getGithub().getRepo());
    }
}
