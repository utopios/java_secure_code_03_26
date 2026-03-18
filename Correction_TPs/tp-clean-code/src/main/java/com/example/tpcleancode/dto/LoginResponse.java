package com.example.tpcleancode.dto;

/**
 * DTO pour la reponse de login.
 *
 * CORRECTION Securite :
 * - Ne contient PAS le hash du mot de passe (contrairement a la version vulnérable)
 * - Ne contient que les informations strictement necessaires
 */
public class LoginResponse {

    private String email;
    private String role;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(String email, String role, String message) {
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
