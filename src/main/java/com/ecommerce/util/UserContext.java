package com.ecommerce.util;

/**
 * UserContext holds the current user's session details.
 * Stores ID, Name, and Role (ADMIN/CUSTOMER).
 */
public class UserContext {
    private static int currentUserId = -1;
    private static String currentUserName;
    private static String currentUserEmail;
    private static String currentUserRole;
    private static String currentUserLocation;

    public static int getCurrentUserId() { return currentUserId; }
    public static void setCurrentUserId(int userId) { currentUserId = userId; }

    public static String getCurrentUserName() { return currentUserName; }
    public static void setCurrentUserName(String name) { currentUserName = name; }

    public static String getCurrentUserEmail() { return currentUserEmail; }
    public static void setCurrentUserEmail(String email) { currentUserEmail = email; }

    public static String getCurrentUserRole() { return currentUserRole; }
    public static void setCurrentUserRole(String role) { currentUserRole = role; }

    public static String getCurrentUserLocation() { return currentUserLocation; }
    public static void setCurrentUserLocation(String location) { currentUserLocation = location; }

    public static boolean isAdmin() { return "ADMIN".equalsIgnoreCase(currentUserRole); }
    public static boolean isCustomer() { return "CUSTOMER".equalsIgnoreCase(currentUserRole); }
    public static boolean isLoggedIn() { return currentUserId != -1; }

    public static void clear() {
        currentUserId = -1;
        currentUserName = null;
        currentUserEmail = null;
        currentUserRole = null;
        currentUserLocation = null;
    }
}
