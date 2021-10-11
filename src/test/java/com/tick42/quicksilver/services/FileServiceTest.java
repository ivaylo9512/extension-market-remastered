package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.FileFormatException;
import com.tick42.quicksilver.exceptions.UnauthorizedException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.FileRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import javax.persistence.EntityNotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {
    @Mock
    private FileRepository fileRepository;

    private FileServiceImpl fileService;

    private final static String uploadsPath = "./uploads/test";

    @BeforeAll
    private static void setup() throws IOException {
        new java.io.File(uploadsPath + "/logo.txt").createNewFile();
        new java.io.File(uploadsPath + "/logo1.txt").createNewFile();
        new java.io.File(uploadsPath + "/logo3.txt").createNewFile();
    }

    @AfterAll
    private static void reset() throws IOException {
        new java.io.File(uploadsPath + "/logo.txt").delete();
        new java.io.File(uploadsPath + "/logo1.txt").delete();
        new java.io.File(uploadsPath + "/logo2.txt").delete();
        new java.io.File(uploadsPath + "/logo3.txt").delete();
    }

    @BeforeEach
    private void setupEach() throws IOException {
        this.fileService = Mockito.spy(new FileServiceImpl(fileRepository, uploadsPath));
    }

    @Test
    public void increaseCount() {
        File file = new File();

        when(fileRepository.save(file)).thenReturn(file);

        File savedFile = fileService.increaseCount(file);
        assertEquals(savedFile.getDownloadCount(), 1);
    }

    @Test
    public void generate() {
        MockMultipartFile file = new MockMultipartFile(
                "image132",
                "image132.png",
                "image/png",
                "image132".getBytes());

        File savedFile = fileService.generate(file, "logo", "image");

        assertEquals(savedFile.getResourceType(), "logo");
        assertEquals(savedFile.getType(), "image/png");
    }

    @Test
    public void generate_WhenTypeDoesNotMatch_FileFormat() {
        MockMultipartFile file = new MockMultipartFile(
                "text132",
                "text132.txt",
                "text/plain",
                "text132".getBytes());

        FileFormatException thrown = assertThrows(FileFormatException.class,
                () -> fileService.generate(file, "logo", "image"));

        assertEquals(thrown.getMessage(), "File should be of type image");
    }

    @Test
    public void generate_WhenTypeIsNull_FileFormat() {
        MockMultipartFile file = new MockMultipartFile(
                "text132",
                "text132.txt",
                null,
                "text132".getBytes());

        FileFormatException thrown = assertThrows(FileFormatException.class,
                () -> fileService.generate(file, "logo", "image"));

        assertEquals(thrown.getMessage(), "File should be of type image");
    }

    @Test
    public void createAndSave() throws Exception{
        FileInputStream input = new FileInputStream("./uploads/test/logo.txt");
        MultipartFile multipartFile = new MockMultipartFile("test", "logo.txt", "text/plain",
                IOUtils.toByteArray(input));
        input.close();

        fileService.save("logo2", multipartFile);

        assertTrue(new java.io.File("./uploads/test/logo2.txt").exists());
    }

    @Test
    public void delete_WithOwner(){
        UserModel owner = new UserModel();
        owner.setId(1);

        File file = new File();
        file.setOwner(owner);
        file.setExtensionType("txt");
        file.setResourceType("logo");

        fileService.delete(file, owner.getId(), owner);

        assertFalse(new java.io.File("./uploads/test/logo1.txt").exists());
    }

    @Test
    public void delete_NotOwner_NotAdmin(){
        UserModel loggedUser = new UserModel();
        loggedUser.setId(2);
        loggedUser.setRole("ROLE_USER");

        UserModel owner = new UserModel();
        owner.setId(1);

        File file = new File();
        file.setOwner(owner);
        file.setExtensionType("txt");
        file.setResourceType("logo");

        UnauthorizedException thrown = assertThrows(UnauthorizedException.class,
                () -> fileService.delete(file, owner.getId(), loggedUser));

        assertEquals(thrown.getMessage(), "Unauthorized.");
    }

    @Test
    public void delete_WithAdmin(){
        UserModel loggedUser = new UserModel();
        loggedUser.setRole("ROLE_ADMIN");
        loggedUser.setId(1);

        UserModel owner = new UserModel();
        owner.setId(3);

        File file = new File();
        file.setOwner(owner);
        file.setExtensionType("txt");
        file.setResourceType("logo");

        fileService.delete(file, owner.getId(), loggedUser);

        assertFalse(new java.io.File("./uploads/test/logo3.txt").exists());
    }

    @Test
    public void deleteById_WithNotFound(){
        UserModel loggedUser = new UserModel();
        loggedUser.setRole("ROLE_ADMIN");

        when(fileRepository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(
                EntityNotFoundException.class,
                () -> fileService.deleteById(1, loggedUser));

        assertEquals(thrown.getMessage(), "File not found.");
    }

    @Test
    public void deleteById_WithOwner(){
        UserModel loggedUser = new UserModel();
        loggedUser.setRole("ROLE_ADMIN");
        loggedUser.setId(1);

        File file = new File();
        file.setOwner(loggedUser);

        doNothing().when(fileService).delete(file, loggedUser.getId(), loggedUser);
        when(fileRepository.findById(1L)).thenReturn(Optional.of(file));

        fileService.deleteById(1, loggedUser);

        verify(fileService, times(1)).delete(file, loggedUser.getId(), loggedUser);
    }

    @Test
    public void deleteById_WithExtension(){
        UserModel loggedUser = new UserModel();
        loggedUser.setRole("ROLE_ADMIN");
        loggedUser.setId(1);

        Extension extension = new Extension();
        extension.setId(2);

        File file = new File();
        file.setOwner(loggedUser);
        file.setExtension(extension);

        doNothing().when(fileService).delete(file, extension.getId(), loggedUser);
        when(fileRepository.findById(1L)).thenReturn(Optional.of(file));

        fileService.deleteById(1, loggedUser);

        verify(fileService, times(1)).delete(file, extension.getId(), loggedUser);
    }

    @Test
    public void getAsResource() throws MalformedURLException {
        Resource resource = fileService.getAsResource("logo.txt");

        assertEquals(resource.getFilename(), "logo.txt");
    }

    @Test
    public void getAsResource_WhenFileNonexistent(){
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> fileService.getAsResource("nonexistent.txt"));

        assertEquals(thrown.getMessage(), "File not found");
    }

    @Test
    public void findByExtension(){
        Extension extension = new Extension();
        File file = new File();

        when(fileRepository.findByExtension("logo", extension)).thenReturn(Optional.of(file));

        File foundFile = fileService.findByExtension("logo", extension);

        assertEquals(foundFile, file);
    }

    @Test
    public void findByExtensions_WithNotFound(){
        Extension extension = new Extension();

        when(fileRepository.findByExtension("logo", extension)).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> fileService.findByExtension("logo", extension));

        assertEquals(thrown.getMessage(), "File not found.");
    }
}
