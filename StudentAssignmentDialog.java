package org.app.gui;

import org.app.model.*;
import org.app.service.AssignmentService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Student Assignment Dialog for viewing assignment details and teacher attachments
 */
public class StudentAssignmentDialog extends JDialog {
    private Assignment assignment;
    private Student student;
    private AssignmentService assignmentService;
    private boolean submissionDialogOpened = false;

    private JTextArea descriptionArea;
    private JList<String> attachmentsList;
    private DefaultListModel<String> attachmentsModel;
    private JButton viewFileButton;
    private JButton downloadFileButton;
    private JButton submitAssignmentButton;

    public StudentAssignmentDialog(JFrame parent, Assignment assignment, Student student,
                                   AssignmentService assignmentService) {
        super(parent, "Assignment Details", true);
        this.assignment = assignment;
        this.student = student;
        this.assignmentService = assignmentService;

        initializeComponents();
        setupLayout();
        loadAssignmentData();

        setSize(700, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        descriptionArea = new JTextArea(8, 50);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        attachmentsModel = new DefaultListModel<>();
        attachmentsList = new JList<>(attachmentsModel);
        attachmentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attachmentsList.setBorder(BorderFactory.createTitledBorder("Question Files from Teacher"));
        attachmentsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = attachmentsList.getSelectedIndex() != -1;
                viewFileButton.setEnabled(hasSelection);
                downloadFileButton.setEnabled(hasSelection);
            }
        });

        viewFileButton = new JButton("View File");
        viewFileButton.setEnabled(false);
        viewFileButton.addActionListener(e -> viewSelectedFile());

        downloadFileButton = new JButton("Download File");
        downloadFileButton.setEnabled(false);
        downloadFileButton.addActionListener(e -> downloadSelectedFile());

        submitAssignmentButton = new JButton("Submit Assignment");
        submitAssignmentButton.setBackground(new Color(76, 175, 80));
        submitAssignmentButton.setForeground(Color.WHITE);
        submitAssignmentButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitAssignmentButton.addActionListener(e -> openSubmissionDialog());

        // Check if student has already submitted
        List<Submission> studentSubmissions = assignmentService.getSubmissionsByStudent(student);
        boolean hasSubmitted = studentSubmissions.stream()
                .anyMatch(sub -> sub.getAssignment().equals(assignment));

        if (hasSubmitted) {
            submitAssignmentButton.setText("Already Submitted");
            submitAssignmentButton.setEnabled(false);
            submitAssignmentButton.setBackground(Color.GRAY);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(assignment.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        // Info Panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Course info
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(assignment.getCourse().getCourseCode() + " - " + assignment.getCourse().getCourseName()), gbc);

        // Due date
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JLabel dueDateLabel = new JLabel(assignment.getDueDate().format(formatter));
        if (assignment.isOverdue()) {
            dueDateLabel.setForeground(Color.RED);
            dueDateLabel.setText(dueDateLabel.getText() + " (OVERDUE)");
        }
        infoPanel.add(dueDateLabel, gbc);

        // Max marks
        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("Maximum Marks:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(String.valueOf(assignment.getMaxMarks())), gbc);

        // Type
        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(new JLabel("Type:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(new JLabel(assignment.getType().toString()), gbc);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Description
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder("Assignment Description"));
        descPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        // Attachments
        JPanel attachPanel = new JPanel(new BorderLayout());
        attachPanel.setBorder(BorderFactory.createTitledBorder("Question Files"));
        attachPanel.add(new JScrollPane(attachmentsList), BorderLayout.CENTER);

        JPanel attachButtonPanel = new JPanel(new FlowLayout());
        attachButtonPanel.add(viewFileButton);
        attachButtonPanel.add(downloadFileButton);
        attachPanel.add(attachButtonPanel, BorderLayout.SOUTH);

        // Split pane for description and attachments
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, descPanel, attachPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.4);

        contentPanel.add(splitPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitAssignmentButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadAssignmentData() {
        // Load description
        descriptionArea.setText(assignment.getDescription());

        // Load attachments
        List<String> attachments = assignment.getAttachmentPaths();
        attachmentsModel.clear();
        for (String filePath : attachments) {
            File file = new File(filePath);
            if (file.exists()) {
                attachmentsModel.addElement(file.getName());
            } else {
                attachmentsModel.addElement(file.getName() + " (File not found)");
            }
        }
    }

    private void viewSelectedFile() {
        int selectedIndex = attachmentsList.getSelectedIndex();
        if (selectedIndex != -1) {
            String filePath = assignment.getAttachmentPaths().get(selectedIndex);
            File file = new File(filePath);

            if (file.exists()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this,
                        "Cannot open file: " + e.getMessage(),
                        "File Open Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "File not found: " + file.getName(),
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void downloadSelectedFile() {
        int selectedIndex = attachmentsList.getSelectedIndex();
        if (selectedIndex != -1) {
            String filePath = assignment.getAttachmentPaths().get(selectedIndex);
            File sourceFile = new File(filePath);

            if (sourceFile.exists()) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save File As");
                fileChooser.setSelectedFile(new File(sourceFile.getName()));

                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File destinationFile = fileChooser.getSelectedFile();
                    try {
                        java.nio.file.Files.copy(sourceFile.toPath(), destinationFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(this,
                            "File downloaded successfully!",
                            "Download Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(this,
                            "Error downloading file: " + e.getMessage(),
                            "Download Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "File not found: " + sourceFile.getName(),
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openSubmissionDialog() {
        // Implementation to open submission dialog
        submissionDialogOpened = true;
        dispose();
    }

    public boolean isSubmissionDialogOpened() {
        return submissionDialogOpened;
    }
}
