package org.app;

import org.app.service.UserService;
import org.app.model.User;

/**
 * Simple console application to delete the user lasifarahman@gmail.com
 */
public class DeleteUserMain {

    public static void main(String[] args) {
        System.out.println("=== User Deletion Tool ===");
        System.out.println("Initializing Assignment Tracker system...");

        // Initialize UserService
        UserService userService = new UserService();

        // Email of the user to delete
        String emailToDelete = "lasifarahman@gmail.com";

        System.out.println("\nAttempting to delete user: " + emailToDelete);

        // Check if user exists first
        User userToDelete = userService.findUserByEmail(emailToDelete);
        if (userToDelete == null) {
            System.out.println("✗ User with email '" + emailToDelete + "' not found in the system.");
            System.out.println("The user might have already been deleted or never existed.");
            return;
        }

        // Show user details before deletion
        System.out.println("Found user: " + userToDelete.getName() + " (" + userToDelete.getRole() + ")");
        System.out.println("User ID: " + userToDelete.getUserId());

        // Delete the user
        boolean deleted = userService.deleteUserByEmail(emailToDelete);

        if (deleted) {
            System.out.println("\n✓ SUCCESS: User '" + emailToDelete + "' has been completely deleted!");
            System.out.println("The user has been removed from:");
            System.out.println("  - Main user database");
            System.out.println("  - Role-specific collections");
            System.out.println("  - Persistent storage");
        } else {
            System.out.println("\n✗ FAILED: Could not delete user '" + emailToDelete + "'");
        }

        // Display updated statistics
        System.out.println("\n=== Updated System Statistics ===");
        userService.displayUserStatistics();
    }
}
