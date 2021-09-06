package com.tick42.quicksilver.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;

public class UserDetails extends User {
    private long id;
    private UserModel userModel;

    public UserDetails(UserModel userModel, Collection<? extends GrantedAuthority> authorities){
        super(userModel.getUsername(), userModel.getPassword(), authorities);
        this.id = userModel.getId();
        this.userModel = userModel;
    }

    public UserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, long id){
        super(username,password,authorities);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
