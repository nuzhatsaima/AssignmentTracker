package org.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Base User class for BUP UCAM Assignment Tracker
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Teacher.class, name = "teacher"),
        @JsonSubTypes.Type(value = Student.class, name = "student")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class User {
    private String userId;
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private LocalDateTime createdAt;
    private boolean active;
    private boolean emailVerified;
    private boolean isActive;
    private boolean isEmailVerified;
    private String emailVerificationCode;
    private List<String> courseIds;
    private List<String> assignmentIds;

    // Default constructor for Jackson
    public User() {
        this.courseIds = new ArrayList<>();
        this.assignmentIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.isActive = true;
        this.emailVerified = false;
        this.isEmailVerified = false;
    }

    // Constructor with basic fields
    public User(String userId, String name, String email, String password, UserRole role) {
        this();
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) {
        this.active = active;
        this.isActive = active; // Keep both in sync
    }

    public boolean getActive() { return active; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        this.isEmailVerified = emailVerified; // Keep both in sync
    }

    public boolean getEmailVerified() { return emailVerified; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
        this.active = isActive; // Keep both in sync
    }

    public boolean getIsEmailVerified() { return isEmailVerified; }
    public void setIsEmailVerified(boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
        this.emailVerified = isEmailVerified; // Keep both in sync
    }

    public String getEmailVerificationCode() { return emailVerificationCode; }
    public void setEmailVerificationCode(String code) { this.emailVerificationCode = code; }

    public List<String> getCourseIds() { return courseIds; }
    public void setCourseIds(List<String> courseIds) { this.courseIds = courseIds != null ? courseIds : new ArrayList<>(); }

    public List<String> getAssignmentIds() { return assignmentIds; }
    public void setAssignmentIds(List<String> assignmentIds) { this.assignmentIds = assignmentIds != null ? assignmentIds : new ArrayList<>(); }

    // Abstract methods that subclasses must implement
    public abstract void displayDashboard();

    public abstract String getDisplayName();

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", emailVerified=" + emailVerified +
                '}';
    }
}