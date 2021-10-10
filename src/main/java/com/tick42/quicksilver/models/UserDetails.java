package com.tick42.quicksilver.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;
import java.util.Objects;

public class UserDetails extends User {
    private final long id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserDetails that = (UserDetails) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    public long getId() {
        return id;
    }

    public UserModel getUserModel() {
        return userModel;
    }
}
