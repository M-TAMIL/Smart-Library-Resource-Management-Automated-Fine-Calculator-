package com.library.smartlibrary.dto;

public class LoginResponse {
    private boolean authenticated;
    private String token;
    private String username;
    private String role;
    private String message;

    public LoginResponse() {}

    public LoginResponse(boolean authenticated, String token, String username, String role, String message) {
        this.authenticated = authenticated;
        this.token = token;
        this.username = username;
        this.role = role;
        this.message = message;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
