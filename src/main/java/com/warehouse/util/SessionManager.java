package com.warehouse.util;

import com.warehouse.model.User;

/**
 * Holds the current session state (logged-in user).
 * Singleton pattern — one session per application instance.
 */
public class SessionManager {

    private static User currentUser;

    private SessionManager() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }

    public static String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "";
    }

    public static boolean isAdmin() {
        return "Admin".equals(getCurrentRole());
    }

    public static boolean isStaff() {
        return "Staff".equals(getCurrentRole());
    }

    public static boolean isManager() {
        return "Manager".equals(getCurrentRole());
    }

    public static void logout() {
        currentUser = null;
    }
}
