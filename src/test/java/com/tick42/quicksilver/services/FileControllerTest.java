package com.tick42.quicksilver.services;

import com.tick42.quicksilver.controllers.FileController;
import com.tick42.quicksilver.models.Dtos.FileDto;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileControllerTest {
    @InjectMocks
    FileController fileController;

    @Mock
    FileServiceImpl fileService;

    @Mock
    UserServiceImpl userService;

    @Mock
    ExtensionServiceImpl extensionService;

    @Mock
    MockHttpServletRequest request;
    @Mock
    ServletContext servletContext;

    private final UserModel userModel = new UserModel(1, "username", "email", "password", "ROLE_ADMIN", "info", "Bulgaria");
    private final UserDetails user = new UserDetails(userModel, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    private final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getId());
    private final File file = new File("profileImage", 32_000, "image/png", "png", userModel);

    @Test
    public void delete(){
        UserModel owner = new UserModel();
        owner.setId(2);

        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);


        when(userService.getById(2L)).thenReturn(owner);
        when(userService.findById(1L, user)).thenReturn(userModel);
        when(fileService.delete("profileImage", owner, userModel)).thenReturn(true);

        fileController.delete("profileImage", 2L);

        verify(fileService, times(1)).delete("profileImage", owner, userModel);
    }

    @Test
    public void findByType(){
        when(userService.getById(1L)).thenReturn(userModel);
        when(fileService.findByOwner("profileImage", userModel)).thenReturn(file);

        FileDto fileDto = fileController.findByOwner("profileImage", 1L);

        assertEquals(fileDto.getOwnerId(), userModel.getId());
        assertEquals(fileDto.getResourceType(), file.getResourceType());
        assertEquals(fileDto.getSize(), file.getSize());
        assertEquals(fileDto.getType(), file.getType());
        assertEquals(fileDto.getExtensionType(), file.getExtensionType());
    }

    @Test
    public void getAsResource() throws IOException {
        Extension extension = new Extension();
        file.setExtension(extension);

        Resource resource = new UrlResource("file", "/file1.txt");

        when(fileService.getAsResource("file1.txt")).thenReturn(resource);
        when(extensionService.getById(1L)).thenReturn(extension);
        when(fileService.findByExtension("file", extension)).thenReturn(file);
        when(fileService.increaseCount(file)).thenReturn(file);
        doNothing().when(extensionService).reloadFile(file);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getMimeType(resource.getFile().getAbsolutePath())).thenReturn("plain/text");

        ResponseEntity<Resource> response = fileController.getAsResource("file1.txt", request);

        assertEquals(response.getBody(), resource);
        assertEquals(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION), "attachment; filename=\"file1.txt\"");
        verify(fileService, times(1)).increaseCount(file);
        verify(fileService, times(1)).findByExtension("file", extension);
        verify(extensionService, times(1)).reloadFile(file);
    }
}