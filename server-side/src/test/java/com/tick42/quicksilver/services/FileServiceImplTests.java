package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.UnauthorizedException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceImplTests {
    @Mock
    MultipartFile multipartFile;

    @Mock
    ExtensionRepository extensionRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    public void storeFile_whenExtensionNotExisting_ShouldThrow() {
        when(extensionRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> fileService.create(multipartFile, "name"));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    @Test
    public void storeImage_whenExtensionNotExisting_ShouldThrow() {
        when(extensionRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> fileService.create(multipartFile, "name"));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    @Test
    public void storeFile_whenUserNotExisting_ShouldThrow() {
        Extension extension = new Extension();
        when(userRepository.findById(1L)).thenReturn(null);
        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> fileService.create(multipartFile, "name"));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    @Test
    public void storeImage_whenUserNotExisting_ShouldThrow() {
        //Arrange
        Extension extension = new Extension();
        when(userRepository.findById(1L)).thenReturn(null);
        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> fileService.create(multipartFile, "name"));

        assertEquals(thrown.getMessage(), "Extension not found.");
    }

    @Test
    public void storeFile_whenUserIsNotOwnerAndNotAdmin_ShouldThrow() {
        //Arrange
        UserModel userModel = new UserModel();
        userModel.setId(1);
        userModel.setRole("USER");
        UserModel owner = new UserModel();
        owner.setId(2);
        Extension extension = new Extension();
        extension.setOwner(owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> fileService.create(multipartFile, "name"));

        assertEquals(thrown.getMessage(), "Unauthorized.");
    }

    @Test
    public void storeImage_whenUserIsNotOwnerAndNotAdmin_ShouldThrow() {
        UserModel userModel = new UserModel();
        userModel.setId(1);
        userModel.setRole("USER");
        UserModel owner = new UserModel();

        owner.setId(2);

        Extension extension = new Extension();
        extension.setOwner(owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userModel));
        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        UnauthorizedException thrown = assertThrows(
                UnauthorizedException.class,
                () -> fileService.create(multipartFile, "name"));

        assertEquals(thrown.getMessage(), "Unauthorized.");
    }
}
