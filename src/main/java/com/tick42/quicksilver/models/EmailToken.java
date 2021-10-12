package com.tick42.quicksilver.models;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_tokens")
public class EmailToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(cascade = CascadeType.DETACH)
    private UserModel user;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    public EmailToken() {
    }

    public EmailToken(String token, UserModel user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusDays(2);
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}