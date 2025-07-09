package org.app.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class EmailUtil {
    private static Properties emailConfig;

    static {
        loadEmailConfig();
    }

    private static void loadEmailConfig() {
        emailConfig = new Properties();
        try {
            emailConfig.load(new FileInputStream("email.properties"));
        } catch (IOException e) {
            System.err.println("Warning: Could not load email.properties. Email functionality will not work.");
            System.err.println("Please create email.properties file with your email credentials.");
        }
    }

    public static void sendEmail(String to, String subject, String content) throws MessagingException {
        if (emailConfig == null || emailConfig.isEmpty()) {
            throw new MessagingException("Email configuration not loaded. Please check email.properties file.");
        }

        String username = emailConfig.getProperty("email.username");
        String password = emailConfig.getProperty("email.password");

        if (username == null || password == null ||
            username.equals("your_email@gmail.com") ||
            password.equals("your_app_password_here")) {
            throw new MessagingException("Please update email.properties with your actual email credentials.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", emailConfig.getProperty("smtp.auth"));
        props.put("mail.smtp.starttls.enable", emailConfig.getProperty("smtp.starttls.enable"));
        props.put("mail.smtp.host", emailConfig.getProperty("smtp.host"));
        props.put("mail.smtp.port", emailConfig.getProperty("smtp.port"));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
    }

    public static void sendVerificationEmail(String to, String code) throws MessagingException {
        String subject = emailConfig.getProperty("email.verification.subject", "Verify your email");
        String body = emailConfig.getProperty("email.verification.body", "Your verification code is: {CODE}");
        body = body.replace("{CODE}", code);
        sendEmail(to, subject, body);
    }
}
