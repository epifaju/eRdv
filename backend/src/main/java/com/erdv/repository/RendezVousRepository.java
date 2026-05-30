package com.erdv.repository;

import com.erdv.entity.RendezVous;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            LEFT JOIN FETCH r.creneauxReserves
            ORDER BY r.dateHeure DESC
            """)
    List<RendezVous> findAllWithDetails();

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            LEFT JOIN FETCH r.creneauxReserves
            WHERE r.utilisateur.id = :utilisateurId
            ORDER BY r.dateHeure DESC
            """)
    List<RendezVous> findByUtilisateurIdWithDetails(@Param("utilisateurId") Long utilisateurId);

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            LEFT JOIN FETCH r.creneauxReserves
            WHERE r.prestataire.id = :prestataireId
            ORDER BY r.dateHeure DESC
            """)
    List<RendezVous> findByPrestataireIdWithDetails(@Param("prestataireId") Long prestataireId);

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            LEFT JOIN FETCH r.creneauxReserves
            WHERE r.statut = :statut
            ORDER BY r.dateHeure DESC
            """)
    List<RendezVous> findByStatutWithDetails(@Param("statut") RendezVous.Statut statut);

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            LEFT JOIN FETCH r.creneauxReserves
            WHERE r.id = :id
            """)
    Optional<RendezVous> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            LEFT JOIN FETCH r.creneauxReserves
            WHERE r.id IN :ids
            ORDER BY r.dateHeure DESC
            """)
    List<RendezVous> findAllWithDetailsByIds(@Param("ids") List<Long> ids);

    Page<RendezVous> findAllByOrderByDateHeureDesc(Pageable pageable);

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            WHERE r.statut IN ('EN_ATTENTE', 'CONFIRME')
            AND r.rappelJ1Envoye = false
            AND r.dateHeure >= :debut
            AND r.dateHeure <= :fin
            """)
    List<RendezVous> findPendingJ1Reminders(
            @Param("debut") java.time.LocalDateTime debut,
            @Param("fin") java.time.LocalDateTime fin);

    @Query("""
            SELECT DISTINCT r FROM RendezVous r
            JOIN FETCH r.utilisateur
            JOIN FETCH r.prestataire
            JOIN FETCH r.creneau
            LEFT JOIN FETCH r.prestation
            WHERE r.statut = 'CONFIRME'
            AND r.rappelH2Envoye = false
            AND r.dateHeure >= :debut
            AND r.dateHeure <= :fin
            """)
    List<RendezVous> findPendingH2Reminders(
            @Param("debut") java.time.LocalDateTime debut,
            @Param("fin") java.time.LocalDateTime fin);
}