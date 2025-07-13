package com.erdv.repository;

import com.erdv.entity.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    List<RendezVous> findByUtilisateurIdOrderByDateHeureDesc(Long utilisateurId);

    List<RendezVous> findByPrestataireIdOrderByDateHeureDesc(Long prestataireId);

    List<RendezVous> findByStatutOrderByDateHeureDesc(RendezVous.Statut statut);
}