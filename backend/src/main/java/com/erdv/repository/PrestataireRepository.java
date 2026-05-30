package com.erdv.repository;

import com.erdv.entity.Prestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrestataireRepository extends JpaRepository<Prestataire, Long> {

    Optional<Prestataire> findByEmail(String email);
}