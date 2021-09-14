package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.GitHubController;
import com.tick42.quicksilver.models.Dtos.GitHubDto;
import com.tick42.quicksilver.models.Dtos.SettingsDto;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.SettingsRepository;
import com.tick42.quicksilver.security.Jwt;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
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
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = { AppConfig.class, TestWebConfig.class, SecurityConfig.class })
@WebAppConfiguration(value = "src/main/java/com/tick42/quicksilver")
@WebMvcTest(GitHubController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@Transactional
public class GitHub {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SettingsRepository settingsRepository;

    private MockMvc mockMvc;
    private static String adminToken, userToken;
    private String githubToken;

    @BeforeEach
    public void setupData() {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/ExtensionsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/GitHubData.sql"));
        rdp.execute(dataSource);
    }

    @BeforeAll
    public void setup() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("/adminUser.txt"));
        githubToken = br.readLine();


        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/UsersData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/SettingsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/GitHubData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/FilesData.sql"));
        rdp.execute(dataSource);

        setOAuthTokenFromFile();
        createJwtTokens();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private void setOAuthTokenFromFile() {
        settingsRepository.findAll().forEach(settings -> {
            settings.setToken(githubToken);
            settingsRepository.save(settings);
        });
    }

    private void createJwtTokens() {
        UserModel admin = new UserModel("adminUser", "password", "ROLE_ADMIN");
        admin.setId(1);

        UserModel user = new UserModel("testUser", "password", "ROLE_USER");
        user.setId(2);

        adminToken = "Token " + Jwt.generate(new UserDetails(admin, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        userToken = "Token " + Jwt.generate(new UserDetails(user, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
    }


    @Test
    public void assertConfig_assertGitHubController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("gitHubController"));
    }

    @Test
    public void reloadGitHub() throws Exception {
        String response = mockMvc.perform(patch("/api/github/auth/reload/1")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        GitHubDto gitHub = objectMapper.readValue(response, GitHubDto.class);

        LocalDateTime time = LocalDateTime.of(2021, Month.SEPTEMBER, 9, 0, 0);
        assertTrue(LocalDateTime.parse(gitHub.getLastCommit()).isAfter(time));
    }

    @Test
    public void reloadGitHub_WithUserThatIsNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/github/auth/reload/1")
                        .header("Authorization", userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getSettings() throws Exception {
        SettingsDto settings = settingsRequest();

        assertEquals(settings.getRate(), 50_0000);
        assertEquals(settings.getWait(), 9000);
        assertEquals(settings.getUsername(), "ivaylo9512");
    }

    private SettingsDto settingsRequest() throws Exception {
        String response = mockMvc.perform(get("/api/github/auth/getSettings")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, SettingsDto.class);
    }

    @Test
    public void getSettings_WithUserThatIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/github/auth/getSettings")
                        .header("Authorization", userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void setSettings() throws Exception {
        mockMvc.perform(post("/api/github/auth/setSettings")
                .header("Authorization", adminToken)
                .content(String.format("{\"username\":\"newUsername\", \"token\":\"%s\",\"rate\":\"700000\",\"wait\":\"700000\"}", githubToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        SettingsDto settings = settingsRequest();

        assertEquals(settings.getRate(), 70_0000);
        assertEquals(settings.getWait(), 70_0000);
        assertEquals(settings.getUsername(), "newUsername");
    }

    @Test
    public void setSettings_WithUserThatIsNotAdmin() throws Exception {
        mockMvc.perform(post("/api/github/auth/setSettings")
                .header("Authorization", userToken)
                .content(String.format("{\"username\":\"newUsername\", \"token\":\"%s\",\"rate\":\"700000\",\"wait\":\"700000\"}", githubToken))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void setNextSettings_AfterFinalReturnToFirst() throws Exception {
        SettingsDto settings = nextSettingsRequest();
        assertEquals(settings.getId(), 3);

        settings = nextSettingsRequest();
        assertEquals(settings.getId(), 5);

        settings = nextSettingsRequest();
        assertEquals(settings.getId(), 7);

        settings = nextSettingsRequest();
        assertEquals(settings.getId(), 1);
    }

    private SettingsDto nextSettingsRequest() throws Exception {
        String result = mockMvc.perform(patch("/api/github/auth/setNextSettings")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().
                getContentAsString();

        return objectMapper.readValue(result, SettingsDto.class);
    }

    @Test
    public void setNextSettings_WithUserThatIsNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/github/auth/setNextSettings")
                .header("Authorization", userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteSettings() throws Exception {
        mockMvc.perform(delete("/api/github/auth/delete/3")
                .header("Authorization", adminToken))
                .andExpect(content().string(""));
    }

    @Test
    public void delete_WithNonExistent() throws Exception {
        mockMvc.perform(delete("/api/github/auth/delete/2")
                .header("Authorization", adminToken))
                .andExpect(status().isNotFound())
                .andExpect(content().string("GitHub not found."));
    }

    @Test
    public void delete_WithMasterAdmin() throws Exception {
        mockMvc.perform(delete("/api/github/auth/delete/1")
                .header("Authorization", adminToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Deleting master admin is not allowed."));
    }

    @Test
    public void delete_WithUserThatIsNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/github/auth/delete/2")
                .header("Authorization", userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_WithoutToken() throws Exception{
        mockMvc.perform(delete("/api/github/auth/delete/2"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void delete_WithIncorrectToken() throws Exception{
        mockMvc.perform(delete("/api/github/auth/delete/2")
                .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void setNextSettings_WithoutToken() throws Exception{
        mockMvc.perform(patch("/api/github/auth/setNextSettings"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void setNextSettings_WithIncorrectToken() throws Exception{
        mockMvc.perform(patch("/api/github/auth/setNextSettings")
                .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void setSettings_WithoutToken() throws Exception{
        mockMvc.perform(post("/api/github/auth/setSettings"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void setSettings_WithIncorrectToken() throws Exception{
        mockMvc.perform(post("/api/github/auth/setSettings")
                .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void reload_WithoutToken() throws Exception{
        mockMvc.perform(patch("/api/github/auth/reload/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void reload_WithIncorrectToken() throws Exception{
        mockMvc.perform(patch("/api/github/auth/reload/1")
                .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void getSettings_WithoutToken() throws Exception{
        mockMvc.perform(get("/api/github/auth/getSettings"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void getSettings_WithIncorrectToken() throws Exception{
        mockMvc.perform(get("/api/github/auth/getSettings")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }
}