package com.erdv.repository;

import com.erdv.entity.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrestataireRepository extends JpaRepository<Prestataire, Long> {

    Optional<Prestataire> findByEmail(String email);

    @Query("SELECT p FROM Prestataire p JOIN FETCH p.etablissement WHERE p.etablissement.id = :etablissementId ORDER BY p.nom ASC")
    List<Prestataire> findByEtablissement_IdOrderByNomAsc(@Param("etablissementId") Long etablissementId);

    @Query("SELECT p FROM Prestataire p JOIN FETCH p.etablissement ORDER BY p.nom ASC")
    List<Prestataire> findAllWithEtablissement();

    @Query("SELECT p FROM Prestataire p JOIN FETCH p.etablissement WHERE p.id = :id")
    Optional<Prestataire> findByIdWithEtablissement(@Param("id") Long id);

    long countByEtablissement_Id(Long etablissementId);
}