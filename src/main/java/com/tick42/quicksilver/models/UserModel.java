package com.tick42.quicksilver.models;

import javax.persistence.*;
import com.tick42.quicksilver.models.specs.RegisterSpec;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    @Column(name = "is_active", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean isActive = true;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File profileImage;

    @Column(name = "is_enabled")
    private boolean isEnabled = false;

    @Column(name = "extensions_rated")
    private int extensionsRated;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @OneToOne(mappedBy = "user")
    private Settings gitHubSettings;

    private String password;
    private String role;
    private double rating;
    private String country;
    private String info;

    public UserModel(){

    }

    public UserModel(String username, String email, String password, String role, String info, String country) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.info = info;
        this.country = country;
    }

    public UserModel(long id, String username, String email, String password, String role,
                     String info, String country) {
        this(username, email, password, role, info, country);
        this.id = id;
    }

    public UserModel(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public UserModel(String username, String email, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    public UserModel(RegisterSpec newUser, File profileImage, String role) {
        this(newUser.getUsername(), newUser.getEmail(), newUser.getPassword(), role,
                newUser.getInfo(), newUser.getCountry());
        setProfileImage(profileImage);
    }

    public UserModel(String username, String email, String password, String role, String info,
                     String country, double rating, int extensionsRated) {
        this(username, email, password, role, info, country);
        this.rating = rating;
        this.extensionsRated = extensionsRated;
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
        if(profileImage != null){
            this.profileImage = profileImage;
            profileImage.setOwner(this);
        }
    }
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Settings getGitHubSettings() {
        return gitHubSettings;
    }

    public void setGitHubSettings(Settings gitHubSettings) {
        this.gitHubSettings = gitHubSettings;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
