package org.app.util;

import org.app.service.UserService;

/**
 * Utility class for deleting specific users from the Assignment Tracker system
 */
public class UserDeletionUtil {

    public static void main(String[] args) {
        // Initialize UserService
        UserService userService = new UserService();

        // Email of the user to delete
        String emailToDelete = "lasifarahman@gmail.com";

        System.out.println("=== User Deletion Utility ===");
        System.out.println("Attempting to delete user: " + emailToDelete);

        // Check if user exists first
        if (userService.findUserByEmail(emailToDelete) == null) {
            System.out.println("✗ User with email '" + emailToDelete + "' not found in the system.");
            return;
        }

        // Delete the user
        boolean deleted = userService.deleteUserByEmail(emailToDelete);

        if (deleted) {
            System.out.println("✓ Successfully deleted user: " + emailToDelete);
            System.out.println("The user has been completely removed from the system.");
        } else {
            System.out.println("✗ Failed to delete user: " + emailToDelete);
        }

        // Display current user statistics
        System.out.println("\n=== Updated User Statistics ===");
        userService.displayUserStatistics();
    }
}
