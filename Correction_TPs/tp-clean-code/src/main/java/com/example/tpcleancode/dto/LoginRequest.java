package com.example.tpcleancode.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la requete de login.
 *
 * CORRECTIONS Clean Code :
 * - Noms explicites : email et password (au lieu de u et p)
 * - Champs prives avec validation Jakarta
 * - Annotations @NotBlank pour empecher les valeurs vides
 *
 * CORRECTIONS Securite :
 * - Validation stricte cote serveur
 * - Le mot de passe n'est jamais serialise dans les reponses
 */
public class LoginRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
