package com.tick42.quicksilver.services;

import com.tick42.quicksilver.models.EmailToken;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.EmailTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailTokenServiceTest {
    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private EmailTokenRepository emailTokenRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    @Spy
    private EmailTokenServiceImpl emailTokenService;

    @Test
    public void sendVerificationEmail() throws MessagingException {
        UUID uuid = UUID.randomUUID();
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromContextPath(new MockHttpServletRequest());
        builder.path("port");
        builder.host("localhost");

        UserModel userModel = new UserModel();
        userModel.setEmail("email@gmail.com");
        try (MockedStatic<UUID> UUIDMock = mockStatic(UUID.class);
             MockedStatic<ServletUriComponentsBuilder> servletMock = mockStatic(ServletUriComponentsBuilder.class)) {

            UUIDMock.when(UUID::randomUUID).thenReturn(uuid);
            servletMock.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(builder);
            when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            emailTokenService.sendVerificationEmail(userModel);

            ArgumentCaptor<EmailToken> tokenCaptor = ArgumentCaptor.forClass(EmailToken.class);
            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);

            verify(mimeMessage).setRecipient(eq(Message.RecipientType.TO), addressCaptor.capture());
            verify(emailTokenService).create(tokenCaptor.capture());

            EmailToken passedToCreate = tokenCaptor.getValue();
            Address address = addressCaptor.getValue();

            verify(mimeMessage, times(1)).setContent(String.format("""
                Click the link to activate your account:\s
                <a href="http://localhost/port/api/users/activate/%s">Activate</a>""", uuid), "text/html;charset=utf-8");
            verify(mimeMessage, times(1)).setSubject("Activate account.", "utf-8");

            assertEquals(address.toString(), userModel.getEmail());
            assertEquals(passedToCreate.getToken(), uuid.toString());
            assertEquals(passedToCreate.getUser(), userModel);
        }
    }

    @Test
    public void create(){
        EmailToken emailToken = new EmailToken();
        emailTokenService.create(emailToken);

        verify(emailTokenRepository, times(1)).save(emailToken);
    }

    @Test
    public void findByToken() {
        EmailToken token = new EmailToken();
        token.setToken("token");

        when(emailTokenRepository.findByToken(token.getToken())).thenReturn(Optional.of(token));

        EmailToken foundToken = emailTokenService.findByToken(token.getToken());

        assertEquals(foundToken, token);
    }

    @Test
    public void findByToken_WithNotFound() {
        when(emailTokenRepository.findByToken("token")).thenReturn(Optional.empty());

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class,
                () -> emailTokenService.findByToken("token"));

        assertEquals(thrown.getMessage(), "Incorrect token.");
    }

    @Test
    public void delete(){
        EmailToken token = new EmailToken();

        emailTokenService.delete(token);

        verify(emailTokenRepository, times(1)).delete(token);
    }
}

