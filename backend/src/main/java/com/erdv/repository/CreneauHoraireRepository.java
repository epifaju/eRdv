package com.erdv.repository;

import com.erdv.entity.CreneauHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreneauHoraireRepository extends JpaRepository<CreneauHoraire, Long> {

    List<CreneauHoraire> findByPrestataireIdAndDisponibleTrueOrderByDateHeure(Long prestataireId);

    @Query("SELECT c FROM CreneauHoraire c WHERE c.prestataire.id = :prestataireId AND c.dateHeure >= :dateDebut AND c.disponible = true ORDER BY c.dateHeure")
    List<CreneauHoraire> findCreneauxDisponibles(@Param("prestataireId") Long prestataireId,
            @Param("dateDebut") LocalDateTime dateDebut);

    @Query("""
            SELECT c FROM CreneauHoraire c
            WHERE c.prestataire.id = :prestataireId
              AND c.dateHeure >= :start
              AND c.dateHeure < :end
              AND c.disponible = true
            ORDER BY c.dateHeure
            """)
    List<CreneauHoraire> findCreneauxDisponiblesBetween(
            @Param("prestataireId") Long prestataireId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    boolean existsByPrestataireIdAndDateHeure(Long prestataireId, LocalDateTime dateHeure);

    List<CreneauHoraire> findByPrestataireId(Long prestataireId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CreneauHoraire c WHERE c.id = :id")
    Optional<CreneauHoraire> findByIdForUpdate(@Param("id") Long id);

    Optional<CreneauHoraire> findByPrestataireIdAndDateHeure(Long prestataireId, LocalDateTime dateHeure);
}