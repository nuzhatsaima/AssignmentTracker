package org.app.model;

import com.fasterxml.jackson.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Student class for BUP UCAM Assignment Tracker
 */
@JsonTypeName("student")
public class Student extends User {
    @JsonProperty("studentId")
    private String studentId;
    @JsonProperty("program")
    private String program;
    @JsonProperty("semester")
    private int semester;

    // Remove JsonIgnore to allow serialization of these collections
    @JsonProperty("enrolledCourseIds")
    private List<String> enrolledCourseIds = new ArrayList<>(); // Store course IDs instead of Course objects

    @JsonProperty("submissionIds")
    private List<String> submissionIds = new ArrayList<>(); // Store submission IDs instead of Submission objects

    @JsonIgnore
    private List<Course> enrolledCourses;
    @JsonIgnore
    private List<Submission> submissions;

    // Default constructor for Jackson
    public Student() {
        super();
        this.enrolledCourses = new ArrayList<>();
        this.submissions = new ArrayList<>();
    }

    public Student(String userId, String name, String email, String password,
                   String studentId, String program, int semester) {
        super(userId, name, email, password, UserRole.STUDENT);
        this.studentId = studentId;
        this.program = program;
        this.semester = semester;
        this.enrolledCourses = new ArrayList<>();
        this.submissions = new ArrayList<>();
        this.enrolledCourseIds = new ArrayList<>();
        this.submissionIds = new ArrayList<>();
    }

    public void displayDashboard() {
        System.out.println("=== Student Dashboard - " + getName() + " ===");
        System.out.println("Student ID: " + studentId);
        System.out.println("Program: " + program);
        System.out.println("Semester: " + semester);
        System.out.println("Enrolled Courses: " + enrolledCourses.size());
        System.out.println("Total Submissions: " + submissions.size());
    }

    public String getDisplayName() {
        return getName() + " (" + studentId + ")";
    }

    public void enrollInCourse(Course course) {
        if (!enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
            if (!enrolledCourseIds.contains(course.getCourseId())) {
                enrolledCourseIds.add(course.getCourseId());
            }
        }
    }

    public void addSubmission(Submission submission) {
        if (!submissions.contains(submission)) {
            submissions.add(submission);
            if (!submissionIds.contains(submission.getSubmissionId())) {
                submissionIds.add(submission.getSubmissionId());
            }
        }
    }

    // Methods to get and set the IDs for JSON serialization
    public List<String> getEnrolledCourseIds() {
        return enrolledCourseIds;
    }

    public void setEnrolledCourseIds(List<String> enrolledCourseIds) {
        this.enrolledCourseIds = enrolledCourseIds;
    }

    public List<String> getSubmissionIds() {
        return submissionIds;
    }

    public void setSubmissionIds(List<String> submissionIds) {
        this.submissionIds = submissionIds;
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public List<Course> getEnrolledCourses() { return new ArrayList<>(enrolledCourses); }

    public List<Submission> getSubmissions() { return new ArrayList<>(submissions); }

    // Add methods to directly manipulate the enrolled courses list for restoration
    public void clearEnrolledCourses() {
        this.enrolledCourses.clear();
    }

    public void addEnrolledCourse(Course course) {
        if (!enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
        }
    }

    // Add methods to directly manipulate the submissions list for restoration
    public void clearSubmissions() {
        this.submissions.clear();
    }

    public void addSubmissionDirect(Submission submission) {
        if (!submissions.contains(submission)) {
            submissions.add(submission);
        }
    }
}
