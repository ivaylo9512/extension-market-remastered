package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.*;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExtensionService {
    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private ExtensionServiceImpl extensionService;

    private final Page<Extension> mostRecentPage = createMostRecent();

    @BeforeEach
    private void create(){
        extensionService = createMock();
        resetMostRecent();
    }

    private void resetMostRecent(){
        when(extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, 5))).thenReturn(mostRecentPage);
        this.extensionService.updateMostRecent();
    }

    private Page<Extension> createMostRecent(){
        Extension extension = new Extension();
        Extension extension1 = new Extension();
        Extension extension2 = new Extension();
        Extension extension3 = new Extension();
        Extension extension4 = new Extension();

        extension.setId(2);
        extension1.setId(2);
        extension2.setId(5);
        extension3.setId(8);
        extension4.setId(10);

        return new PageImpl<>(List.of(extension, extension1, extension2, extension3, extension4));
    }

    private ExtensionServiceImpl createMock(){
        Page<Extension> page = new PageImpl<>(new ArrayList<>());
        when(extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, 5))).thenReturn(page);
        return Mockito.spy(new ExtensionServiceImpl(extensionRepository));
    }

    @Test
    public void setFeatured_WithTrue() {
        Extension extension = new Extension();
        extension.setFeatured(false);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newExtension = extensionService.setFeatured(1, true);

        assertTrue(newExtension.isFeatured());
        verify(extensionService, times(1)).updateFeatured(extension);
    }

    @Test
    public void setFeatured_WithFalse() {
        Extension extension = new Extension();
        extension.setFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newState = extensionService.setFeatured(1, false);

        assertFalse(newState.isFeatured());
        verify(extensionService, times(1)).updateFeatured(extension);
    }

    @Test
    public void setPublished_WithFalse() {
        Extension extension = new Extension();
        extension.setPending(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newState = extensionService.setPending(1, false);

        assertFalse(newState.isPending());
        verify(extensionService, times(2)).updateMostRecent();
    }

    @Test
    public void setPublished_WithTrue() {
        Extension extension = new Extension();
        extension.setPending(false);
        extension.setFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newState = extensionService.setPending(1, true);

        assertTrue(newState.isPending());
        assertFalse(newState.isFeatured());
        verify(extensionRepository, times(1)).save(extension);
        verify(extensionService, times(2)).updateMostRecent();
    }

    @Test
    public void findPending() {
        Extension extension1 = new Extension();
        Extension extension2 = new Extension();
        extension1.setPending(true);
        extension2.setPending(true);
        List<Extension> extensions = Arrays.asList(extension1, extension2);

        when(extensionRepository.findByPending(true, 1, PageRequest.of(0, 2,
                Sort.Direction.ASC, "id"))).thenReturn(new PageImpl<>(extensions));

        Page<Extension> pending = extensionService.findByPending(true, 2, 1);

        assertEquals(2, pending.getContent().size());
        assertTrue(pending.getContent().get(0).isPending());
        assertTrue(pending.getContent().get(1).isPending());
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
        Page<Extension> page = new PageImpl<>(new ArrayList<>());
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
        Page<Extension> page = new PageImpl<>(extensions);

        when(extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, 5))).thenReturn(page);

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
        Page<Extension> page = new PageImpl<>(extensions);

        when(extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, 10))).thenReturn(page);

        List<Extension> mostRecent = extensionService.findMostRecent(10);

        assertEquals(extensions, mostRecent);
    }

    @Test
    public void findMostRecent_WhenCountIsNull(){
        extensionService.updateMostRecent();
        List<Extension> mostRecent = extensionService.findMostRecent(null);

        assertEquals(mostRecent, mostRecentPage.getContent());
    }

    @Test
    public void findMostDownloaded(){
        List<Extension> extensions = List.of(new Extension(), new Extension(), new Extension(),
                new Extension(), new Extension());

        Page<Extension> page = new PageImpl<>(extensions);

        when(extensionRepository.findAllByDownloaded(Integer.MAX_VALUE, "", 0, PageRequest.of(0, 5))).thenReturn(page);

        Page<Extension> mostDownloaded = extensionService.findAllByDownloaded(Integer.MAX_VALUE, 5, "", 0);

        assertEquals(extensions, mostDownloaded.getContent());
        assertEquals(extensions.size(), mostDownloaded.getTotalElements());
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
    public void updateFeatured_WithNotFeatured(){
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
        Page<Extension> page = new PageImpl<>(extensions);

        when(extensionRepository.findByFeatured(true)).thenReturn(extensions);
        extensionService.loadFeatured();

        when(extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, extensionService.getMostRecentQueueLimit()))).thenReturn(page);
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
        Page<Extension> page = new PageImpl<>(extensions2);

        when(extensionRepository.findByFeatured(true)).thenReturn(extensions);
        extensionService.loadFeatured();

        when(extensionRepository.findAllByUploadDate(LocalDateTime.of(9999, Month.DECEMBER, 31, 23, 23, 59, 59), "", 0, PageRequest.of(0, extensionService.getMostRecentQueueLimit()))).thenReturn(page);
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
