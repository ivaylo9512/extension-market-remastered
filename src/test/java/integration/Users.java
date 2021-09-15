package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.UserController;
import com.tick42.quicksilver.models.Dtos.FileDto;
import com.tick42.quicksilver.models.Dtos.UserDto;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.UserSpec;
import com.tick42.quicksilver.security.Jwt;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = { AppConfig.class, TestWebConfig.class, SecurityConfig.class })
@WebAppConfiguration(value = "src/main/java/com/tick42/quicksilver")
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@Transactional
public class Users {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DataSource dataSource;

    private MockMvc mockMvc;
    private static String adminToken, userToken;
    private ObjectMapper objectMapper;
    private MockMultipartFile profileImage;

    @BeforeEach
    public void setupData() {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/UsersData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/FilesData.sql"));
        rdp.execute(dataSource);
    }

    @AfterEach
    public void reset() {
        new File("./uploads/logo.png").delete();
    }

    @BeforeAll
    public void setup() throws IOException {
        ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
        rdp.addScript(new ClassPathResource("integrationTestsSql/UsersData.sql"));
        rdp.addScript(new ClassPathResource("integrationTestsSql/SettingsData.sql"));
        rdp.execute(dataSource);

        FileInputStream input = new FileInputStream("./uploads/test.png");
        profileImage = new MockMultipartFile("profileImage", "test.png", "image/png",
                IOUtils.toByteArray(input));
        input.close();

        UserModel admin = new UserModel("adminUser", "password", "ROLE_ADMIN");
        admin.setId(1);

        UserModel user = new UserModel("testUser", "password", "ROLE_USER");
        user.setId(2);

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
    public void assertConfig_assertUserController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("userController"));
    }

    private UserModel user = new UserModel("username", "email@gmail.com", "password1234","ROLE_USER", "info", "Bulgaria");
    private UserDto userDto = new UserDto(user);

    private RequestBuilder createMediaRegisterRequest(String url, String role, String username, String email, String token) throws IOException {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(url)
                .file(profileImage)
                .param("username", username)
                .param("email", email)
                .param("password", user.getPassword())
                .param("country", user.getCountry())
                .param("info", user.getInfo());

        if(token != null){
            request.header("Authorization", token);
        }

        userDto.setRole(role);
        userDto.setProfileImage("profileImage10.png");
        userDto.setId(10);
        userDto.setEmail(email);
        userDto.setUsername(username);

        return  request;
    }

    @WithMockUser(value = "spring")
    @Test
    public void register() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(createMediaRegisterRequest("/api/users/register", "ROLE_USER",
                        "username", "username@gmail.com", null))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String token = response.getHeader("Authorization");

        assertNotNull(token);
        enableUser(userDto.getId());
        checkDBForUser(userDto, null);
        checkDBForImage("profileImage", userDto.getId());
    }

    @WithMockUser(value = "spring")
    @Test
    public void registerAdmin() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(objectMapper.writeValueAsString(userDto))));

        enableUser(userDto.getId());
        checkDBForUser(userDto, null);
        checkDBForImage("profileImage", userDto.getId());
    }

    @WithMockUser(value = "spring")
    @Test
    public void registerAdmin_WithUserThatIsNotAdmin_Unauthorized() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", userToken))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Access is denied"));
    }

    @Test
    public void register_WhenUsernameIsTaken() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/register", "ROLE_USER",
                        "testUser", "username@gmail.com", null))
                .andExpect(content().string(containsString("Username is already taken.")));
    }

    private void checkDBForUser(UserDto user, String token) throws Exception{
        MockHttpServletRequestBuilder request = get("/api/users/findById/" + user.getId());
        if(token != null){
            request.header("Authorization", token);
        }

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content()
                .string(objectMapper.writeValueAsString(user)));
    }

    private void checkDBForImage(String resourceType, long userId) throws Exception{
        MvcResult result = mockMvc.perform(get(String.format("/api/files/findByType/%s/%s", resourceType, userId)))
                .andExpect(status().isOk())
                .andReturn();

        FileDto image = objectMapper.readValue(result.getResponse().getContentAsString(), FileDto.class);

        assertEquals(image.getResourceType(), "profileImage");
        assertEquals(image.getExtensionType(), "png");
        assertEquals(image.getOwnerId(), userId);
        assertEquals(image.getType(), "image/png");
        assertEquals(image.getSize(), 66680.0);
    }

    private void enableUser(long id) throws Exception{
        mockMvc.perform(patch("/api/users/auth/setEnabled/true/" + id)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    public void login() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType("Application/json")
                        .content("{\"username\": \"adminUser\", \"password\": \"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void login_WithWrongPassword_ShouldThrow() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType("Application/json")
                        .content("{\"username\": \"username\", \"password\": \"incorrect\"}"))
                .andExpect(status().is(401))
                .andExpect(content().string(containsString("Invalid username or password.")));
    }

    @Test
    public void login_WithWrongUsername_ShouldThrow() throws Exception {
        mockMvc.perform(post("/api/users/login")
                        .contentType("Application/json")
                        .content("{\"username\": \"incorrect\", \"password\": \"password\"}"))
                .andExpect(status().is(401))
                .andExpect(content().string(containsString("Invalid username or password.")));
    }

    @Test
    void findById() throws Exception {
        UserDto user = new UserDto(new UserModel("adminUser", "adminUser@gmail.com", "password", "ROLE_ADMIN",
                "info", "Bulgaria", 4.166666666666667, 3));
        user.setId(1);

        checkDBForUser(user, null);
    }

    @Test
    void findById_WithNotActive_WithLoggedUserAdmin() throws Exception {
        UserDto user = new UserDto(new UserModel("testForth", "testForth@gmail.com", "password", "ROLE_USER",
                "info", "Italy", 0, 0));
        user.setId(8);
        user.setIsActive(false);

        checkDBForUser(user, adminToken);
    }

    @Test
    void findById_WithNotActive_WithLoggedUserNotAdmin() throws Exception {
        mockMvc.perform(get("/api/users/findById/8"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("User is unavailable."));
    }

    @Test
    void findById_WithNonExistentId() throws Exception {
        mockMvc.perform(get("/api/users/findById/222"))
                .andExpect(status().isNotFound());
    }

    @Test
    void setState() throws Exception {
        UserDto user = new UserDto(new UserModel("testForth", "testForth@gmail.com", "password", "ROLE_USER",
                "info", "Italy", 0, 0));
        user.setId(8);
        user.setIsActive(true);

        mockMvc.perform(patch("/api/users/auth/setState/8/enable")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());

        checkDBForUser(user, null);

        mockMvc.perform(patch("/api/users/auth/setState/8/block")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        user.setIsActive(false);
        checkDBForUser(user, adminToken);
    }

    @Test
    void setState_WithLoggedUserNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/users/auth/setState/8/true")
                .header("Authorization", userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changeUserInfo() throws Exception {
        UserSpec userSpec = new UserSpec(1, "newUsername", "newUsername@gmail.com",
                "Bulgaria", "info");
        UserDto userDto = new UserDto(userSpec, "ROLE_ADMIN");
        userDto.setIsActive(true);
        userDto.setRating(4.166666666666667);
        userDto.setExtensionsRated(3);

        mockMvc.perform(post("/api/users/auth/changeUserInfo")
                        .header("Authorization", adminToken)
                        .contentType("Application/json")
                        .content(objectMapper.writeValueAsString(userSpec)))
                .andExpect(content().string(objectMapper.writeValueAsString(userDto)));

        checkDBForUser(userDto, null);
    }

    @Test
    void registerAdmin_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", null))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void registerAdmin_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void changeUserInfo_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(post("/api/users/auth/changeUserInfo"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void changeUserInfo_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(post("/api/users/auth/changeUserInfo")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void searchForUsers_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(get("/api/users/auth/searchForUsers/2"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void searchForUsers_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(get("/api/users/auth/searchForUsers/2")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void changePassword_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(post("/api/users/auth/changePassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void changePassword_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(get("/api/users/auth/changePassword")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }
}
