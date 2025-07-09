package org.app.gui;

import org.app.service.UserService;

import javax.swing.*;
import java.awt.*;

public class EmailVerificationDialog extends JDialog {
    private final UserService userService;
    private final String userEmail;
    private JTextField codeField;
    private boolean verified = false;

    public EmailVerificationDialog(JFrame parent, UserService userService, String email) {
        super(parent, "Email Verification", true);
        this.userService = userService;
        this.userEmail = email;

        initializeComponents();
        setupLayout();
        setupEventHandlers();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(750, 550);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initializeComponents() {
        codeField = new JTextField(20);
        codeField.setPreferredSize(new Dimension(500, 60));
        codeField.setFont(new Font("Arial", Font.BOLD, 24));
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(33, 150, 243), 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Email Verification", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Content panel with vertical layout
        JPanel contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.Y_AXIS));
        contentWrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 40, 40));

        // Info label
        JLabel infoLabel = new JLabel("<html><center><div style='font-size: 14px; line-height: 1.5;'>"
                + "A verification code has been sent to:<br><br>"
                + "<b style='font-size: 16px; color: #2196F3;'>" + userEmail + "</b><br><br>"
                + "Please enter the 6-digit verification code below:<br><br>"
                + "<i style='color: #666;'>Check your email inbox and spam folder</i>"
                + "</div></center></html>", SwingConstants.CENTER);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoLabel.setMaximumSize(new Dimension(700, 150));

        contentWrapper.add(infoLabel);
        contentWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        // Code label
        JLabel codeLabel = new JLabel("Verification Code:");
        codeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentWrapper.add(codeLabel);
        contentWrapper.add(Box.createRigidArea(new Dimension(0, 10)));

        // Code input field
        codeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        contentWrapper.add(codeField);
        contentWrapper.add(Box.createRigidArea(new Dimension(0, 30)));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        JButton verifyButton = new JButton("Verify Code");
        verifyButton.setPreferredSize(new Dimension(140, 45));
        verifyButton.setFont(new Font("Arial", Font.BOLD, 14));
        verifyButton.setBackground(new Color(76, 175, 80));
        verifyButton.setForeground(Color.WHITE);
        verifyButton.setFocusPainted(false);
        verifyButton.addActionListener(e -> handleVerification());

        JButton resendButton = new JButton("Resend Code");
        resendButton.setPreferredSize(new Dimension(140, 45));
        resendButton.setFont(new Font("Arial", Font.BOLD, 14));
        resendButton.setBackground(new Color(255, 152, 0));
        resendButton.setForeground(Color.WHITE);
        resendButton.setFocusPainted(false);
        resendButton.addActionListener(e -> handleResendCode());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(140, 45));
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(244, 67, 54));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(verifyButton);
        buttonPanel.add(resendButton);
        buttonPanel.add(cancelButton);

        contentWrapper.add(buttonPanel);

        // Add everything to dialog
        add(headerPanel, BorderLayout.NORTH);
        add(contentWrapper, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        codeField.addActionListener(e -> handleVerification());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> codeField.requestFocusInWindow());
            }
        });
    }

    private void handleVerification() {
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the verification code.",
                    "Missing Code",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userService.verifyEmail(userEmail, code)) {
            verified = true;
            JOptionPane.showMessageDialog(this,
                    "Email verified successfully!",
                    "Verification Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid verification code. Please try again.",
                    "Verification Failed",
                    JOptionPane.ERROR_MESSAGE);
            codeField.selectAll();
            codeField.requestFocus();
        }
    }

    private void handleResendCode() {
        try {
            userService.resendVerificationCode(userEmail);
            JOptionPane.showMessageDialog(this,
                    "A new verification code has been sent to " + userEmail,
                    "Code Resent",
                    JOptionPane.INFORMATION_MESSAGE);
            codeField.setText("");
            codeField.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to resend verification code: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isVerified() {
        return verified;
    }
}
