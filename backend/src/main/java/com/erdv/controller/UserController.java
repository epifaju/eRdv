package com.erdv.controller;

import com.erdv.dto.ChangePasswordRequest;
import com.erdv.dto.UpdateProfileRequest;
import com.erdv.dto.UserProfileResponse;
import com.erdv.entity.Utilisateur;
import com.erdv.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe() {
        Utilisateur u = currentUser();
        return ResponseEntity.ok(UserProfileResponse.from(u));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        Utilisateur u = currentUser();
        return ResponseEntity.ok(utilisateurService.updateProfile(u.getId(), request));
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Utilisateur u = currentUser();
        utilisateurService.changePassword(u.getId(), request);
    }

    private static Utilisateur currentUser() {
        return (Utilisateur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
