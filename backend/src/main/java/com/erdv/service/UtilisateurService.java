package com.erdv.service;

import com.erdv.dto.ChangePasswordRequest;
import com.erdv.dto.UpdateProfileRequest;
import com.erdv.dto.UserProfileResponse;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));
    }

    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Un utilisateur avec cet email existe déjà");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        Utilisateur u = getUtilisateurById(userId);
        if (!u.getEmail().equalsIgnoreCase(request.getEmail())
                && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }
        u.setNom(request.getNom());
        u.setEmail(request.getEmail());
        u.setTelephone(request.getTelephone());
        return UserProfileResponse.from(utilisateurRepository.save(u));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        Utilisateur u = getUtilisateurById(userId);
        if (!passwordEncoder.matches(request.getMotDePasseActuel(), u.getMotDePasse())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }
        if (request.getMotDePasseActuel().equals(request.getNouveauMotDePasse())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le nouveau mot de passe doit être différent de l'actuel");
        }
        u.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(u);
    }

    public Utilisateur updateUtilisateur(Long id, Utilisateur utilisateurDetails) {
        Utilisateur utilisateur = getUtilisateurById(id);

        utilisateur.setNom(utilisateurDetails.getNom());
        utilisateur.setTelephone(utilisateurDetails.getTelephone());

        if (utilisateurDetails.getMotDePasse() != null && !utilisateurDetails.getMotDePasse().isEmpty()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateurDetails.getMotDePasse()));
        }

        return utilisateurRepository.save(utilisateur);
    }

    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }
}
