package integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.ExtensionController;
import com.tick42.quicksilver.models.*;
import com.tick42.quicksilver.models.Dtos.ExtensionDto;
import com.tick42.quicksilver.models.Dtos.GitHubDto;
import com.tick42.quicksilver.models.Dtos.HomePageDto;
import com.tick42.quicksilver.models.Tag;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.ExtensionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = { AppConfig.class, TestWebConfig.class, SecurityConfig.class })
@WebAppConfiguration(value = "src/main/java/com/tick42/quicksilver")
@WebMvcTest(ExtensionController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class Extensions {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ExtensionService extensionService;

    private MockMvc mockMvc;
    private static String adminToken, userToken;
    private ObjectMapper objectMapper;

    private ExtensionDto extensionDto;

    @BeforeEach
    public void setupData() {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/ExtensionsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/ExtensionTagsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/TagsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/UsersData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/SettingsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/RatingsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/GitHubData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/FilesData.sql"));
        rdp.execute(dataSource);

        extensionService.setFeaturedLimit(5);
        extensionService.updateMostRecent();
        extensionService.loadFeatured();
        extensionService.setFeatured(2, false);
    }

    @BeforeAll
    public void setup() {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/ExtensionTagsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/TagsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/ExtensionsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/UsersData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/SettingsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/RatingsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/GitHubData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/FilesData.sql"));
        rdp.execute(dataSource);

        UserModel admin = new UserModel("adminUser", "password", "ROLE_ADMIN");
        admin.setId(1);

        UserModel user = new UserModel("testUser", "password", "ROLE_USER");
        user.setId(3);

        adminToken = "Token " + Jwt.generate(new UserDetails(admin, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        userToken = "Token " + Jwt.generate(new UserDetails(user, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_USER"))));

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


        Extension extension = new Extension(1, "Extension Market", "Extension market application.", "1", tags, github, admin);
        extension.setRating(5);
        extension.setTimesRated(1);
        extension.setUploadDate(LocalDateTime.of(2021, Month.FEBRUARY, 1, 22, 32, 46));
        extension.setFile(file);
        extension.setImage(image);
        extension.setCover(cover);
        extension.setPending(false);
        extension.setFeatured(true);

        extensionDto = new ExtensionDto(extension);
        extensionDto.setTags(List.of("app", "c", "auto", "repo"));

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void findById() throws Exception {
        mockMvc.perform(get("/api/extensions/findById/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(extensionDto)));
    }

    @Test
    public void findById_WithNotFound() throws Exception {
        mockMvc.perform(get("/api/extensions/findById/222"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Extension not found."));
    }

    @Test
    public void findById_WithPending_WithoutLoggedUser() throws Exception {
        mockMvc.perform(get("/api/extensions/findById/4"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Extension is not available."));
    }

    @Test
    public void findById_WithPending_WithLoggedUser_Owner() throws Exception {
        String response = mockMvc.perform(get("/api/extensions/findById/3")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExtensionDto extension = objectMapper.readValue(response, ExtensionDto.class);

        assertEquals(extension.getId(), 3);
    }

    @Test
    public void findById_WithPending_WithLoggedUser_NotOwner_NotAdmin() throws Exception {
        mockMvc.perform(get("/api/extensions/findById/4")
                        .header("Authorization", userToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Extension is not available."));
    }

    @Test
    public void findById_WithPending_WithLoggedUser_NotOwner_Admin() throws Exception {
        String response = mockMvc.perform(get("/api/extensions/findById/3")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExtensionDto extension = objectMapper.readValue(response, ExtensionDto.class);

        assertEquals(extension.getId(), 3);
        assertEquals(extension.getCurrentUserRatingValue(), 5);
    }

    @Test
    public void setPendingTrue_AndCheckUpdatedHomePage() throws Exception {
        mockMvc.perform(patch("/api/extensions/auth/setPending/1/true")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/api/extensions/findById/1")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExtensionDto extension = objectMapper.readValue(response, ExtensionDto.class);
        assertTrue(extension.isPending());

        HomePageDto homePage = homeExtensionsRequest();
        assertFalse(homePage.getFeatured().contains(extension));
        assertFalse(homePage.getMostDownloaded().contains(extension));
        assertFalse(homePage.getMostRecent().contains(extension));
    }


    @Test
    public void setPendingFalse_AndCheckUpdatedHomePage() throws Exception {
        mockMvc.perform(patch("/api/extensions/auth/setPending/3/false")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        String response = mockMvc.perform(get("/api/extensions/findById/3")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExtensionDto extension = objectMapper.readValue(response, ExtensionDto.class);
        assertFalse(extension.isPending());

        HomePageDto homePage = homeExtensionsRequest();
        assertExtensions(homePage.getMostDownloaded().get(0), extension);
        assertExtensions(homePage.getMostRecent().get(0), extension);
    }

    @Test
    public void setPending_WithUserNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/extensions/auth/setPending/1/false")
                .header("Authorization", userToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Access is denied"));
    }

    private HomePageDto homeExtensionsRequest() throws Exception {
        String response = mockMvc.perform(get("/api/extensions/findHomeExtensions/4/4"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(response);

        return objectMapper.readValue(response, HomePageDto.class);
    }

    private void assertExtensions(ExtensionDto extension, ExtensionDto extension1){
        assertEquals(extension.getId(), extension1.getId());
        assertEquals(extension.getCoverName(), extension1.getCoverName());
        assertEquals(extension.getImageName(), extension1.getImageName());
        assertEquals(extension.getFileName(), extension1.getFileName());
        assertEquals(extension.getDescription(), extension1.getDescription());
        assertEquals(extension.getTags(), extension1.getTags());
        assertEquals(extension.getUploadDate(), extension1.getUploadDate());
        assertEquals(extension.getName(), extension1.getName());
        assertEquals(extension.getRating(), extension1.getRating());
        assertEquals(extension.getTimesRated(), extension1.getTimesRated());
        assertEquals(extension.getTimesDownloaded(), extension1.getTimesDownloaded());
        assertEquals(extension.getOwnerId(), extension1.getOwnerId());
        assertEquals(extension.getOwnerName(), extension1.getOwnerName());
        assertEquals(extension.getVersion(), extension1.getVersion());

        GitHubDto gitHub = extension.getGithub();
        assertEquals(gitHub.getId(), extension1.getGithub().getId());
        assertEquals(gitHub.getUser(), extension1.getGithub().getUser());
        assertEquals(gitHub.getRepo(), extension1.getGithub().getRepo());

    }

    @Test
    public void assertConfig_assertExtensionController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("extensionController"));
    }


    @Test
    void create_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(post("/api/extensions/auth/create"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void create_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(post("/api/extensions/auth/create")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void edit_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(post("/api/extensions/auth/edit"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void edit_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(post("/api/extensions/auth/edit")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

}
