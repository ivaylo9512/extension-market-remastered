package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.InvalidInputException;
import com.tick42.quicksilver.models.EmailToken;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.EmailTokenRepository;
import com.tick42.quicksilver.services.base.EmailTokenService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

@Service
public class EmailTokenServiceImpl implements EmailTokenService {
    private final EmailTokenRepository tokenRepository;
    private final JavaMailSender javaMailSender;

    public EmailTokenServiceImpl(EmailTokenRepository tokenRepository, JavaMailSender javaMailSender) {
        this.tokenRepository = tokenRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void create(EmailToken token) {
        tokenRepository.save(token);
    }

    @Override
    public EmailToken findByToken(String token) {
        return tokenRepository.findByToken(token).orElseThrow(() ->
                new InvalidInputException("Incorrect token."));
    }

    @Override
    public void delete(EmailToken token){
        tokenRepository.delete(token);
    }

    @Override
    public void sendVerificationEmail(UserModel user) throws MessagingException {
        String token = UUID.randomUUID().toString();
        create(new EmailToken(token, user));

        String subject = "Activate account.";
        String content = String.format("""
                Click the link to activate your account:\s
                <a href="%s">Activate</a>""", ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/api/users/activate/" + token);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "utf-8");

        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(content, true);

        javaMailSender.send(mimeMessage);
    }
}
