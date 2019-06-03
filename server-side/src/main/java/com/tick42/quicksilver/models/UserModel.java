package com.tick42.quicksilver.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.tick42.quicksilver.models.Spec.UserSpec;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message="is required")
    @Size(min=1, message="is required")
    @Column(name = "username")
    private String username;

    @NotNull(message="is required")
    @Size(min=1, message="is required")
    private String password;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    @OrderBy(value = "upload_date DESC")
    private Set<Extension> extensions = new HashSet<>();

    @Column(name = "enabled", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean active = true;

    private String role;

    @Column(name = "rating")
    private double rating;

    @Column(name = "extensions_rated")
    private int extensionsRated;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private File profileImage;

    @Column(name = "country")
    private String country;

    @Column(name = "info")
    private String info;

    public UserModel(){

    }

    public UserModel(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public UserModel(UserSpec userSpec, String role) {
        this.setUsername(userSpec.getUsername());
        this.setPassword(userSpec.getPassword());
        this.setRole(role);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(Set<Extension> extensions) {
        this.extensions = extensions;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getIsActive() {
        return active;
    }

    public void setIsActive(boolean active) {
        this.active = active;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getExtensionsRated() {
        return extensionsRated;
    }

    public void setExtensionsRated(int extensionsRated) {
        this.extensionsRated = extensionsRated;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public File getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(File profileImage) {
        this.profileImage = profileImage;
    }
}
