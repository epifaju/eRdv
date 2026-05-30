package com.erdv.repository;

import com.erdv.entity.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {

    List<Etablissement> findByActifTrueOrderByNomAsc();
}
