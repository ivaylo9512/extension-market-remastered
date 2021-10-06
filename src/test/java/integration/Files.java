package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.FileController;
import com.tick42.quicksilver.models.Dtos.FileDto;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.security.Jwt;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = { AppConfig.class, TestWebConfig.class, SecurityConfig.class })
@WebAppConfiguration(value = "src/main/java/com/tick42/quicksilver")
@WebMvcTest(FileController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@Transactional
public class Files {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DataSource dataSource;

    private MockMvc mockMvc;
    private static String adminToken, userToken;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setupData() {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/FilesData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/UsersData.sql"));
        rdp.execute(dataSource);
    }

    @AfterEach
    public void resetState() throws IOException {
        java.nio.file.Files.copy(Paths.get("./uploads/test.png"), Paths.get("./uploads/test3.png"), StandardCopyOption.REPLACE_EXISTING);
    }

    @BeforeAll
    public void setup() {
        UserModel admin = new UserModel("adminUser", "password", "ROLE_ADMIN");
        admin.setId(1);

        UserModel user = new UserModel("testUser", "password", "ROLE_USER");
        user.setId(3);

        adminToken = "Token " + Jwt.generate(new UserDetails(admin, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        userToken = "Token " + Jwt.generate(new UserDetails(user, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_USER"))));

        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void assertConfig_assertFileController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("fileController"));
    }

    @Test
    public void download() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/files/download/profileImage1.png"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertEquals(response.getHeader(HttpHeaders.CONTENT_DISPOSITION), "attachment; filename=\"profileImage1.png\"");
        assertEquals(response.getContentType(), "image/png");
    }

    @Test
    public void findByType() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/files/findByOwner/profileImage/1"))
                .andExpect(status().isOk())
                .andReturn();

        FileDto image = objectMapper.readValue(result.getResponse().getContentAsString(), FileDto.class);

        assertEquals(image.getResourceType(), "profileImage");
        assertEquals(image.getExtensionType(), "png");
        assertEquals(image.getOwnerId(), 1);
        assertEquals(image.getType(), "image/png");
        assertEquals(image.getSize(), 43250.0);
    }

    @Test
    public void findByTypeWithNonExistent() throws Exception {
        mockMvc.perform(get("/api/files/findByOwner/nonexistent/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("File not found."));
    }

    @Test
    public void deleteFile() throws Exception{
        mockMvc.perform(delete("/api/files/auth/delete/test/3")
                .header("Authorization", userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/files/findByOwner/test/3"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("File not found."));

        assertFalse(new File("test3.png").exists());
    }

    @Test
    public void deleteFileWithNonExistent() throws Exception{
        mockMvc.perform(delete("/api/files/auth/delete/nonexistent/3")
                .header("Authorization", userToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("File not found."));
    }

    @Test
    public void deleteFileWithUserThatIsNotOwner() throws Exception{
        mockMvc.perform(delete("/api/files/auth/delete/profileImage/1")
                        .header("Authorization", userToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Unauthorized"));
    }

    @Test
    public void deleteFileWithUserThatIsNotOwnerAndIsRoleAdmin() throws Exception{
        mockMvc.perform(delete("/api/files/auth/delete/test/3")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/files/findByOwner/test/3"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("File not found."));

        assertFalse(new File("test3.png").exists());
    }
}