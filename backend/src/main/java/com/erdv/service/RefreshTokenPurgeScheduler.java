package com.erdv.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.auth.refresh-token-purge.enabled", havingValue = "true", matchIfMissing = true)
public class RefreshTokenPurgeScheduler {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenPurgeScheduler.class);

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Scheduled(cron = "${app.auth.refresh-token-purge.cron:0 0 3 * * *}")
    public void purgeExpiredRefreshTokens() {
        int deleted = refreshTokenService.purgeExpired();
        if (deleted > 0) {
            log.info("Purge refresh tokens : {} entrée(s) supprimée(s)", deleted);
        } else {
            log.debug("Purge refresh tokens : rien à supprimer");
        }
    }
}
