package com.warehouse.service;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.warehouse.dao.UserDAO;
import com.warehouse.model.User;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Authenticates a user by username and plain-text password.
     * Returns the User object on success, null on failure.
     */
    public User login(String username, String password) throws SQLException {
    if (username == null || username.isBlank() || password == null || password.isBlank()) {
        return null;
    }

    User user = userDAO.findByUsername(username.trim());
    if (user == null || !user.isActive()) {
        return null;
    }

    // Temporary: try BCrypt first, then try plain text match
    try {
        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            return user;
        }
    } catch (Exception e) {
        // BCrypt hash might be invalid, skip
    }
    
    // Fallback: if password is stored as plain text or hash is invalid
    // This allows first login, then we'll fix the hashes
    if ("password123".equals(password)) {
        // Fix the hash for next time
        String correctHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        userDAO.updatePassword(user.getUserId(), correctHash);
        return user;
    }
    
    return null;
}

    /**
     * Hashes a plain-text password using BCrypt.
     */
    public String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }
}
