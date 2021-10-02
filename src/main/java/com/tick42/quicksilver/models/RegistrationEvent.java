package com.tick42.quicksilver.models;

import org.springframework.context.ApplicationEvent;
import java.util.Locale;

public class RegistrationEvent extends ApplicationEvent {
    private String appUrl;
    private Locale locale;
    private UserModel user;

    public RegistrationEvent(UserModel user, Locale locale, String appUrl) {
        super(user);

        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}
