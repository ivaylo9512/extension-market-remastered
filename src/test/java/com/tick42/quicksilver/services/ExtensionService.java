package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.PageDto;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import com.tick42.quicksilver.services.base.GitHubService;
import com.tick42.quicksilver.services.base.TagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import javax.persistence.EntityNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExtensionService {
    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagService tagService;

    @Mock
    private GitHubService gitHubService;

    @Spy
    @InjectMocks
    private ExtensionServiceImpl extensionService;


    @Test
    public void toggleFeaturedState() {
        Extension extension = new Extension();
        extension.setFeatured(false);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newExtension = extensionService.setFeaturedState(1, "feature");

        assertTrue(newExtension.isFeatured());
    }

    @Test
    public void toggleFeaturedState_WhenFeatured() {
        Extension extension = new Extension();
        extension.setFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newExtension = extensionService.setFeaturedState(1, "unfeature");

        assertFalse(extension.isFeatured());
    }

    @Test
    public void setFeaturedState_whenGivenInvalidParameter_shouldThrow() {
        Extension extension = new Extension();
        extension.setFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> extensionService.setFeaturedState(1, "invalidInput"));

        assertEquals(thrown.getMessage(), "\"invalidInput\" is not a valid featured state. Use \"feature\" or \"unfeature\".");
    }

    @Test
    public void togglePending_WhenPending() {
        Extension extension = new Extension();
        extension.setPending(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension extensionShouldBePending = extensionService.setPublishedState(1, "publish");

        assertFalse(extensionShouldBePending.isPending());
    }

    @Test
    public void togglePending_WhenNotPending() {
        Extension extension = new Extension();
        extension.setPending(false);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension extensionShouldBeUnpublished = extensionService.setPublishedState(1, "unpublish");

        assertTrue(extensionShouldBeUnpublished.isPending());
    }

    @Test
    public void setPublishedState_whenGivenInvalidParameter_shouldThrow() {
        Extension extension = new Extension();
        extension.setFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> extensionService.setPublishedState(1, "invalidInput"));

        assertEquals(thrown.getMessage(), "\"invalidInput\" is not a valid extension state. Use \"publish\" or \"unpublish\".");
    }

    @Test
    public void findPending() {
        Extension extension1 = new Extension();
        Extension extension2 = new Extension();
        extension1.setPending(true);
        extension2.setPending(true);
        List<Extension> extensions = Arrays.asList(extension1, extension2);

        when(extensionRepository.findByPending(true)).thenReturn(extensions);

        List<Extension> pendingExtensionDtos = extensionService.findPending();

        assertEquals(2, pendingExtensionDtos.size());
        assertTrue(pendingExtensionDtos.get(0).isPending());
        assertTrue(pendingExtensionDtos.get(1).isPending());
    }

    @Test
    public void findById_whenExtensionDoesNotExist_EntityNotFound() {
        UserDetails user = new UserDetails("text", "test", new ArrayList<>(), 1);
        when(extensionRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> extensionService.findById(1, user));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    @Test
    public void findById_WhenOwnerIsInactiveAndUserIsNull_EntityUnavailable() {
        UserModel owner = new UserModel();
        owner.setActive(false);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> extensionService.findById(1, null));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenOwnerIsInactiveAndUserIsNotAdmin_EntityUnavailable() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setActive(false);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> extensionService.findById(1, user));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenExtensionIsPendingAndOwnerIsActiveAndUserIsNull_EntityUnavailable() {
        UserModel owner = new UserModel();
        owner.setActive(true);

        Extension extension = new Extension();
        extension.setPending(true);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> extensionService.findById(1, null));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenExtensionIsPendingAndOwnerIsActiveAndUserIsNotOwnerAndNotAdmin_EntityUnavailable() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setActive(true);
        owner.setId(2);

        Extension extension = new Extension();
        extension.setPending(true);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> extensionService.findById(1, user));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenExtensionIsPendingAndOwnerIsInactiveAndUserIsAdmin() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setActive(false);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setPending(true);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension expectedExtension = extensionService.findById(1, user);

        assertEquals(extension.getId(), expectedExtension.getId());
    }

    @Test
    public void findById_whenExtensionIsPendingAndOwnerIsActiveAndUserIsNotAdmin() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setActive(true);
        owner.setId(1);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setPending(true);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension expectedExtension = extensionService.findById(1, user);

        assertEquals(extension.getId(), expectedExtension.getId());
    }

    @Test
    public void findById_whenExtensionIsNotPendingAndOwnerIsActiveAndUserIsNotOwnerAndNotAdmin() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setActive(true);
        owner.setId(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setPending(false);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension expectedExtension = extensionService.findById(1, user);

        assertEquals(extension.getId(), expectedExtension.getId());
    }

    @Test
    public void update_whenExtensionNonExist_ShouldThrow() {
        Extension extension = new Extension();
        extension.setId(1);

        when(extensionRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> extensionService.update(extension, new UserModel()));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    @Test
    public void update_whenLoggedUserIsNotOwnerAndNotAdmin_ShouldThrow() {
        UserModel loggedUser = new UserModel();
        loggedUser.setId(1);
        loggedUser.setRole("ROLE_USER");

        UserModel owner = new UserModel();
        owner.setId(2);

        Extension extension = new Extension();
        extension.setOwner(owner);
        extension.setId(1);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> extensionService.update(extension, loggedUser)
        );

        assertEquals(thrown.getMessage(), "You are not authorized to edit this extension.");
    }

    @Test
    public void update_whenLoggedUserIsOwner() {
        UserModel loggedUser = new UserModel();
        loggedUser.setId(1);
        loggedUser.setRole("ROLE_USER");

        UserModel owner = new UserModel();
        owner.setId(1);

        Extension extension = new Extension("name", "description", "version", owner);
        extension.setTags(new HashSet<>(List.of(new Tag("tag1"), new Tag("tag2"))));
        extension.setGithub(new GitHubModel("username", "repo"));
        extension.setId(1);

        Extension foundExtension = new Extension();
        foundExtension.setId(1);
        foundExtension.setOwner(loggedUser);


        when(extensionRepository.findById(1L)).thenReturn(Optional.of(foundExtension));
        when(extensionRepository.save(extension)).thenReturn(extension);

        Extension actualExtension = extensionService.update(extension, loggedUser);

        assertEquals(extension, actualExtension);
    }

    @Test
    public void update_whenLoggedUserIsAdmin() {
        UserModel loggedUser = new UserModel();
        loggedUser.setId(2);
        loggedUser.setRole("ROLE_ADMIN");

        UserModel owner = new UserModel();
        owner.setId(1);

        Extension extension = new Extension("name", "description", "version", owner);
        extension.setTags(new HashSet<>(List.of(new Tag("tag1"), new Tag("tag2"))));
        extension.setGithub(new GitHubModel("username", "repo"));
        extension.setId(1);

        Extension foundExtension = new Extension();
        foundExtension.setId(1);
        foundExtension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(foundExtension));
        when(extensionRepository.save(extension)).thenReturn(extension);

        Extension actualExtension = extensionService.update(extension, loggedUser);

        assertEquals(extension, actualExtension);
    }

    @Test
    public void delete_whenExtensionDoesNotExist_EntityNotFound() {
        when(extensionRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> extensionService.delete(1, new UserModel()));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    @Test
    public void delete_whenLoggedUserIsOwner() {
        UserModel loggedUser = new UserModel();
        loggedUser.setId(1);
        loggedUser.setRole("ROLE_USER");

        UserModel owner = new UserModel();
        owner.setId(1);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        extensionService.delete(1, loggedUser);
    }

    @Test
    public void delete_whenLoggedUserIsAdmin() {
        UserModel loggedUser = new UserModel();
        loggedUser.setId(1);
        loggedUser.setRole("ROLE_ADMIN");

        UserModel owner = new UserModel();
        owner.setId(2);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        extensionService.delete(1, loggedUser);
    }

    @Test
    public void delete_WhenLoggedUserIsNotOwnerAndIsNotAdmin_Unauthorized() {
        UserModel loggedUser = new UserModel();
        loggedUser.setId(1);
        loggedUser.setRole("ROLE_USER");

        UserModel owner = new UserModel();
        owner.setId(2);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> extensionService.delete(1, loggedUser));

        assertEquals(thrown.getMessage(), "You are not authorized to delete this extension.");
    }

    @Test
    public void findAll_WhenPageMoreThanTotalPages_InvalidInput() {
        when(extensionRepository.getTotalResults("name")).thenReturn(21L);

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> extensionService.findPageWithCriteria("name", "date", 5, 10));

        assertEquals(thrown.getMessage(), "Page 3 is the last page. Page 5 is invalid.");

    }

    @Test
    public void findAll_whenPageMoreThanTotalPagesAndTotalResultsAreZero() {
        int page = 5;
        int pageSize = 2;
        Long totalResults = 20L;
        Extension extension = new Extension("extension", "description", "version", new UserModel());
        Extension extension1 = new Extension("extension", "description", "version", new UserModel());
        List<Extension> extensions = List.of(extension, extension1);

        when(extensionRepository.getTotalResults("extension")).thenReturn(totalResults);
        when(extensionRepository.findAllOrderedBy("extension", PageRequest.of(page,
                pageSize, Sort.Direction.ASC, "name"))).thenReturn(extensions);


        PageDto<Extension> pageDto = extensionService.findPageWithCriteria("extension", "name", page, pageSize);

        assertEquals(pageDto.getExtensions(), extensions);
        assertEquals(pageDto.getTotalResults(), totalResults);
        assertEquals(pageDto.getCurrentPage(), page);
    }

    @Test
    public void findAll_WhenInvalidParameter_InvalidInput() {
        String name = "name";
        String orderBy = "orderType";
        int page = 5;
        int pageSize = 10;
        Long totalResults = 500L;

        when(extensionRepository.getTotalResults(name)).thenReturn(totalResults);

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> extensionService.findPageWithCriteria(name, orderBy, page, pageSize));

        assertEquals(thrown.getMessage(), "\"orderType\" is not a valid parameter. Use \"date\", \"commits\", \"name\" or \"downloads\".");
    }

    @Test
    public void checkName(){
        when(extensionRepository.findByName("name")).thenReturn(new Extension());

        boolean isFound = extensionService.isNameAvailable("name");

        assertFalse(isFound);
    }

    @Test
    public void checkNameWhenNotFound(){
        when(extensionRepository.findByName("name")).thenReturn(null);

        boolean isFound = extensionService.isNameAvailable("name");

        assertTrue(isFound);
    }

    @Test
    public void findMostRecent(){
        List<Extension> extensions = List.of(new Extension(), new Extension(), new Extension(), new Extension(),
                new Extension(), new Extension(), new Extension());

        when(extensionRepository.findAllOrderedBy("", PageRequest.of(0, 5,
                Sort.Direction.DESC, "uploadDate"))).thenReturn(extensions);

        extensionService.updateMostRecent();
        List<Extension> mostRecent = extensionService.findMostRecent(3);

        assertEquals(mostRecent.size(), 3);
        assertEquals(extensions.get(0), mostRecent.get(0));
        assertEquals(extensions.get(1), mostRecent.get(1));
        assertEquals(extensions.get(2), mostRecent.get(2));
    }

    @Test
    public void findFeatured() {
        Extension extension = new Extension();
        Extension extension1 = new Extension();
        Extension extension2 = new Extension();

        extension.setId(1);
        extension1.setId(2);
        extension2.setId(3);

        List<Extension> extensions = List.of(extension, extension1, extension2);

        when(extensionRepository.findByFeatured(true)).thenReturn(extensions);

        extensionService.loadFeatured();
        List<Extension> featured = extensionService.findFeatured();

        assertEquals(extensions, featured);
    }

    @Test
    public void findMostRecent_WhenCountIsMoreThanMaxCount(){
        List<Extension> extensions = List.of(new Extension(), new Extension(), new Extension());

        when(extensionRepository.findAllOrderedBy("", PageRequest.of(0, 10,
                Sort.Direction.DESC, "uploadDate"))).thenReturn(extensions);

        List<Extension> mostRecent = extensionService.findMostRecent(10);

        assertEquals(extensions, mostRecent);
        verify(extensionRepository, times(1)).findAllOrderedBy("", PageRequest.of(0, 10,
                Sort.Direction.DESC, "uploadDate"));
    }

    @Test
    public void findMostRecent_WhenCountIsNull(){
        List<Extension> extensions = List.of(new Extension(), new Extension(), new Extension(), new Extension(),
                new Extension(), new Extension(), new Extension());

        when(extensionRepository.findAllOrderedBy("", PageRequest.of(0, 5,
                Sort.Direction.DESC, "uploadDate"))).thenReturn(extensions);


        extensionService.updateMostRecent();
        List<Extension> mostRecent = extensionService.findMostRecent(null);

        assertEquals(mostRecent.size(), 5);
    }

    @Test
    public void findMostDownloaded(){
        List<Extension> extensions = List.of(new Extension(), new Extension(), new Extension(),
                new Extension(), new Extension());

        when(extensionRepository.findAllOrderedBy("", PageRequest.of(0, 5,
                Sort.Direction.DESC, "timesDownloaded"))).thenReturn(extensions);

        List<Extension> mostDownloaded = extensionService.findMostDownloaded(5);

        assertEquals(extensions, mostDownloaded);
    }

    @Test
    public void updateFeatured(){
        Extension extension = new Extension();
        extension.setId(2);
        extension.setFeatured(true);

        extensionService.updateFeatured(extension);
        List<Extension> extensions = extensionService.findFeatured();

        assertEquals(extensions.get(0), extension);
    }

    @Test
    public void updateFeatured_WithUnfeatured(){
        Extension extension = new Extension();
        extension.setId(2);
        extension.setFeatured(true);

        when(extensionRepository.findByFeatured(true)).thenReturn(List.of(extension));
        extensionService.loadFeatured();
        List<Extension> extensions = extensionService.findFeatured();

        assertEquals(extensions.get(0), extension);

        extension.setFeatured(false);
        extensionService.updateFeatured(extension);
        List<Extension> featured = extensionService.findFeatured();

        assertEquals(featured.size(), 0);
    }

    @Test
    public void reloadExtension(){
        Extension extension = new Extension();
        extension.setId(2);
        extension.setDescription("description");
        extension.setFeatured(true);
        List<Extension> extensions = List.of(extension);

        when(extensionRepository.findByFeatured(true)).thenReturn(extensions);
        extensionService.loadFeatured();

        when(extensionRepository.findAllOrderedBy("", PageRequest.of(0, 5,
                Sort.Direction.DESC, "uploadDate"))).thenReturn(extensions);
        extensionService.updateMostRecent();

        extension.setDescription("newDescription");

        extensionService.reloadExtension(extension);

        List<Extension> featured = extensionService.findFeatured();
        List<Extension> mostRecent = extensionService.findMostRecent(null);

        assertEquals(featured.get(0).getDescription(), "newDescription");
        assertEquals(mostRecent.get(0).getDescription(), "newDescription");
    }

    @Test
    public void reloadFile() {
        Extension extension = new Extension();
        File file = new File();
        file.setId(1);
        file.setDownloadCount(2);

        Extension extension2 = new Extension();
        File file2 = new File();
        file2.setId(2);
        file2.setDownloadCount(2);

        extension.setFile(file);
        extension2.setFile(file2);

        List<Extension> extensions = List.of(extension);
        List<Extension> extensions2 = List.of(extension2);

        when(extensionRepository.findByFeatured(true)).thenReturn(extensions);
        extensionService.loadFeatured();

        when(extensionRepository.findAllOrderedBy("", PageRequest.of(0, 5,
                Sort.Direction.DESC, "uploadDate"))).thenReturn(extensions2);
        extensionService.updateMostRecent();

        File newFile = new File();
        newFile.setId(1);
        newFile.setDownloadCount(5);

        File newFile2 = new File();
        newFile2.setId(2);
        newFile2.setDownloadCount(5);

        extensionService.reloadFile(newFile);
        extensionService.reloadFile(newFile2);

        List<Extension> featured = extensionService.findFeatured();
        List<Extension> mostRecent = extensionService.findMostRecent(null);

        assertEquals(featured.get(0).getFile().getDownloadCount(), 5);
        assertEquals(mostRecent.get(0).getFile().getDownloadCount(), 5);
    }

    @Test
    public void save(){
        Extension extension = new Extension();

        when(extensionRepository.save(extension)).thenReturn(extension);

        Extension savedExtension = extensionService.save(extension);

        verify(extensionService, times(1)).save(extension);
        assertEquals(extension, savedExtension);
    }
}
