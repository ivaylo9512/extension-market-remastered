package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.ExtensionController;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.security.Jwt;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = { AppConfig.class, TestWebConfig.class, SecurityConfig.class })
@WebAppConfiguration(value = "src/main/java/com/chat/app")
@WebMvcTest(ExtensionController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@Transactional
public class Extensions {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DataSource dataSource;

    private MockMvc mockMvc;
    private static String adminToken, userToken;
    private ObjectMapper objectMapper;

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
