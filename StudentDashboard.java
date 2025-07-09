package org.app.gui;

import org.app.model.*;
import org.app.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Student Dashboard GUI for BUP UCAM Assignment Tracker (Tabbed UI version)
 */
public class StudentDashboard extends JPanel {
    private Student student;
    private UserService userService;
    private CourseService courseService;
    private AssignmentService assignmentService;
    private AssignmentTrackerGUI mainFrame;

    private JTabbedPane tabbedPane;
    private DefaultTableModel coursesTableModel;
    private DefaultTableModel assignmentsTableModel;
    private DefaultTableModel submissionsTableModel;
    private DefaultTableModel gradesTableModel;

    public StudentDashboard(Student student, UserService userService,
                            CourseService courseService, AssignmentService assignmentService,
                            AssignmentTrackerGUI mainFrame) {
        this.student = student;
        this.userService = userService;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.mainFrame = mainFrame;

        initializeComponents();
        refreshData();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("My Courses", createCoursesPanel());
        tabbedPane.addTab("Assignments", createAssignmentsPanel());
        tabbedPane.addTab("My Submissions", createSubmissionsPanel());
        tabbedPane.addTab("Grades", createGradesPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(76, 175, 80));

        JLabel title = new JLabel("Welcome, " + student.getName() + " (" + student.getStudentId() + ")");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 0));

        JLabel details = new JLabel("Program: " + student.getProgram() + " | Semester: " + student.getSemester());
        details.setFont(new Font("Arial", Font.PLAIN, 14));
        details.setForeground(Color.WHITE);
        details.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(new Color(76, 175, 80));
        left.add(title);
        left.add(details);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 14));
        logoutBtn.setBackground(new Color(244, 67, 54));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> mainFrame.showLogin());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(new Color(76, 175, 80));
        right.add(logoutBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        List<Course> courses = courseService.getCoursesForStudent(student);
        List<Submission> submissions = assignmentService.getSubmissionsByStudent(student);
        int assignmentCount = courses.stream().mapToInt(c -> assignmentService.getAssignmentsByCourse(c).size()).sum();
        long gradedCount = submissions.stream().filter(s -> s.getStatus() == SubmissionStatus.GRADED).count();

        panel.add(createStatCard("Courses", courses.size(), new Color(65, 161, 237)));
        panel.add(createStatCard("Assignments", assignmentCount, new Color(178, 100, 230)));
        panel.add(createStatCard("Submissions", submissions.size(), new Color(175, 38, 73)));
        panel.add(createStatCard("Graded", gradedCount, new Color(55, 161, 80)));

        return panel;
    }

    private JPanel createStatCard(String title, long value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setMaximumSize(new Dimension(200, 120));

        JLabel iconLabel = new JLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setPreferredSize(new Dimension(30, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(valueLabel);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton enrollBtn = new JButton("Enroll in Courses");
        enrollBtn.setBackground(new Color(33, 150, 243));
        enrollBtn.setForeground(Color.WHITE);
        enrollBtn.addActionListener(e -> showEnrollCoursesDialog());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(enrollBtn);

        coursesTableModel = new DefaultTableModel(new String[]{"Course Code", "Name", "Instructor", "Credits", "Dept"}, 0);
        JTable table = new JTable(coursesTableModel);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAssignmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton submitBtn = new JButton("Submit Assignment");
        submitBtn.setBackground(new Color(76, 175, 80));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> showSubmitAssignmentDialog());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(submitBtn);

        assignmentsTableModel = new DefaultTableModel(new String[]{"Title", "Course", "Type", "Due Date", "Max Marks", "Status"}, 0);
        JTable table = new JTable(assignmentsTableModel);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSubmissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        submissionsTableModel = new DefaultTableModel(new String[]{"Assignment", "Course", "Submitted At", "Status", "Late"}, 0);
        JTable table = new JTable(submissionsTableModel);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        gradesTableModel = new DefaultTableModel(new String[]{"Assignment", "Course", "Marks", "Max", "%", "Feedback"}, 0);
        JTable table = new JTable(gradesTableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void showEnrollCoursesDialog() {
        EnrollCoursesDialog dialog = new EnrollCoursesDialog(mainFrame, student, courseService);
        dialog.setVisible(true);
        refreshData();
    }

    private void showSubmitAssignmentDialog() {
        List<Course> enrolled = courseService.getCoursesForStudent(student);
        if (enrolled.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You are not enrolled in any courses.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SubmitAssignmentDialog dialog = new SubmitAssignmentDialog(mainFrame, student, enrolled, assignmentService);
        dialog.setVisible(true);
        refreshData();
    }

    private void refreshData() {
        refreshCoursesTable();
        refreshAssignmentsTable();
        refreshSubmissionsTable();
        refreshGradesTable();
    }

    private void refreshCoursesTable() {
        coursesTableModel.setRowCount(0);
        for (Course c : courseService.getCoursesForStudent(student)) {
            coursesTableModel.addRow(new Object[]{
                    c.getCourseCode(), c.getCourseName(), c.getInstructor().getName(), c.getCreditHours(), c.getDepartment()
            });
        }
    }

    private void refreshAssignmentsTable() {
        assignmentsTableModel.setRowCount(0);
        List<Submission> submissions = assignmentService.getSubmissionsByStudent(student);
        for (Course c : courseService.getCoursesForStudent(student)) {
            for (Assignment a : assignmentService.getAssignmentsByCourse(c)) {
                boolean submitted = submissions.stream().anyMatch(s -> s.getAssignment().equals(a));
                assignmentsTableModel.addRow(new Object[]{
                        a.getTitle(), c.getCourseCode(), a.getType(),
                        a.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        a.getMaxMarks(), submitted ? "Submitted" : "Pending"
                });
            }
        }
    }

    private void refreshSubmissionsTable() {
        submissionsTableModel.setRowCount(0);
        for (Submission s : assignmentService.getSubmissionsByStudent(student)) {
            submissionsTableModel.addRow(new Object[]{
                    s.getAssignment().getTitle(),
                    s.getAssignment().getCourse().getCourseCode(),
                    s.getSubmittedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    s.getStatus(),
                    s.isLateSubmission() ? "Yes" : "No"
            });
        }
    }

    private void refreshGradesTable() {
        gradesTableModel.setRowCount(0);
        for (Submission s : assignmentService.getSubmissionsByStudent(student)) {
            if (s.getStatus() == SubmissionStatus.GRADED) {
                Assignment a = s.getAssignment();
                double percent = (s.getMarks() * 100.0) / a.getMaxMarks();
                gradesTableModel.addRow(new Object[]{
                        a.getTitle(), a.getCourse().getCourseCode(),
                        s.getMarks(), a.getMaxMarks(), String.format("%.1f%%", percent),
                        s.getFeedback() == null ? "N/A" : s.getFeedback()
                });
            }
        }
    }
}
