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
public class ExtensionServiceImplTests {
    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagService tagService;

    @Mock
    private GitHubService gitHubService;

    @InjectMocks
    private ExtensionServiceImpl extensionService;


    @Test
    public void toggleFeaturedState() {
        Extension extension = new Extension();
        extension.isFeatured(false);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newExtension = extensionService.setFeaturedState(1, "feature");

        assertTrue(newExtension.isFeatured());
    }

    @Test
    public void toggleFeaturedState_WhenFeatured() {
        Extension extension = new Extension();
        extension.isFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension newExtension = extensionService.setFeaturedState(1, "unfeature");

        assertFalse(extension.isFeatured());
    }

    @Test
    public void setFeaturedState_whenGivenInvalidParameter_shouldThrow() {
        Extension extension = new Extension();
        extension.isFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> extensionService.setFeaturedState(1, "invalidInput"));

        assertEquals(thrown.getMessage(), "\"invalidInput\" is not a valid featured state. Use \"feature\" or \"unfeature\".");
    }

    @Test
    public void togglePending_WhenPending() {
        // Arrange
        Extension extension = new Extension();
        extension.setIsPending(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension extensionShouldBePending = extensionService.setPublishedState(1, "publish");

        assertFalse(extensionShouldBePending.getIsPending());
    }

    @Test
    public void togglePending_WhenNotPending() {
        Extension extension = new Extension();
        extension.setIsPending(false);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        Extension extensionShouldBeUnpublished = extensionService.setPublishedState(1, "unpublish");

        assertTrue(extensionShouldBeUnpublished.getIsPending());
    }

    @Test
    public void setPublishedState_whenGivenInvalidParameter_shouldThrow() {
        // Arrange
        Extension extension = new Extension();
        extension.isFeatured(true);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        InvalidInputException thrown = assertThrows(InvalidInputException.class,
                () -> extensionService.setPublishedState(1, "invalidInput"));

        assertEquals(thrown.getMessage(), "\"invalidInput\" is not a valid extension state. Use \"publish\" or \"unpublish\".");
    }

    @Test
    public void findPending() {
        Extension extension1 = new Extension();
        Extension extension2 = new Extension();
        extension1.setIsPending(true);
        extension2.setIsPending(true);
        List<Extension> extensions = Arrays.asList(extension1, extension2);

        when(extensionRepository.findByPending(true)).thenReturn(extensions);

        List<Extension> pendingExtensionDtos = extensionService.findPending();

        assertEquals(2, pendingExtensionDtos.size());
        assertTrue(pendingExtensionDtos.get(0).getIsPending());
        assertTrue(pendingExtensionDtos.get(1).getIsPending());
    }

    @Test
    public void findFeatured_shouldReturnListOfFeaturedExtensionDTOs() {
        Extension extension1 = new Extension();
        Extension extension2 = new Extension();
        extension1.isFeatured(true);
        extension2.isFeatured(true);
        List<Extension> extensions = Arrays.asList(extension1, extension2);

        when(extensionRepository.findByFeatured(true)).thenReturn(extensions);

        List<Extension> featuredExtensionDtos = extensionService.findFeatured();

        assertEquals(2, featuredExtensionDtos.size());
        assertTrue(featuredExtensionDtos.get(0).isFeatured());
        assertTrue(featuredExtensionDtos.get(1).isFeatured());
    }

    @Test
    public void findById_whenExtensionDoesNotExist_EntityNotFound() {
        UserDetails user = new UserDetails("text", "test", new ArrayList<>(), 1);
        when(extensionRepository.findById(1L)).thenReturn(null);

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> extensionService.findById(1, user));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    public void findById_WhenOwnerIsInactiveAndUserIsNull_EntityUnavailable() {
        UserModel owner = new UserModel();
        owner.setIsActive(false);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        EntityUnavailableException thrown = assertThrows(
                EntityUnavailableException.class,
                () -> extensionService.findById(1, null));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenOwnerIsInactiveAndUserIsNotAdmin_EntityUnavailable() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setIsActive(false);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        EntityUnavailableException thrown = assertThrows(
                EntityUnavailableException.class,
                () -> extensionService.findById(1, user));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenExtensionIsPendingAndOwnerIsActiveAndUserIsNull_EntityUnavailable() {
        UserModel owner = new UserModel();
        owner.setIsActive(true);

        Extension extension = new Extension();
        extension.setIsPending(true);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        EntityUnavailableException thrown = assertThrows(
                EntityUnavailableException.class,
                () -> extensionService.findById(1, null));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenExtensionIsPendingAndOwnerIsActiveAndUserIsNotOwnerAndNotAdmin_EntityUnavailable() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setIsActive(true);
        owner.setId(2);

        Extension extension = new Extension();
        extension.setIsPending(true);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        EntityUnavailableException thrown = assertThrows(
                EntityUnavailableException.class,
                () -> extensionService.findById(1, user));

        assertEquals(thrown.getMessage(), "Extension is not available.");
    }

    @Test
    public void findById_whenExtensionIsPendingAndOwnerIsInactiveAndUserIsAdmin() {
        Collection<GrantedAuthority> authorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        UserDetails user = new UserDetails("TEST", "TEST", authorities, 1);

        UserModel owner = new UserModel();
        owner.setIsActive(false);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setIsPending(true);
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
        owner.setIsActive(true);
        owner.setId(1);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setIsPending(true);
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
        owner.setIsActive(true);
        owner.setId(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setIsPending(false);
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
        extension.setGithub(new GitHubModel("gitHubLink", "username", "repo"));

        Extension foundExtension = new Extension();
        extension.setId(1);
        extension.setOwner(loggedUser);


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
        extension.setGithub(new GitHubModel("gitHubLink", "username", "repo"));

        Extension foundExtension = new Extension();
        extension.setId(1);
        extension.setOwner(owner);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(foundExtension));
        when(extensionRepository.save(extension)).thenReturn(extension);

        Extension actualExtension = extensionService.update(extension, loggedUser);

        assertEquals(extension, actualExtension);
    }

    @Test
    public void delete_whenExtensionDoesNotExist_EntityNotFound() {
        when(extensionRepository.findById(1L)).thenReturn(null);

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
        String name = "name";
        int page = 5;
        int pageSize = 10;
        Long totalResults = 21L;

        when(extensionRepository.getTotalResults(name)).thenReturn(totalResults);

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> extensionService.findPageWithCriteria("", "date", page, pageSize));

        assertEquals(thrown.getMessage(), "Page 3 is the last page. Page 5 is invalid.");

    }

    @Test
    public void findAll_whenPageMoreThanTotalPagesAndTotalResultsAreZero() {
        String name = "name";
        int page = 5;
        int pageSize = 2;
        Long totalResults = 20L;
        Extension extension = new Extension("name", "description", "version", new UserModel());
        Extension extension1 = new Extension("name", "description", "version", new UserModel());
        List<Extension> extensions = List.of(extension, extension1);

        when(extensionRepository.getTotalResults(name)).thenReturn(totalResults);
        when(extensionRepository.findAllOrderedBy(name, PageRequest.of(page,
                pageSize, Sort.Direction.ASC, "name"))).thenReturn(extensions);


        PageDto<Extension> pageDto = extensionService.findPageWithCriteria(name, "name", page, pageSize);

        assertEquals(pageDto.getExtensions(), extensions);
        assertEquals(pageDto.getTotalResults(), 20);
        assertEquals(pageDto.getCurrentPage(), 5);
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

        assertEquals(thrown.getMessage(), "orderType is not a valid parameter. Use \"date\", \"commits\", \"name\" or \"downloads\".");
    }

}
