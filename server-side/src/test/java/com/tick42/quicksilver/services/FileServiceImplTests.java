package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.UnauthorizedExtensionModificationException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FileServiceImplTests {
    @Mock
    MultipartFile multipartFile;

    @Mock
    ExtensionRepository extensionRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test(expected = NullPointerException.class)
    public void storeFile_whenExtensionNotExisting_ShouldThrow() {
        //Arrange
        when(extensionRepository.findById(1L)).thenReturn(null);

        //Act
        fileService.create(multipartFile, "name");
    }

    @Test(expected = NullPointerException.class)
    public void storeImage_whenExtensionNotExisting_ShouldThrow() {
        //Arrange
        when(extensionRepository.findById(1L)).thenReturn(null);

        //Act
        fileService.create(multipartFile, "name");
    }

    @Test(expected = NullPointerException.class)
    public void storeFile_whenUserNotExisting_ShouldThrow() {
        //Arrange
        Extension extension = new Extension();
        when(userRepository.findById(1L)).thenReturn(null);
        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        //Act
        fileService.create(multipartFile, "name");
    }

    @Test(expected = NullPointerException.class)
    public void storeImage_whenUserNotExisting_ShouldThrow() {
        //Arrange
        Extension extension = new Extension();
        when(userRepository.findById(1L)).thenReturn(null);
        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));

        //Act
        fileService.create(multipartFile, "name");
    }

    @Test(expected = UnauthorizedExtensionModificationException.class)
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

        //Act
        fileService.create(multipartFile, "name");
    }

    @Test(expected = UnauthorizedExtensionModificationException.class)
    public void storeImage_whenUserIsNotOwnerAndNotAdmin_ShouldThrow() {
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

        //Act
        fileService.create(multipartFile, "name");
    }

}
