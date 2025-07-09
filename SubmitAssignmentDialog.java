package org.app.gui;

import org.app.model.*;
import org.app.service.AssignmentService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Submit Assignment Dialog for Students
 */
public class SubmitAssignmentDialog extends JDialog {
    private AssignmentService assignmentService;
    private Student student;
    private List<Course> courses;
    private boolean success = false;

    private JComboBox<Assignment> assignmentComboBox;
    private JTextArea contentArea;
    private JLabel assignmentDetailsLabel;
    private JList<String> attachmentsList;
    private DefaultListModel<String> attachmentsModel;
    private JButton addFileButton;
    private JButton removeFileButton;
    private List<String> selectedFiles;

    // Teacher attachments components
    private JList<String> teacherAttachmentsList;
    private DefaultListModel<String> teacherAttachmentsModel;
    private JButton viewTeacherFileButton;
    private JButton downloadTeacherFileButton;

    public SubmitAssignmentDialog(JFrame parent, Student student, List<Course> courses,
                                  AssignmentService assignmentService) {
        super(parent, "Submit Assignment", true);
        this.student = student;
        this.courses = courses;
        this.assignmentService = assignmentService;
        this.selectedFiles = new ArrayList<>();

        initializeComponents();
        setupLayout();

        setSize(800, 750); // Increased size to accommodate teacher attachments
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        // Get all available assignments from student's courses
        List<Assignment> availableAssignments = courses.stream()
                .flatMap(course -> assignmentService.getAssignmentsByCourse(course).stream())
                .filter(assignment -> assignment.getStatus() == AssignmentStatus.ACTIVE)
                .filter(assignment -> {
                    // Check if student hasn't already submitted
                    List<Submission> studentSubmissions = assignmentService.getSubmissionsByStudent(student);
                    return studentSubmissions.stream()
                            .noneMatch(sub -> sub.getAssignment().equals(assignment));
                })
                .toList();

        assignmentComboBox = new JComboBox<>(availableAssignments.toArray(new Assignment[0]));
        assignmentComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Assignment) {
                    Assignment assignment = (Assignment) value;
                    setText(assignment.getTitle() + " (" + assignment.getCourse().getCourseCode() + ")");
                }
                return this;
            }
        });

        contentArea = new JTextArea(8, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        assignmentDetailsLabel = new JLabel("<html><i>Select an assignment to view details</i></html>");
        assignmentDetailsLabel.setVerticalAlignment(SwingConstants.TOP);

        // Student attachments list
        attachmentsModel = new DefaultListModel<>();
        attachmentsList = new JList<>(attachmentsModel);
        attachmentsList.setBorder(BorderFactory.createTitledBorder("Your Submission Files"));
        attachmentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Initialize teacher attachments model and list BEFORE any method calls that might use them
        teacherAttachmentsModel = new DefaultListModel<>();
        teacherAttachmentsList = new JList<>(teacherAttachmentsModel);
        teacherAttachmentsList.setBorder(BorderFactory.createTitledBorder("Assignment Materials"));
        teacherAttachmentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add listener to update assignment details
        assignmentComboBox.addActionListener(e -> updateAssignmentDetails());

        // Initialize with first assignment if available (moved after all components are initialized)
        if (assignmentComboBox.getItemCount() > 0) {
            updateAssignmentDetails();
        }

        // Buttons for student file attachment
        addFileButton = new JButton("Add File");
        removeFileButton = new JButton("Remove File");
        removeFileButton.setEnabled(false);

        // Buttons for teacher file viewing
        viewTeacherFileButton = new JButton("View Question File");
        downloadTeacherFileButton = new JButton("Download");
        viewTeacherFileButton.setEnabled(false);
        downloadTeacherFileButton.setEnabled(false);

        addFileButton.addActionListener(e -> addFile());
        removeFileButton.addActionListener(e -> removeFile());
        viewTeacherFileButton.addActionListener(e -> viewTeacherFile());
        downloadTeacherFileButton.addActionListener(e -> downloadTeacherFile());

        // Listener to enable/disable remove button
        attachmentsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                removeFileButton.setEnabled(!attachmentsList.isSelectionEmpty());
            }
        });

        // Listener to enable/disable teacher file buttons
        teacherAttachmentsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = !teacherAttachmentsList.isSelectionEmpty();
                viewTeacherFileButton.setEnabled(hasSelection);
                downloadTeacherFileButton.setEnabled(hasSelection);
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(76, 175, 80));
        JLabel titleLabel = new JLabel("Submit Assignment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        headerPanel.add(titleLabel);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Assignment selection
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Select Assignment:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        formPanel.add(assignmentComboBox, gbc);

        // Assignment details
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        JScrollPane detailsScrollPane = new JScrollPane(assignmentDetailsLabel);
        detailsScrollPane.setPreferredSize(new Dimension(0, 80));
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Assignment Details"));
        formPanel.add(detailsScrollPane, gbc);

        // Content area
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 0.7;
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentScrollPane.setBorder(BorderFactory.createTitledBorder("Your Submission Content"));
        formPanel.add(contentScrollPane, gbc);

        // Attachments
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;
        JScrollPane attachmentsScrollPane = new JScrollPane(attachmentsList);
        attachmentsScrollPane.setBorder(BorderFactory.createTitledBorder("Attachments"));
        formPanel.add(attachmentsScrollPane, gbc);

        // Teacher attachments
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;
        JScrollPane teacherAttachmentsScrollPane = new JScrollPane(teacherAttachmentsList);
        teacherAttachmentsScrollPane.setBorder(BorderFactory.createTitledBorder("Teacher Attachments"));
        formPanel.add(teacherAttachmentsScrollPane, gbc);

        // Buttons panel with both submission and file buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // File attachment buttons
        JPanel fileButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileButtonPanel.add(addFileButton);
        fileButtonPanel.add(removeFileButton);

        // Teacher file action buttons
        JPanel teacherButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        teacherButtonPanel.add(viewTeacherFileButton);
        teacherButtonPanel.add(downloadTeacherFileButton);

        // Main action buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Submit Assignment");
        submitButton.setBackground(new Color(76, 175, 80));
        submitButton.setForeground(Color.WHITE);
        submitButton.setPreferredSize(new Dimension(150, 35));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(244, 67, 54));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(80, 35));

        submitButton.addActionListener(e -> handleSubmission());
        cancelButton.addActionListener(e -> dispose());

        actionButtonPanel.add(submitButton);
        actionButtonPanel.add(cancelButton);

        bottomPanel.add(fileButtonPanel, BorderLayout.WEST);
        bottomPanel.add(teacherButtonPanel, BorderLayout.CENTER);
        bottomPanel.add(actionButtonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateAssignmentDetails() {
        Assignment selectedAssignment = (Assignment) assignmentComboBox.getSelectedItem();
        if (selectedAssignment != null) {
            // Update assignment details
            String details = "<html><body style='width: 400px;'>" +
                    "<b>Course:</b> " + selectedAssignment.getCourse().getCourseCode() + " - " + selectedAssignment.getCourse().getCourseName() + "<br>" +
                    "<b>Type:</b> " + selectedAssignment.getType() + "<br>" +
                    "<b>Due Date:</b> " + selectedAssignment.getDueDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) + "<br>" +
                    "<b>Max Marks:</b> " + selectedAssignment.getMaxMarks() + "<br><br>" +
                    "<b>Description:</b><br>" + selectedAssignment.getDescription().replace("\n", "<br>") +
                    "</body></html>";
            assignmentDetailsLabel.setText(details);

            // Update teacher attachments
            updateTeacherAttachments(selectedAssignment);
        }
    }

    private void updateTeacherAttachments(Assignment assignment) {
        teacherAttachmentsModel.clear();
        List<String> attachments = assignment.getAttachmentPaths();
        for (String filePath : attachments) {
            java.io.File file = new java.io.File(filePath);
            if (file.exists()) {
                teacherAttachmentsModel.addElement(file.getName());
            } else {
                teacherAttachmentsModel.addElement(file.getName() + " (File not found)");
            }
        }
    }

    private void addFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Attach");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            // Add to list model and selected files
            attachmentsModel.addElement(filePath);
            selectedFiles.add(filePath);
        }
    }

    private void removeFile() {
        int selectedIndex = attachmentsList.getSelectedIndex();
        if (selectedIndex != -1) {
            // Remove from list model and selected files
            attachmentsModel.remove(selectedIndex);
            selectedFiles.remove(selectedIndex);
        }
    }

    private void viewTeacherFile() {
        int selectedIndex = teacherAttachmentsList.getSelectedIndex();
        if (selectedIndex != -1) {
            Assignment selectedAssignment = (Assignment) assignmentComboBox.getSelectedItem();
            if (selectedAssignment != null) {
                String filePath = selectedAssignment.getAttachmentPaths().get(selectedIndex);
                java.io.File file = new java.io.File(filePath);

                if (file.exists()) {
                    try {
                        java.awt.Desktop.getDesktop().open(file);
                    } catch (java.io.IOException e) {
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
    }

    private void downloadTeacherFile() {
        int selectedIndex = teacherAttachmentsList.getSelectedIndex();
        if (selectedIndex != -1) {
            Assignment selectedAssignment = (Assignment) assignmentComboBox.getSelectedItem();
            if (selectedAssignment != null) {
                String filePath = selectedAssignment.getAttachmentPaths().get(selectedIndex);
                java.io.File sourceFile = new java.io.File(filePath);

                if (sourceFile.exists()) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save Question File As");
                    fileChooser.setSelectedFile(new java.io.File(sourceFile.getName()));

                    int result = fileChooser.showSaveDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        java.io.File destinationFile = fileChooser.getSelectedFile();
                        try {
                            java.nio.file.Files.copy(sourceFile.toPath(), destinationFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            JOptionPane.showMessageDialog(this,
                                "Question file downloaded successfully!",
                                "Download Complete",
                                JOptionPane.INFORMATION_MESSAGE);
                        } catch (java.io.IOException e) {
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
    }

    private void handleSubmission() {
        Assignment selectedAssignment = (Assignment) assignmentComboBox.getSelectedItem();
        String content = contentArea.getText().trim();

        if (selectedAssignment == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an assignment to submit.",
                    "No Assignment Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your submission content.",
                    "Empty Content",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Submit the assignment
            Submission submission = assignmentService.submitAssignment(selectedAssignment, student, content);

            // Add file attachments to the submission
            for (String filePath : selectedFiles) {
                submission.addAttachment(filePath);
            }

            success = true;
            JOptionPane.showMessageDialog(this,
                    "Assignment submitted successfully!",
                    "Submission Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Submission failed: " + e.getMessage(),
                    "Submission Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
