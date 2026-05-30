package com.erdv.repository;

import com.erdv.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.jti = :jti")
    int revokeByJti(@Param("jti") String jti);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.utilisateur.id = :userId AND r.revoked = false")
    int revokeAllActiveForUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :before OR r.revoked = true")
    int deleteExpiredOrRevoked(@Param("before") Instant before);
}
