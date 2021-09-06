package com.tick42.quicksilver.models;

import javax.persistence.*;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import org.hibernate.annotations.Type;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    @OrderBy(value = "upload_date DESC")
    private Set<Extension> extensions = new HashSet<>();

    @Column(name = "enabled", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean active = true;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private File profileImage;

    @Column(name = "is_enabled")
    private boolean isEnabled = false;

    @Column(name = "extensions_rated")
    private int extensionsRated;

    private String username;
    private String password;
    private String role;
    private double rating;
    private String country;
    private String info;

    public UserModel(){

    }

    public UserModel(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public UserModel(RegisterSpec newUser, String role) {
        this.setUsername(newUser.getUsername());
        this.setPassword(newUser.getPassword());
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
