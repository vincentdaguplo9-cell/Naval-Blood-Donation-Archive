package util;

// Lightweight in-memory session for the currently authenticated admin.
public final class AdminSession {
    private static int adminId;
    private static String username;

    private AdminSession() {
    }

    public static void start(int currentAdminId, String currentUsername) {
        adminId = currentAdminId;
        username = currentUsername;
    }

    public static void clear() {
        adminId = 0;
        username = null;
    }

    public static int getAdminId() {
        return adminId;
    }

    public static String getUsername() {
        return username == null ? "" : username;
    }

    public static boolean isLoggedIn() {
        return adminId > 0;
    }
}
