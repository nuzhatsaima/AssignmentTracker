package org.app.service;

import org.app.model.*;
import org.app.util.SimpleDataPersistence;
import org.app.util.EmailUtil;
import javax.mail.MessagingException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Management Service for BUP UCAM Assignment Tracker
 */
public class UserService {
    private Map<String, User> users;
    private Map<String, Teacher> teachers;
    private Map<String, Student> students;
    private int userCounter;
    private SimpleDataPersistence dataPersistence;

    public UserService() {
        this.dataPersistence = new SimpleDataPersistence();
        this.users = new HashMap<>();
        this.teachers = new HashMap<>();
        this.students = new HashMap<>();
        loadData();
    }

    private void loadData() {
        SimpleDataPersistence.AppSettings settings = dataPersistence.loadSettings();
        this.userCounter = settings.userCounter;

        Map<String, SimpleDataPersistence.SimpleUserData> userData = dataPersistence.loadUserCredentials();

        System.out.println("Converting " + userData.size() + " user credentials to User objects...");

        for (Map.Entry<String, SimpleDataPersistence.SimpleUserData> entry : userData.entrySet()) {
            SimpleDataPersistence.SimpleUserData simpleUser = entry.getValue();
            User user = simpleUser.toUser();
            if (user != null) {
                users.put(user.getUserId(), user);
                if (user instanceof Teacher) {
                    teachers.put(user.getUserId(), (Teacher) user);
                    System.out.println("✓ Loaded teacher: " + user.getName() + " (" + user.getEmail() + ")");
                } else if (user instanceof Student) {
                    students.put(user.getUserId(), (Student) user);
                    System.out.println("✓ Loaded student: " + user.getName() + " (" + user.getEmail() + ")");
                }
            } else {
                System.err.println("✗ Failed to convert user: " + simpleUser.name);
            }
        }

        System.out.println("✓ Successfully loaded " + users.size() + " users from persistent storage");
        System.out.println("  - Teachers: " + teachers.size());
        System.out.println("  - Students: " + students.size());
    }

    private void saveData() {
        // Convert users to simple format
        Map<String, SimpleDataPersistence.SimpleUserData> userData = new HashMap<>();
        for (User user : users.values()) {
            userData.put(user.getUserId(), new SimpleDataPersistence.SimpleUserData(user));
        }

        // Save user data
        dataPersistence.saveUserCredentials(userData);

        // Save settings
        SimpleDataPersistence.AppSettings settings = new SimpleDataPersistence.AppSettings();
        settings.userCounter = userCounter;
        settings.firstRun = false;
        dataPersistence.saveSettings(settings);

        System.out.println("✓ Saved " + users.size() + " users to persistent storage");
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }

    /**
     * Register a new teacher with email verification
     */
    public Teacher registerTeacher(String name, String email, String password,
                                   String department, String employeeId) {
        String userId = "TCH-" + String.format("%04d", userCounter++);
        Teacher teacher = new Teacher(userId, name, email, password, department, employeeId);
        teacher.setEmailVerified(false);
        String code = generateVerificationCode();
        teacher.setEmailVerificationCode(code);
        users.put(userId, teacher);
        teachers.put(userId, teacher);
        saveData();
        // Send verification email
        try {
            EmailUtil.sendVerificationEmail(email, code);
        } catch (MessagingException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
        System.out.println("✓ Teacher registered (verification required): " + name);
        return teacher;
    }

    /**
     * Register a new student with email verification
     */
    public Student registerStudent(String name, String email, String password,
                                   String studentId, String program, int semester) {
        String userId = "STD-" + String.format("%04d", userCounter++);
        Student student = new Student(userId, name, email, password, studentId, program, semester);
        student.setEmailVerified(false);
        String code = generateVerificationCode();
        student.setEmailVerificationCode(code);
        users.put(userId, student);
        students.put(userId, student);
        saveData();
        // Send verification email
        try {
            EmailUtil.sendVerificationEmail(email, code);
        } catch (MessagingException e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
        }
        System.out.println("✓ Student registered (verification required): " + name);
        return student;
    }

    /**
     * Authenticate user login
     */
    public User authenticateUser(String email, String password) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email) && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find user by email
     */
    public User findUserByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all teachers
     */
    public List<Teacher> getAllTeachers() {
        return new ArrayList<>(teachers.values());
    }

