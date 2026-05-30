package com.erdv.dto;

public class AuthResponse {

    private String token;
    /** Jeton longue durée pour POST /auth/refresh */
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String nom;
    private String email;
    private String role;
    private Long prestataireId;

    public AuthResponse(String token, String refreshToken, Long id, String nom, String email, String role,
            Long prestataireId) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.prestataireId = prestataireId;
    }

    // Constructeurs
    public AuthResponse() {
    }

    public AuthResponse(String token, String refreshToken, Long id, String nom, String email, String role) {
        this(token, refreshToken, id, nom, email, role, null);
    }

    public AuthResponse(String token, Long id, String nom, String email, String role) {
        this(token, null, id, nom, email, role);
    }

    // Getters et Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

    public Long getPrestataireId() {
        return prestataireId;
    }

    public void setPrestataireId(Long prestataireId) {
        this.prestataireId = prestataireId;
    }
}