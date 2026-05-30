package com.erdv.repository;

import com.erdv.entity.Prestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestationRepository extends JpaRepository<Prestation, Long> {

    List<Prestation> findByPrestataireIdAndActifTrueOrderByNomAsc(Long prestataireId);

    List<Prestation> findByPrestataireIdOrderByNomAsc(Long prestataireId);

    @Query("""
            SELECT MIN(p.dureeMinutes) FROM Prestation p
            WHERE p.prestataire.id = :prestataireId AND p.actif = true
            """)
    Optional<Integer> findMinDureeActive(@Param("prestataireId") Long prestataireId);
}
