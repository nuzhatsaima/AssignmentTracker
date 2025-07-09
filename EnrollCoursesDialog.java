package org.app.gui;

import org.app.model.*;
import org.app.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EnrollCoursesDialog extends JDialog {
    private Student student;
    private CourseService courseService;
    private DefaultTableModel availableCoursesModel;
    private JTable availableCoursesTable;
    private boolean enrollmentSuccess = false;

    public EnrollCoursesDialog(JFrame parent, Student student, CourseService courseService) {
        super(parent, "Enroll in Courses", true);
        this.student = student;
        this.courseService = courseService;

        initializeComponents();
        setupLayout();
        loadAvailableCourses();

        setSize(700, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initializeComponents() {
        String[] columns = {"Course Code", "Course Name", "Department", "Credits", "Teacher", "Enrolled"};
        availableCoursesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        availableCoursesTable = new JTable(availableCoursesModel);
        availableCoursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableCoursesTable.getTableHeader().setBackground(new Color(33, 150, 243));
        availableCoursesTable.getTableHeader().setForeground(Color.WHITE);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Available Courses for Enrollment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Select a course and click 'Enroll' to join:");
        tablePanel.add(infoLabel, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(availableCoursesTable), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton enrollButton = new JButton("Enroll in Selected Course");
        enrollButton.setBackground(new Color(76, 175, 80));
        enrollButton.setForeground(Color.WHITE);
        enrollButton.setPreferredSize(new Dimension(200, 35));
        enrollButton.addActionListener(e -> handleEnrollment());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(255, 152, 0));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> loadAvailableCourses());

        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(244, 67, 54));
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(enrollButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAvailableCourses() {
        availableCoursesModel.setRowCount(0);

        List<Course> allCourses = courseService.getAllCourses();
        List<Course> enrolledCourses = courseService.getCoursesForStudent(student);

        if (allCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No courses are available yet.\nAsk your teachers to create some courses first.",
                    "No Courses Available",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (Course course : allCourses) {
            boolean isEnrolled = enrolledCourses.contains(course);
            String enrolledStatus = isEnrolled ? "✓ Enrolled" : "Not Enrolled";

            availableCoursesModel.addRow(new Object[]{
                    course.getCourseCode(),
                    course.getCourseName(),
                    course.getDepartment(),
                    course.getCreditHours(),
                    course.getInstructor().getName(),
                    enrolledStatus
            });
        }
    }

    private void handleEnrollment() {
        int selectedRow = availableCoursesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to enroll in.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String courseCode = (String) availableCoursesModel.getValueAt(selectedRow, 0);
        String enrolledStatus = (String) availableCoursesModel.getValueAt(selectedRow, 5);

        if (enrolledStatus.equals("✓ Enrolled")) {
            JOptionPane.showMessageDialog(this,
                    "You are already enrolled in this course.",
                    "Already Enrolled",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Find the course object
        Course selectedCourse = courseService.getAllCourses().stream()
                .filter(course -> course.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);

        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this,
                    "Course not found. Please refresh and try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Enroll the student
        try {
            courseService.enrollStudent(selectedCourse.getCourseId(), student);
            enrollmentSuccess = true;

            JOptionPane.showMessageDialog(this,
                    "Successfully enrolled in " + selectedCourse.getCourseName() + "!",
                    "Enrollment Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refresh the table
            loadAvailableCourses();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to enroll in course: " + e.getMessage(),
                    "Enrollment Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isEnrollmentSuccess() {
        return enrollmentSuccess;
    }
}
