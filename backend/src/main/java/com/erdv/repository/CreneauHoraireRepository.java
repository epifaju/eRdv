package com.erdv.repository;

import com.erdv.entity.CreneauHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CreneauHoraireRepository extends JpaRepository<CreneauHoraire, Long> {

    List<CreneauHoraire> findByPrestataireIdAndDisponibleTrueOrderByDateHeure(Long prestataireId);

    @Query("SELECT c FROM CreneauHoraire c WHERE c.prestataire.id = :prestataireId AND c.dateHeure >= :dateDebut AND c.disponible = true ORDER BY c.dateHeure")
    List<CreneauHoraire> findCreneauxDisponibles(@Param("prestataireId") Long prestataireId,
            @Param("dateDebut") LocalDateTime dateDebut);

    List<CreneauHoraire> findByPrestataireId(Long prestataireId);
}