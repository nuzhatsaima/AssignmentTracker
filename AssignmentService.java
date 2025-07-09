package org.app.service;

import org.app.model.*;
import org.app.util.DataPersistence;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Assignment Management Service for BUP UCAM Assignment Tracker
 */
public class AssignmentService {
    private Map<String, Assignment> assignments;
    private Map<String, Submission> submissions;
    private int assignmentCounter;
    private int submissionCounter;
    private DataPersistence dataPersistence;

    public AssignmentService() {
        this.dataPersistence = new DataPersistence();
        this.assignments = new HashMap<>();
        this.submissions = new HashMap<>();
        loadData();
        System.out.println("✓ AssignmentService initialized with persistent storage");
    }

    private void loadData() {
        DataPersistence.AssignmentData assignmentData = dataPersistence.loadAssignments();
        if (assignmentData.assignments != null) {
            this.assignments = assignmentData.assignments;
        }
        if (assignmentData.submissions != null) {
            this.submissions = assignmentData.submissions;
        }
        this.assignmentCounter = assignmentData.assignmentCounter;
        this.submissionCounter = assignmentData.submissionCounter;
        System.out.println("✓ Loaded " + assignments.size() + " assignments and " +
                submissions.size() + " submissions from storage");
    }

    private void saveData() {
        dataPersistence.saveAssignments(assignments, submissions, assignmentCounter, submissionCounter);
    }

    /**
     * Create a new assignment
     */
    public Assignment createAssignment(String title, String description, Course course,
                                       Teacher creator, AssignmentType type, int maxMarks,
                                       LocalDateTime dueDate) {
        String assignmentId = "ASSIGN-" + String.format("%04d", assignmentCounter++);
        Assignment assignment = new Assignment(assignmentId, title, description, course,
                creator, type, dueDate, maxMarks);

        assignments.put(assignmentId, assignment);
        course.addAssignment(assignment);
        creator.addAssignment(assignment);

        saveData(); // Save after creating assignment
        System.out.println("✓ Assignment created successfully: " + title);
        return assignment;
    }

    /**
     * Submit assignment by student
     */
    public Submission submitAssignment(Assignment assignment, Student student, String content) {
        if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
            throw new IllegalStateException("Assignment is not active for submissions");
        }

        String submissionId = "SUB-" + String.format("%04d", submissionCounter++);
        Submission submission = new Submission(submissionId, assignment, student, content);

        submissions.put(submissionId, submission);
        assignment.addSubmission(submission);
        student.addSubmission(submission);

        saveData(); // Save after submission
        System.out.println("✓ Assignment submitted successfully by " + student.getName());
        return submission;
    }

    /**
     * Grade a submission
     */
    public void gradeSubmission(String submissionId, int marks, String feedback, Teacher teacher) {
        Submission submission = submissions.get(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("Submission not found");
        }

        if (marks > submission.getAssignment().getMaxMarks()) {
            throw new IllegalArgumentException("Marks cannot exceed maximum marks");
        }

        submission.grade(marks, feedback, teacher);
        saveData(); // Save after grading
        System.out.println("✓ Submission graded successfully");
    }

    /**
     * Get assignments by course
     */
    public List<Assignment> getAssignmentsByCourse(Course course) {
        return assignments.values().stream()
                .filter(assignment -> assignment.getCourse().equals(course))
                .collect(Collectors.toList());
    }

    /**
     * Get assignments by teacher
     */
    public List<Assignment> getAssignmentsByTeacher(Teacher teacher) {
        return assignments.values().stream()
                .filter(assignment -> assignment.getCreator().equals(teacher))
                .collect(Collectors.toList());
    }

    /**
     * Get submissions for an assignment
     */
    public List<Submission> getSubmissionsForAssignment(Assignment assignment) {
        return submissions.values().stream()
                .filter(submission -> submission.getAssignment().equals(assignment))
                .collect(Collectors.toList());
    }

    /**
     * Get submissions by student
     */
    public List<Submission> getSubmissionsByStudent(Student student) {
        return submissions.values().stream()
                .filter(submission -> submission.getStudent().equals(student))
                .collect(Collectors.toList());
    }

    /**
     * Get overdue assignments
     */
    public List<Assignment> getOverdueAssignments() {
        return assignments.values().stream()
                .filter(Assignment::isOverdue)
                .collect(Collectors.toList());
    }

    /**
     * Close assignment for submissions
     */
    public void closeAssignment(String assignmentId) {
        Assignment assignment = assignments.get(assignmentId);
        if (assignment != null) {
            assignment.setStatus(AssignmentStatus.CLOSED);
            saveData(); // Save after status change
            System.out.println("✓ Assignment closed: " + assignment.getTitle());
        }
    }

    /**
     * Get assignment statistics
     */
    public void displayAssignmentStatistics(Assignment assignment) {
        List<Submission> assignmentSubmissions = getSubmissionsForAssignment(assignment);
        int totalStudents = assignment.getCourse().getEnrolledStudents().size();
        int submittedCount = assignmentSubmissions.size();
        int gradedCount = (int) assignmentSubmissions.stream()
                .filter(sub -> sub.getStatus() == SubmissionStatus.GRADED)
                .count();

        System.out.println("=== Assignment Statistics ===");
        System.out.println("Assignment: " + assignment.getTitle());
        System.out.println("Total Students: " + totalStudents);
        System.out.println("Submissions: " + submittedCount + "/" + totalStudents);
        System.out.println("Graded: " + gradedCount + "/" + submittedCount);
        System.out.println("Submission Rate: " + String.format("%.1f%%",
                (submittedCount * 100.0) / totalStudents));
    }

    /**
     * Restore a student's submissions based on their stored submissionIds
     * This ensures submissions persist between sessions for students
     */
    public void restoreStudentSubmissions(Student student) {
        List<String> submissionIds = student.getSubmissionIds();

        // Clear the current list to avoid duplicates
        student.clearSubmissions();

        // For each submissionId, get the actual submission and add it to the student's submissions
        for (String submissionId : submissionIds) {
            Submission submission = getSubmission(submissionId);
            if (submission != null) {
                // Make sure the student has the submission in their list
                student.addSubmissionDirect(submission);
            }
        }

        System.out.println("✓ Restored " + student.getSubmissions().size() +
                          " submissions for student " + student.getName());
    }

    /**
     * Restore a teacher's assignments based on their stored assignmentIds
     * This ensures assignments persist between sessions for teachers
     */
    public void restoreTeacherAssignments(Teacher teacher) {
        List<String> assignmentIds = teacher.getAssignmentIds();

        // Clear the current list to avoid duplicates
        teacher.clearAssignmentsCreated();

        // For each assignmentId, get the actual assignment and add it to the teacher's assignmentsCreated
        for (String assignmentId : assignmentIds) {
            Assignment assignment = getAssignment(assignmentId);
            if (assignment != null) {
                teacher.addAssignmentDirect(assignment);
                // Make sure the assignment has this teacher as creator
                if (!assignment.getCreator().equals(teacher)) {
                    assignment.setCreator(teacher);
                }
            }
        }

        System.out.println("✓ Restored " + teacher.getAssignmentsCreated().size() +
                          " assignments for teacher " + teacher.getName());
    }

    // Getters
    public Assignment getAssignment(String assignmentId) {
        return assignments.get(assignmentId);
    }

    public Submission getSubmission(String submissionId) {
        return submissions.get(submissionId);
    }

    public List<Assignment> getAllAssignments() {
        return new ArrayList<>(assignments.values());
    }

    public List<Submission> getAllSubmissions() {
        return new ArrayList<>(submissions.values());
    }
}