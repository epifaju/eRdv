package com.erdv.repository;

import com.erdv.entity.PlageRecurrente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlageRecurrenteRepository extends JpaRepository<PlageRecurrente, Long> {

    List<PlageRecurrente> findByPrestataireIdAndActifTrueOrderByJourSemaineAscHeureDebutAsc(Long prestataireId);

    List<PlageRecurrente> findByPrestataireIdOrderByJourSemaineAscHeureDebutAsc(Long prestataireId);
}
