package com.hms.dto;

public class AuthResponse {
    private String token;
    private String message;
    private boolean success;
    private String role; // Just for convenience
    private String username;

    public AuthResponse(String token, String message, boolean success, String role, String username) {
        this.token = token;
        this.message = message;
        this.success = success;
        this.role = role;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