    /**
     * Get all students
     */
    public List<Student> getAllStudents() {
        return new ArrayList<>(students.values());
    }

    /**
     * Get teachers by department
     */
    public List<Teacher> getTeachersByDepartment(String department) {
        return teachers.values().stream()
                .filter(teacher -> teacher.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    /**
     * Get students by program
     */
    public List<Student> getStudentsByProgram(String program) {
        return students.values().stream()
                .filter(student -> student.getProgram().equalsIgnoreCase(program))
                .collect(Collectors.toList());
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(String userId, String name, String email) {
        User user = users.get(userId);
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            saveData(); // Save after update
            System.out.println("✓ User profile updated successfully");
        }
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setActive(false);
            saveData(); // Save after deactivation
            System.out.println("✓ User deactivated: " + user.getName());
        }
    }

    /**
     * Display user statistics
     */
    public void displayUserStatistics() {
        System.out.println("=== User Statistics ===");
        System.out.println("Total Users: " + users.size());
        System.out.println("Teachers: " + teachers.size());
        System.out.println("Students: " + students.size());
        System.out.println("Active Users: " + users.values().stream()
                .mapToInt(user -> user.isActive() ? 1 : 0).sum());
    }

    /**
     * Check if this is the first run (no saved data exists)
     */
    public boolean isFirstRun() {
        return !dataPersistence.dataExists();
    }

    /**
     * Verify user email with code
     */
    public boolean verifyEmail(String email, String code) {
        User user = findUserByEmail(email);
        if (user != null && user.getEmailVerificationCode() != null &&
            user.getEmailVerificationCode().equals(code)) {
            user.setEmailVerified(true);
            user.setEmailVerificationCode(null); // Clear code after verification
            saveData();
            System.out.println("✓ Email verified for user: " + user.getName());
            return true;
        }
        return false;
    }

    /**
     * Resend verification code
     */
    public void resendVerificationCode(String email) {
        User user = findUserByEmail(email);
        if (user != null && !user.isEmailVerified()) {
            String newCode = generateVerificationCode();
            user.setEmailVerificationCode(newCode);
            saveData();

            try {
                EmailUtil.sendVerificationEmail(email, newCode);
                System.out.println("✓ Verification code resent to: " + email);
            } catch (MessagingException e) {
                System.err.println("Failed to resend verification email: " + e.getMessage());
            }
        }
    }

    // Getters
    public User getUser(String userId) {
        return users.get(userId);
    }

    public Teacher getTeacher(String userId) {
        return teachers.get(userId);
    }

    public Student getStudent(String userId) {
        return students.get(userId);
    }

    /**
     * Delete user completely from the system by email
     */
    public boolean deleteUserByEmail(String email) {
        User userToDelete = findUserByEmail(email);
        if (userToDelete != null) {
            return deleteUser(userToDelete.getUserId());
        }
        return false;
    }

    /**
     * Delete user completely from the system by user ID
     */
    public boolean deleteUser(String userId) {
        User user = users.get(userId);
        if (user != null) {
            // Remove from main users map
            users.remove(userId);

            // Remove from role-specific map
            if (user instanceof Teacher) {
                teachers.remove(userId);
                System.out.println("✓ Teacher deleted: " + user.getName() + " (" + user.getEmail() + ")");
            } else if (user instanceof Student) {
                students.remove(userId);
                System.out.println("✓ Student deleted: " + user.getName() + " (" + user.getEmail() + ")");
            }

            // Save changes to persistent storage
            saveData();
            return true;
        }
        System.out.println("✗ User not found with ID: " + userId);
        return false;
    }
}