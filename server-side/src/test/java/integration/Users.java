package integration;

import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.UserController;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.security.Jwt;
import com.tick42.quicksilver.services.base.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.ServletContext;
import java.util.Collections;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { AppConfig.class, TestWebConfig.class, SecurityConfig.class})
@WebAppConfiguration(value = "src/main/java/com/chat/app")
@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
public class Users {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserService userService;

    private MockMvc mockMvc;

    public static UserModel userDetails;
    public static String adminToken;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @BeforeAll
    public void setupUser() {
        userDetails = userService.create(new UserModel("admin123", "admin", "ROLE_ADMIN"));
        adminToken = "Token " + Jwt.generate(new UserDetails(userDetails, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    public void assertConfig_assertUserController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        Assert.assertNotNull(servletContext);
        Assert.assertTrue(servletContext instanceof MockServletContext);
        Assert.assertNotNull(webApplicationContext.getBean("userController"));
    }
}
