package com.tick42.quicksilver.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class UserDetails extends User {
    private long id;
    private String token;

    public UserDetails(UserModel userModel, Collection<? extends GrantedAuthority> authorities){
        super(userModel.getUsername(), userModel.getPassword(), authorities);
        this.id = userModel.getId();
    }
    public UserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, int id){
        super(username,password,authorities);
        this.id = id;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
