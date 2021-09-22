package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.RatingController;
import com.tick42.quicksilver.models.Extension;
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
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = { AppConfig.class, TestWebConfig.class, SecurityConfig.class })
@WebAppConfiguration(value = "src/main/java/com/tick42/quicksilver")
@WebMvcTest(RatingController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@Transactional
public class Ratings {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DataSource dataSource;

    private MockMvc mockMvc;
    private static String adminToken;

    @BeforeEach
    public void setupData() {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/ExtensionsData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/UsersData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/RatingsData.sql"));
        rdp.execute(dataSource);
    }

    @BeforeAll
    public void setup() {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/FilesData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/GitHubData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/SettingsData.sql"));
        rdp.execute(dataSource);

        UserModel admin = new UserModel("adminUser", "password", "ROLE_ADMIN");
        admin.setId(1);

        adminToken = "Token " + Jwt.generate(new UserDetails(admin, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void assertConfig_assertUserController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("userController"));
    }

    @Test
    public void userRatingForExtension() throws Exception {
        mockMvc.perform(get("/api/rating/auth/userRating/2")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    public void rate() throws Exception {
        mockMvc.perform(patch("/api/rating/auth/rate/2/3")
                .header("Authorization", adminToken))
                .andExpect(content().string("3.0"));
    }

    @Test
    void rate_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(get("/api/rating/auth/rate/2/3"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void rate_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(get("/api/rating/auth/rate/2/3")
                .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void userRatingForExtension_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(get("/api/rating/auth/userRating/2"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void userRatingForExtension_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(get("/api/rating/auth/userRating/2")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }
}
