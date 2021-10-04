package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.EmailToken;
import com.tick42.quicksilver.models.UserModel;
import javax.mail.MessagingException;

public interface EmailTokenService {
    void createVerificationToken(UserModel user, String token);

    EmailToken getToken(String token);

    void delete(EmailToken token);

    void sendVerificationEmail(UserModel user) throws MessagingException;
}
