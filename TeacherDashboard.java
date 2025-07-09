package org.app.gui;

import org.app.model.*;
import org.app.service.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeacherDashboard extends JPanel {
    private Teacher teacher;
    private UserService userService;
    private CourseService courseService;
    private AssignmentService assignmentService;
    private AssignmentTrackerGUI mainFrame;

    private DefaultTableModel coursesTableModel;
    private DefaultTableModel assignmentsTableModel;
    private DefaultTableModel submissionsTableModel;

    private JTable coursesTable;
    private JTable assignmentsTable;
    private JTable submissionsTable;

    public TeacherDashboard(Teacher teacher, UserService userService,
                            CourseService courseService, AssignmentService assignmentService,
                            AssignmentTrackerGUI mainFrame) {
        this.teacher = teacher;
        this.userService = userService;
        this.courseService = courseService;
        this.assignmentService = assignmentService;
        this.mainFrame = mainFrame;

        initializeComponents();
        refreshAllTables();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Teacher Dashboard - " + teacher.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JButton logoutButton = createStyledButton("Logout", new Color(231, 76, 60), Color.WHITE);
        logoutButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) mainFrame.showLogin();
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Manage Courses", createManageCoursesTab());
        tabbedPane.addTab("Create Assignments", createCreateAssignmentsTab());
        tabbedPane.addTab("View Assignments", createViewAssignmentsTab());
        tabbedPane.addTab("Grade Submissions", createGradeSubmissionsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createManageCoursesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton createCourseBtn = createStyledButton("Create Course", new Color(52, 152, 219), Color.WHITE);
        createCourseBtn.addActionListener(e -> {
            showCreateCourseDialog();
            refreshCoursesTable();
        });

        String[] columns = {"Course Code", "Course Name", "Department", "Credit Hours", "Enrolled Students"};
        coursesTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        coursesTable = new JTable(coursesTableModel);
        styleTable(coursesTable);

        panel.add(createToolbar(createCourseBtn), BorderLayout.NORTH);
        panel.add(new JScrollPane(coursesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCreateAssignmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton createAssignmentBtn = createStyledButton("Create Assignment", new Color(46, 204, 113), Color.WHITE);
        createAssignmentBtn.addActionListener(e -> {
            showCreateAssignmentDialog();
            refreshAssignmentsTable();
        });

        String[] columns = {"Title", "Course", "Type", "Due Date", "Max Marks", "Submissions"};
        assignmentsTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        assignmentsTable = new JTable(assignmentsTableModel);
        styleTable(assignmentsTable);

        panel.add(createToolbar(createAssignmentBtn), BorderLayout.NORTH);
        panel.add(new JScrollPane(assignmentsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createViewAssignmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refreshBtn = createStyledButton("Refresh Assignments", new Color(155, 89, 182), Color.WHITE);
        refreshBtn.addActionListener(e -> refreshAssignmentsTable());

        if (assignmentsTableModel == null) {
            String[] columns = {"Title", "Course", "Type", "Due Date", "Max Marks", "Submissions"};
            assignmentsTableModel = new DefaultTableModel(columns, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
            assignmentsTable = new JTable(assignmentsTableModel);
            styleTable(assignmentsTable);
        }

        panel.add(createToolbar(refreshBtn), BorderLayout.NORTH);
        panel.add(new JScrollPane(assignmentsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGradeSubmissionsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refreshBtn = createStyledButton("Refresh Submissions", new Color(241, 196, 15), Color.BLACK);
        refreshBtn.addActionListener(e -> refreshSubmissionsTable());

        JButton gradeBtn = createStyledButton("Grade Selected Submission", new Color(241, 196, 15), Color.BLACK);
        gradeBtn.addActionListener(e -> gradeSelectedSubmission());

        JButton viewFileBtn = createStyledButton("View Attached File", new Color(241, 196, 15), Color.BLACK);
        viewFileBtn.addActionListener(e -> viewSelectedSubmissionFile());

        String[] columns = {"Assignment", "Student", "Submitted At", "Status", "File", "Marks"};
        submissionsTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        submissionsTable = new JTable(submissionsTableModel);
        styleTable(submissionsTable);

        panel.add(createToolbar(refreshBtn, gradeBtn, viewFileBtn), BorderLayout.NORTH);
        panel.add(new JScrollPane(submissionsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createToolbar(JButton... buttons) {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setBackground(Color.WHITE);
        for (JButton btn : buttons) {
            btn.setPreferredSize(new Dimension(200, 30));
            toolbar.add(btn);
        }
        return toolbar;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return button;
    }

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setIntercellSpacing(new Dimension(5, 5));
    }

    private void refreshAllTables() {
        refreshCoursesTable();
        refreshAssignmentsTable();
        refreshSubmissionsTable();
    }

    private void refreshCoursesTable() {
        if (coursesTableModel == null) return;
        coursesTableModel.setRowCount(0);
        List<Course> courses = courseService.getCoursesByTeacher(teacher);
        for (Course c : courses) {
            coursesTableModel.addRow(new Object[]{
                    c.getCourseCode(), c.getCourseName(), c.getDepartment(),
                    c.getCreditHours(), c.getEnrolledStudents().size()
            });
        }
    }

    private void refreshAssignmentsTable() {
        if (assignmentsTableModel == null) return;
        assignmentsTableModel.setRowCount(0);
        List<Assignment> assignments = assignmentService.getAssignmentsByTeacher(teacher);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        for (Assignment a : assignments) {
            assignmentsTableModel.addRow(new Object[]{
                    a.getTitle(), a.getCourse().getCourseCode(),
                    a.getType().toString(), a.getDueDate().format(formatter),
                    a.getMaxMarks(), a.getSubmissionCount()
            });
        }
    }

    private void refreshSubmissionsTable() {
        if (submissionsTableModel == null) return;
        submissionsTableModel.setRowCount(0);
        List<Assignment> assignments = assignmentService.getAssignmentsByTeacher(teacher);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        for (Assignment a : assignments) {
            List<Submission> submissions = assignmentService.getSubmissionsForAssignment(a);
            for (Submission s : submissions) {
                submissionsTableModel.addRow(new Object[]{
                        a.getTitle(), s.getStudent().getName(),
                        s.getSubmittedAt().format(formatter), s.getStatus().toString(),
                        s.getFile() != null ? s.getFile().getName() : "No file",
                        s.getMarks() != null ? s.getMarks() : "Not graded"
                });
            }
        }
    }

    private void showCreateCourseDialog() {
        CreateCourseDialog dialog = new CreateCourseDialog(mainFrame, teacher, courseService);
        dialog.setVisible(true);
        refreshCoursesTable();
    }

    private void showCreateAssignmentDialog() {
        List<Course> teacherCourses = courseService.getCoursesByTeacher(teacher);
        if (teacherCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You need to create a course first before creating assignments.", "No Courses", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CreateAssignmentDialog dialog = new CreateAssignmentDialog(mainFrame, teacher, teacherCourses, assignmentService);
        dialog.setVisible(true);
        refreshAssignmentsTable();
    }

    private void gradeSelectedSubmission() {
        int row = submissionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a submission to grade.");
            return;
        }
        Submission selectedSubmission = getSubmissionFromTableRow(row);
        if (selectedSubmission == null) {
            JOptionPane.showMessageDialog(this, "Selected submission not found.");
            return;
        }
        String markStr = JOptionPane.showInputDialog(this, "Enter marks for this submission:");
        if (markStr == null) return;
        try {
            int marks = Integer.parseInt(markStr);
            assignmentService.gradeSubmission(selectedSubmission.getSubmissionId(), marks, "Graded via dashboard", teacher);
            JOptionPane.showMessageDialog(this, "Submission graded successfully.");
            refreshSubmissionsTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid marks input.");
        }
    }

    private void viewSelectedSubmissionFile() {
        int row = submissionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a submission to view.");
            return;
        }
        Submission selectedSubmission = getSubmissionFromTableRow(row);
        if (selectedSubmission == null) {
            JOptionPane.showMessageDialog(this, "Selected submission not found.");
            return;
        }
        if (selectedSubmission.getFile() != null) {
            try {
                java.net.URI fileUri = selectedSubmission.getFile().toURI();
                Desktop.getDesktop().browse(fileUri);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot open the attached file.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No file attached to this submission.");
        }
    }

    private Submission getSubmissionFromTableRow(int row) {
        List<Assignment> assignments = assignmentService.getAssignmentsByTeacher(teacher);
        int count = 0;
        for (Assignment a : assignments) {
            List<Submission> subs = assignmentService.getSubmissionsForAssignment(a);
            for (Submission s : subs) {
                if (count == row) return s;
                count++;
            }
        }
        return null;
    }
}
