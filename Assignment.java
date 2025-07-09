package org.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment class for BUP UCAM Assignment Tracker
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Assignment {
    private String assignmentId;
    private String title;
    private String description;
    private Course course;
    private Teacher creator;
    private AssignmentType type;
    private AssignmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private int maxMarks;
    private List<Submission> submissions;
    private List<String> attachmentPaths; // Added field for assignment attachments

    // Default constructor for Jackson
    public Assignment() {
        this.submissions = new ArrayList<>();
        this.attachmentPaths = new ArrayList<>(); // Initialize attachmentPaths
        this.createdAt = LocalDateTime.now();
        this.status = AssignmentStatus.ACTIVE;
    }

    // Constructor with basic fields
    public Assignment(String assignmentId, String title, String description, Course course, Teacher creator) {
        this();
        this.assignmentId = assignmentId;
        this.title = title;
        this.description = description;
        this.course = course;
        this.creator = creator;
    }

    // Constructor with all fields
    public Assignment(String assignmentId, String title, String description, Course course,
                     Teacher creator, AssignmentType type, LocalDateTime dueDate, int maxMarks) {
        this(assignmentId, title, description, course, creator);
        this.type = type;
        this.dueDate = dueDate;
        this.maxMarks = maxMarks;
    }

    // Getters and setters
    public String getAssignmentId() { return assignmentId; }
    public void setAssignmentId(String assignmentId) { this.assignmentId = assignmentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Teacher getCreator() { return creator; }
    public void setCreator(Teacher creator) { this.creator = creator; }

    public AssignmentType getType() { return type; }
    public void setType(AssignmentType type) { this.type = type; }

    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public List<Submission> getSubmissions() { return submissions; }
    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions != null ? submissions : new ArrayList<>();
    }

    public void addSubmission(Submission submission) {
        if (submissions == null) {
            submissions = new ArrayList<>();
        }
        submissions.add(submission);
    }

    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate);
    }

    public List<String> getAttachmentPaths() {
        if (attachmentPaths == null) {
            attachmentPaths = new ArrayList<>();
        }
        return attachmentPaths;
    }

    public void setAttachmentPaths(List<String> attachmentPaths) {
        this.attachmentPaths = attachmentPaths != null ? attachmentPaths : new ArrayList<>();
    }

    public void addAttachmentPath(String path) {
        if (attachmentPaths == null) {
            attachmentPaths = new ArrayList<>();
        }
        if (path != null && !path.isEmpty() && !attachmentPaths.contains(path)) {
            attachmentPaths.add(path);
        }
    }

    public void displayAssignmentInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        System.out.println("Assignment: " + title);
        System.out.println("Course: " + (course != null ? course.getCourseCode() + " - " + course.getCourseName() : "N/A"));
        System.out.println("Type: " + (type != null ? type.getDisplayName() : "N/A"));
        System.out.println("Status: " + status);
        System.out.println("Due Date: " + (dueDate != null ? dueDate.format(formatter) : "N/A"));
        System.out.println("Max Marks: " + maxMarks);
        System.out.println("Submissions: " + getSubmissionCount());
        System.out.println("Attachments: " + attachmentPaths.size());
        if (!attachmentPaths.isEmpty()) {
            System.out.println("Attached Files:");
            for (String path : attachmentPaths) {
                System.out.println("  - " + new java.io.File(path).getName());
            }
        }
    }

    public int getSubmissionCount() {
        return submissions != null ? submissions.size() : 0;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId='" + assignmentId + '\'' +
                ", title='" + title + '\'' +
                ", course=" + (course != null ? course.getCourseName() : "null") +
                ", creator=" + (creator != null ? creator.getName() : "null") +
                ", type=" + type +
                ", status=" + status +
                ", dueDate=" + dueDate +
                ", maxMarks=" + maxMarks +
                '}';
    }
}