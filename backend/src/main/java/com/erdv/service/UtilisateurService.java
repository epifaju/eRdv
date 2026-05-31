package com.erdv.service;

import com.erdv.dto.ChangePasswordRequest;
import com.erdv.dto.DeleteAccountRequest;
import com.erdv.dto.UpdateProfileRequest;
import com.erdv.dto.UserDataExportResponse;
import com.erdv.dto.UserProfileResponse;
import com.erdv.dto.RendezVousResponse;
import com.erdv.entity.Utilisateur;
import com.erdv.exception.ApiException;
import com.erdv.repository.RendezVousRepository;
import com.erdv.repository.UtilisateurRepository;
import com.erdv.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UtilisateurService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final RendezVousRepository rendezVousRepository;
    private final RefreshTokenService refreshTokenService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            RendezVousRepository rendezVousRepository,
            RefreshTokenService refreshTokenService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.rendezVousRepository = rendezVousRepository;
        this.refreshTokenService = refreshTokenService;
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
        if (request.getConsentementSmsRappels() != null) {
            applySmsConsent(u, request.getConsentementSmsRappels(), request.getTelephone());
        } else if (u.isConsentementSmsRappels()
                && SmsService.normalizePhone(request.getTelephone()) == null) {
            u.setConsentementSmsRappels(false);
            u.setConsentementSmsRappelsAt(null);
        }
        return UserProfileResponse.from(utilisateurRepository.save(u));
    }

    private void applySmsConsent(Utilisateur u, boolean consent, String telephone) {
        if (consent && SmsService.normalizePhone(telephone) == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Un numéro de mobile valide est requis pour recevoir des SMS de rappel");
        }
        if (consent == u.isConsentementSmsRappels()) {
            return;
        }
        u.setConsentementSmsRappels(consent);
        u.setConsentementSmsRappelsAt(consent ? Instant.now() : null);
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

    @Transactional(readOnly = true)
    public UserDataExportResponse exportUserData(Long userId) {
        Utilisateur u = getUtilisateurById(userId);
        UserDataExportResponse export = new UserDataExportResponse();
        export.setExportedAt(Instant.now());
        export.setProfil(UserProfileResponse.from(u));
        export.setRendezVous(RendezVousResponse.fromList(
                rendezVousRepository.findByUtilisateurIdWithDetails(userId)));
        return export;
    }

    @Transactional
    public void deleteMyAccount(Long userId, DeleteAccountRequest request) {
        Utilisateur u = getUtilisateurById(userId);
        if (u.getRole() == Utilisateur.Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Les comptes administrateur ne peuvent pas être supprimés via cette action");
        }
        if (!passwordEncoder.matches(request.getMotDePasse(), u.getMotDePasse())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mot de passe incorrect");
        }
        refreshTokenService.revokeAllForUser(userId);
        u.setActif(false);
        u.setNom("Compte supprimé");
        u.setEmail("deleted-" + userId + "@anon.erdv.local");
        u.setTelephone("0000000000");
        u.setMotDePasse(passwordEncoder.encode(UUID.randomUUID().toString()));
        utilisateurRepository.save(u);
    }
}
