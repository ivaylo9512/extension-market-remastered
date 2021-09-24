package integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.config.AppConfig;
import com.tick42.quicksilver.config.SecurityConfig;
import com.tick42.quicksilver.config.TestWebConfig;
import com.tick42.quicksilver.controllers.UserController;
import com.tick42.quicksilver.models.Dtos.FileDto;
import com.tick42.quicksilver.models.Dtos.UserDto;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.NewPasswordSpec;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
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
import java.util.Map;

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
    private static String adminToken, userToken, expiredToken;
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
    public void reset(){
        new File("./uploads/profileImage10.png").delete();
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

        int expiration = Jwt.getJwtExpirationInMs();
        Jwt.setJwtExpirationInMs(-20);

        expiredToken = "Token " + Jwt.generate(new UserDetails(admin, Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        Jwt.setJwtExpirationInMs(expiration);

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

    private RequestBuilder createMediaRegisterRequest(String url, String role, String username, String email, String token, boolean isWithImage) throws IOException {
        MockHttpServletRequestBuilder request = (isWithImage
                ? MockMvcRequestBuilders.multipart(url).file(profileImage)
                : MockMvcRequestBuilders.multipart(url))
                .param("username", username)
                .param("email", email)
                .param("password", user.getPassword())
                .param("country", user.getCountry())
                .param("info", user.getInfo());

        if(token != null){
            request.header("Authorization", token);
        }

        userDto.setRole(role);
        userDto.setId(10);
        userDto.setEmail(email);
        userDto.setUsername(username);
        userDto.setProfileImage(isWithImage
                ? "profileImage10.png"
                : null);

        return request;
    }

    @WithMockUser(value = "spring")
    @Test
    public void register() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/register", "ROLE_USER",
                        "username", "username@gmail.com", null, true))
                .andExpect(status().isOk());

        enableUser(userDto.getId());
        checkDBForUser(userDto, null);
        checkDBForImage("profileImage", userDto.getId());
    }

    @WithMockUser(value = "spring")
    @Test
    public void registerAdmin() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", adminToken, true))
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
                        "username", "username@gmail.com", userToken, true))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Access is denied"));
    }

    @Test
    public void register_WhenUsernameIsTaken() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/register", "ROLE_USER",
                        "testUser", "username@gmail.com", null, true))
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
        UserModel user = new UserModel("adminUser", "adminUser@gmail.com", "password","ROLE_ADMIN", "info",
                "Bulgaria", 4.166666666666667, 3);
        UserDto userDto = new UserDto(user);
        userDto.setId(1);
        userDto.setActive(true);
        userDto.setProfileImage("profileImage1.png");

        mockMvc.perform(post("/api/users/login")
                .contentType("Application/json")
                .content("{\"username\": \"adminUser\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(userDto)));
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
        user.setProfileImage("profileImage1.png");

        checkDBForUser(user, null);
    }

    @Test
    void findById_WithNotActive_WithLoggedUserAdmin() throws Exception {
        UserDto user = new UserDto(new UserModel("testForth", "testForth@gmail.com", "password", "ROLE_USER",
                "info", "Italy", 0, 0));
        user.setId(8);
        user.setActive(false);
        user.setProfileImage("profileImage8.png");

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
    void setActive() throws Exception {
        UserDto user = new UserDto(new UserModel("testForth", "testForth@gmail.com", "password", "ROLE_USER",
                "info", "Italy", 0, 0));
        user.setId(8);
        user.setActive(true);
        user.setProfileImage("profileImage8.png");

        mockMvc.perform(patch("/api/users/auth/setActive/8/true")
                .header("Authorization", adminToken))
                .andExpect(status().isOk());

        checkDBForUser(user, null);

        mockMvc.perform(patch("/api/users/auth/setActive/8/false")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        user.setActive(false);
        checkDBForUser(user, adminToken);
    }

    @Test
    void setActive_WithLoggedUserNotAdmin() throws Exception {
        mockMvc.perform(patch("/api/users/auth/setActive/8/true")
                .header("Authorization", userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changeUserInfo() throws Exception {
        UserSpec userSpec = new UserSpec(1, "newUsername", "newUsername@gmail.com",
                "Bulgaria", "info");
        UserDto userDto = new UserDto(userSpec, "ROLE_ADMIN");
        userDto.setActive(true);
        userDto.setRating(4.166666666666667);
        userDto.setExtensionsRated(3);
        userDto.setProfileImage("profileImage1.png");

        mockMvc.perform(patch("/api/users/auth/changeUserInfo")
                        .header("Authorization", adminToken)
                        .contentType("Application/json")
                        .content(objectMapper.writeValueAsString(userSpec)))
                .andExpect(content().string(objectMapper.writeValueAsString(userDto)));

        checkDBForUser(userDto, null);
    }

    @Test
    public void changeUserInfo_WhenUsernameIsTaken() throws Exception {
        UserSpec userSpec = new UserSpec(1, "testUser", "newUsername@gmail.com", "newCountry", "info");

        mockMvc.perform(patch("/api/users/auth/changeUserInfo")
                        .header("Authorization", adminToken)
                        .contentType("Application/json")
                        .content(objectMapper.writeValueAsString(userSpec)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken."));
    }

    @Test
    public void changeUserInfo_WhenEmailIsTaken() throws Exception {
        UserSpec userSpec = new UserSpec(1, "newUsername", "testUser@gmail.com", "newCountry", "info");

        mockMvc.perform(patch("/api/users/auth/changeUserInfo")
                        .header("Authorization", adminToken)
                        .contentType("Application/json")
                        .content(objectMapper.writeValueAsString(userSpec)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already taken."));
    }

    @Test
    public void changePassword() throws Exception {
        NewPasswordSpec passwordSpec = new NewPasswordSpec("adminUser", "password", "newPassword");
        UserModel user = new UserModel("adminUser", "adminUser@gmail.com", "password","ROLE_ADMIN", "info",
                "Bulgaria", 4.166666666666667, 3);

        UserDto userDto = new UserDto(user);
        userDto.setId(1);
        userDto.setProfileImage("profileImage1.png");

        mockMvc.perform(patch("/api/users/auth/changePassword")
                        .header("Authorization", adminToken)
                        .contentType("Application/json")
                        .content(objectMapper.writeValueAsString(passwordSpec)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/login")
                        .contentType("Application/json")
                        .content("{\"username\": \"adminUser\", \"password\": \"newPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(userDto)));
    }

    @Test
    public void register_WithWrongFileType() throws Exception {
        FileInputStream input = new FileInputStream("./uploads/test.txt");
        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "test.txt", "text/plain",
                IOUtils.toByteArray(input));
        input.close();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/api/users/register")
                .file(profileImage)
                .param("username", "username")
                .param("email", "email@gmail.com")
                .param("password", user.getPassword())
                .param("country", user.getCountry())
                .param("info", user.getInfo());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File should be of type image"));
    }

    @Test
    public void register_WithoutProfileImage() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/register", "ROLE_USER",
                        "username", "username@gmail.com", null, false))
                .andExpect(status().isOk());

        enableUser(userDto.getId());
        checkDBForUser(userDto, adminToken);
    }

    @Test
    public void registerAdmin_WithoutProfileImage() throws Exception {
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", adminToken, false))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(objectMapper.writeValueAsString(userDto))));

        checkDBForUser(userDto, adminToken);
    }

    @Test
    public void register_WithWrongFields() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/api/users/register")
                .param("password", "short")
                .param("username", "short");

        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> errors = objectMapper.readValue(response, new TypeReference<>() {});

        assertEquals(errors.get("username"), "Username must be between 8 and 20 characters.");
        assertEquals(errors.get("password"), "Password must be between 10 and 25 characters.");
        assertEquals(errors.get("email"), "You must provide email.");
        assertEquals(errors.get("country"), "You must provide country.");
        assertEquals(errors.get("info"), "You must provide info.");
    }

    @Test
    void changeUserInfo_WithWrongFields() throws Exception {
        String response = mockMvc.perform(patch("/api/users/auth/changeUserInfo")
                        .content("{\"username\": \"short\", \"email\": \"incorrect\"}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", adminToken))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> errors = objectMapper.readValue(response, new TypeReference<>() {});

        assertEquals(errors.get("username"), "Username must be between 8 and 20 characters.");
        assertEquals(errors.get("email"), "Must be a valid email.");
        assertEquals(errors.get("country"), "You must provide country.");
        assertEquals(errors.get("info"), "You must provide info.");
    }

    @Test
    public void changePassword_WithWrongFields() throws Exception {
        String response = mockMvc.perform(patch("/api/users/auth/changePassword")
                        .content("{\"newPassword\": \"short\"}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", adminToken))
                .andExpect(status().isUnprocessableEntity())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> errors = objectMapper.readValue(response, new TypeReference<>() {});

        assertEquals(errors.get("newPassword"), "Password must be between 10 and 25 characters.");
        assertEquals(errors.get("currentPassword"), "You must provide current password.");
        assertEquals(errors.get("username"), "You must provide username.");
    }

    @Test
    void registerAdmin_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", null, true))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void registerAdmin_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", "Token incorrect", true))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void registerAdmin_WithExpiredToken() throws Exception{
        mockMvc.perform(createMediaRegisterRequest("/api/users/auth/registerAdmin", "ROLE_ADMIN",
                        "username", "username@gmail.com", expiredToken, true))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token has expired."));
    }

    @Test
    void changeUserInfo_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(patch("/api/users/auth/changeUserInfo"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void changeUserInfo_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(patch("/api/users/auth/changeUserInfo")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void changeUserInfo_WithTokenWithoutPrefix() throws Exception{
        mockMvc.perform(patch("/api/users/auth/changeUserInfo")
                        .header("Authorization", "Incorrect token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
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
        mockMvc.perform(patch("/api/users/auth/changePassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void changePassword_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(patch("/api/users/auth/changePassword")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }

    @Test
    void setActive_WithoutToken_Unauthorized() throws Exception{
        mockMvc.perform(patch("/api/users/auth/setActive"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is missing"));
    }

    @Test
    void setActive_WithIncorrectToken_Unauthorized() throws Exception{
        mockMvc.perform(patch("/api/users/auth/setActive")
                        .header("Authorization", "Token incorrect"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Jwt token is incorrect"));
    }
}
