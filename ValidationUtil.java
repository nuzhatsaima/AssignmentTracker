package org.app.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final String EMAIL_PATTERN =
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern EMAIL_REGEX = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_REGEX.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static String validateRegistrationInput(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }
        if (!isValidEmail(email)) {
            return "Please enter a valid email address";
        }
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters long";
        }
        return null; // No errors
    }
}
