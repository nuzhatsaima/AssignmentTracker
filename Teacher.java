package org.app.model;

import com.fasterxml.jackson.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher class for BUP UCAM Assignment Tracker
 */
@JsonTypeName("teacher")
public class Teacher extends User {
    @JsonProperty("department")
    private String department;
    @JsonProperty("employeeId")
    private String employeeId;

    // Store course and assignment IDs for persistence
    @JsonProperty("courseIds")
    private List<String> courseIds = new ArrayList<>();

    @JsonProperty("assignmentIds")
    private List<String> assignmentIds = new ArrayList<>();

    @JsonIgnore
    private List<Course> coursesTaught;
    @JsonIgnore
    private List<Assignment> assignmentsCreated;

    private String displayName; // This field was missing and causing the error

    // Default constructor for Jackson
    public Teacher() {
        super();
        this.coursesTaught = new ArrayList<>();
        this.assignmentsCreated = new ArrayList<>();
        this.courseIds = new ArrayList<>();
        this.assignmentIds = new ArrayList<>();
    }

    public Teacher(String userId, String name, String email, String password,
                   String department, String employeeId) {
        super(userId, name, email, password, UserRole.TEACHER);
        this.department = department;
        this.employeeId = employeeId;
        this.coursesTaught = new ArrayList<>();
        this.assignmentsCreated = new ArrayList<>();
        this.courseIds = new ArrayList<>();
        this.assignmentIds = new ArrayList<>();
        this.displayName = "Prof. " + name + " (" + department + ")";
    }


    public void displayDashboard() {
        System.out.println("=== Teacher Dashboard - " + getName() + " ===");
        System.out.println("Department: " + department);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Courses Teaching: " + coursesTaught.size());
        System.out.println("Assignments Created: " + assignmentsCreated.size());
    }

    public String getDisplayName() {
        if (displayName == null && getName() != null && department != null) {
            displayName = "Prof. " + getName() + " (" + department + ")";
        }
        return displayName;
    }

    public void addCourse(Course course) {
        if (!coursesTaught.contains(course)) {
            coursesTaught.add(course);
            if (!courseIds.contains(course.getCourseId())) {
                courseIds.add(course.getCourseId());
            }
        }
    }

    public void addAssignment(Assignment assignment) {
        if (!assignmentsCreated.contains(assignment)) {
            assignmentsCreated.add(assignment);
            if (!assignmentIds.contains(assignment.getAssignmentId())) {
                assignmentIds.add(assignment.getAssignmentId());
            }
        }
    }

    // Methods to manipulate the collections directly for restoration
    public void clearCoursesTaught() {
        this.coursesTaught.clear();
    }

    public void addCourseDirect(Course course) {
        if (!coursesTaught.contains(course)) {
            coursesTaught.add(course);
        }
    }

    public void clearAssignmentsCreated() {
        this.assignmentsCreated.clear();
    }

    public void addAssignmentDirect(Assignment assignment) {
        if (!assignmentsCreated.contains(assignment)) {
            assignmentsCreated.add(assignment);
        }
    }

    // Getters and setters for the ID lists
    public List<String> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<String> courseIds) {
        this.courseIds = courseIds;
    }

    public List<String> getAssignmentIds() {
        return assignmentIds;
    }

    public void setAssignmentIds(List<String> assignmentIds) {
        this.assignmentIds = assignmentIds;
    }

    // Getters and Setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) {
        this.department = department;
        // Update display name when department changes
        if (getName() != null && department != null) {
            this.displayName = "Prof. " + getName() + " (" + department + ")";
        }
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public List<Course> getCoursesTaught() { return new ArrayList<>(coursesTaught); }

    public List<Assignment> getAssignmentsCreated() { return new ArrayList<>(assignmentsCreated); }

    @Override
    public void setName(String name) {
        super.setName(name);
        // Update display name when name changes
        if (name != null && getDepartment() != null) {
            this.displayName = "Prof. " + name + " (" + getDepartment() + ")";
        }
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "userId='" + getUserId() + '\'' +
                ", name='" + getName() + '\'' +
                ", displayName='" + displayName + '\'' +
                ", department='" + department + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", email='" + getEmail() + '\'' +
                ", active=" + isActive() +
                ", emailVerified=" + isEmailVerified() +
                '}';
    }
}
