package com.erdv.repository;

import com.erdv.entity.Utilisateur;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    @EntityGraph(attributePaths = "prestataire")
    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}