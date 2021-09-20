package com.tick42.quicksilver.models.specs;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

public class NewPasswordSpec {
    @NotNull(message = "You must provide username.")
    private String username;

    @NotNull(message = ("You must provide current password."))
    private String currentPassword;

    @NotNull(message = ("New password is required"))
    @Length(min = 10, max = 25, message = ("Password must be between 10 and 25 characters."))
    private String newPassword;

    public NewPasswordSpec(){
    }

    public NewPasswordSpec(String username, String currentPassword, String newPassword) {
        this.username = username;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
