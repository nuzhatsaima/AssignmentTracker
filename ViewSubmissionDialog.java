package org.app.gui;

import org.app.model.*;
import org.app.service.AssignmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog for viewing and grading student submissions
 */
public class ViewSubmissionDialog extends JDialog {
    private AssignmentService assignmentService;
    private Teacher teacher;
    private Submission submission;
    private Assignment assignment;
    private Student student;
    private boolean success = false;

    private JTextArea contentArea;
    private JTextField marksField;
    private JTextArea feedbackArea;
    private JList<String> attachmentsList;
    private DefaultListModel<String> attachmentsModel;
    private JButton viewFileButton;
    private JButton downloadFileButton;
    private JButton gradeButton;

    public ViewSubmissionDialog(JFrame parent, Teacher teacher, Submission submission,
                               AssignmentService assignmentService) {
        super(parent, "View Submission", true);
        this.teacher = teacher;
        this.submission = submission;
        this.assignment = submission.getAssignment();
        this.student = submission.getStudent();
        this.assignmentService = assignmentService;

        initializeComponents();
        setupLayout();

        setSize(800, 750);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // Content area to display submission content
        contentArea = new JTextArea(8, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setText(submission.getContent());
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Submission Content"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Student attachments list
        attachmentsModel = new DefaultListModel<>();

        // Use the correct method getAttachmentPaths() instead of getAttachments()
        List<String> attachmentPaths = submission.getAttachmentPaths();
        if (attachmentPaths != null && !attachmentPaths.isEmpty()) {
            for (String attachment : attachmentPaths) {
                attachmentsModel.addElement(attachment);
            }
        }

        attachmentsList = new JList<>(attachmentsModel);
        attachmentsList.setBorder(BorderFactory.createTitledBorder("Submission Files"));
        attachmentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Buttons for file handling
        viewFileButton = new JButton("View Selected File");
        downloadFileButton = new JButton("Download Selected File");
        viewFileButton.setEnabled(attachmentsModel.size() > 0);
        downloadFileButton.setEnabled(attachmentsModel.size() > 0);

        // Grading components
        marksField = new JTextField(5);
        marksField.setText(submission.getMarks() != null ? submission.getMarks().toString() : "");

        feedbackArea = new JTextArea(4, 30);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setText(submission.getFeedback() != null ? submission.getFeedback() : "");
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Feedback"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Buttons
        gradeButton = new JButton("Submit Grade");
        gradeButton.setBackground(new Color(33, 150, 243));
        gradeButton.setForeground(Color.WHITE);

        JButton cancelButton = new JButton("Close");

        // Add event listeners
        gradeButton.addActionListener(e -> {
            try {
                int marks = Integer.parseInt(marksField.getText().trim());
                if (marks < 0 || marks > assignment.getMaxMarks()) {
                    JOptionPane.showMessageDialog(this,
                            "Marks must be between 0 and " + assignment.getMaxMarks(),
                            "Invalid Marks", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String feedback = feedbackArea.getText().trim();
                assignmentService.gradeSubmission(submission.getSubmissionId(), marks, feedback, teacher);
                success = true;
                JOptionPane.showMessageDialog(this,
                        "Assignment graded successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number for marks",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());

        viewFileButton.addActionListener(e -> {
            String selectedFile = attachmentsList.getSelectedValue();
            if (selectedFile != null) {
                try {
                    // Create a File object from the file path
                    java.io.File file = new java.io.File(selectedFile);

                    if (file.exists()) {
                        // Try to open the file with the system's default application
                        java.awt.Desktop.getDesktop().open(file);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "File not found: " + file.getAbsolutePath(),
                                "File Not Found", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (java.io.IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error opening file: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } catch (java.awt.HeadlessException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot open files in this environment.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a file to view",
                        "No File Selected", JOptionPane.WARNING_MESSAGE);
            }
        });

        downloadFileButton.addActionListener(e -> {
            String selectedFile = attachmentsList.getSelectedValue();
            if (selectedFile != null) {
                try {
                    // Create a File object from the file path
                    java.io.File sourceFile = new java.io.File(selectedFile);

                    if (sourceFile.exists()) {
                        // Create a file chooser for selecting destination
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Save File As");
                        fileChooser.setSelectedFile(new java.io.File(sourceFile.getName()));

                        int userSelection = fileChooser.showSaveDialog(this);

                        if (userSelection == JFileChooser.APPROVE_OPTION) {
                            java.io.File destFile = fileChooser.getSelectedFile();

                            // Copy the file
                            java.nio.file.Files.copy(
                                sourceFile.toPath(),
                                destFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );

                            JOptionPane.showMessageDialog(this,
                                "File downloaded successfully to:\n" + destFile.getAbsolutePath(),
                                "Download Complete", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "File not found: " + sourceFile.getAbsolutePath(),
                            "File Not Found", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error downloading file: " + ex.getMessage(),
                        "Download Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a file to download",
                    "No File Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel with assignment and student info
        JPanel headerPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        headerPanel.setBorder(BorderFactory.createTitledBorder("Assignment Information"));

        headerPanel.add(new JLabel("Assignment:"));
        headerPanel.add(new JLabel(assignment.getTitle()));

        headerPanel.add(new JLabel("Course:"));
        headerPanel.add(new JLabel(assignment.getCourse().getCourseCode() + " - " +
                                 assignment.getCourse().getCourseName()));

        headerPanel.add(new JLabel("Due Date:"));
        headerPanel.add(new JLabel(assignment.getDueDate().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));

        headerPanel.add(new JLabel("Student:"));
        headerPanel.add(new JLabel(student.getName() + " (" + student.getStudentId() + ")"));

        headerPanel.add(new JLabel("Submitted:"));
        headerPanel.add(new JLabel(submission.getSubmittedAt().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));

        headerPanel.add(new JLabel("Status:"));
        headerPanel.add(new JLabel(submission.getStatus().toString()));

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        // Attachments panel
        JPanel attachmentsPanel = new JPanel(new BorderLayout(5, 5));
        attachmentsPanel.add(new JScrollPane(attachmentsList), BorderLayout.CENTER);

        JPanel attachmentButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        attachmentButtonsPanel.add(viewFileButton);
        attachmentButtonsPanel.add(downloadFileButton);
        attachmentsPanel.add(attachmentButtonsPanel, BorderLayout.SOUTH);

        // Grading panel
        JPanel gradingPanel = new JPanel(new BorderLayout(5, 5));
        gradingPanel.setBorder(BorderFactory.createTitledBorder("Grading"));

        JPanel marksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        marksPanel.add(new JLabel("Marks (out of " + assignment.getMaxMarks() + "):"));
        marksPanel.add(marksField);

        gradingPanel.add(marksPanel, BorderLayout.NORTH);
        gradingPanel.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);

        // Bottom button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(gradeButton);
        buttonPanel.add(new JButton("Close"));

        // Combine panels
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.add(contentPanel);
        centerPanel.add(attachmentsPanel);
        centerPanel.add(gradingPanel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public boolean isSuccess() {
        return success;
    }
}
