package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.FileFormatException;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.repositories.base.FileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileServiceImplTests {
    @Mock
    private FileRepository fileRepository;

    @Spy
    @InjectMocks
    private FileServiceImpl fileService;

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

        File savedFile = fileService.generate(file, "savedName");

        assertEquals(savedFile.getName(), "savedName.png");
        assertEquals(savedFile.getType(), "image/png");
    }

    @Test
    public void create_when() {
        MockMultipartFile file = new MockMultipartFile(
                "text132",
                "text132.txt",
                "text",
                "text132".getBytes());

        FileFormatException thrown = assertThrows(FileFormatException.class,
                () -> fileService.create(file, "savedName", "image"));

        assertEquals(thrown.getMessage(), "File should be of type image");
    }

    @Test
    public void create() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "image132",
                "image132.png",
                "image/png",
                "image132".getBytes());

        File file1 = new File();
        doNothing().when(fileService).save(file1, file);

        File savedFile = fileService.create(file, "savedName", "image");

        assertEquals(savedFile.getName(), "savedName.png");
        assertEquals(savedFile.getType(), "image/png");
    }

    @Test
    public void find() throws Exception{
        fileService.getAsResource("test.txt");
    }
}
