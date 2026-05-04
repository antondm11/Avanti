package teamavanti.util;

import teamavanti.model.User;

public class SessionManager {
    private static User currentUser = null;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "admin".equals(currentUser.getRol());
    }

    public static boolean isCliente() {
        return currentUser != null && "cliente".equals(currentUser.getRol());
    }

    public static void logout() {
        currentUser = null;
    }
}
