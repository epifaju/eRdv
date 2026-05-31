package com.erdv.service;

import com.erdv.entity.RendezVous;
import com.erdv.repository.RendezVousRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.reminders.enabled", havingValue = "true", matchIfMissing = true)
public class RendezVousReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(RendezVousReminderScheduler.class);

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Scheduled(cron = "${app.reminders.cron:0 */15 * * * *}")
    @Transactional
    public void envoyerRappels() {
        LocalDateTime now = LocalDateTime.now();
        envoyerRappelsJ1(now);
        envoyerRappelsH2(now);
        envoyerRappelsSmsJ1(now);
        envoyerRappelsSmsH2(now);
    }

    private void envoyerRappelsJ1(LocalDateTime now) {
        LocalDateTime debut = now.plusHours(23);
        LocalDateTime fin = now.plusHours(25);
        List<RendezVous> candidats = rendezVousRepository.findPendingJ1Reminders(debut, fin);
        for (RendezVous rdv : candidats) {
            try {
                emailService.envoyerRappelJ1(rdv);
                rdv.setRappelJ1Envoye(true);
                rendezVousRepository.save(rdv);
                log.info("Rappel J-1 e-mail envoyé pour le rendez-vous {}", rdv.getId());
            } catch (Exception e) {
                log.warn("Échec rappel J-1 e-mail pour le rendez-vous {} : {}", rdv.getId(), e.getMessage());
            }
        }
    }

    private void envoyerRappelsH2(LocalDateTime now) {
        LocalDateTime debut = now.plusMinutes(105);
        LocalDateTime fin = now.plusMinutes(135);
        List<RendezVous> candidats = rendezVousRepository.findPendingH2Reminders(debut, fin);
        for (RendezVous rdv : candidats) {
            try {
                emailService.envoyerRappelH2(rdv);
                rdv.setRappelH2Envoye(true);
                rendezVousRepository.save(rdv);
                log.info("Rappel H-2 e-mail envoyé pour le rendez-vous {}", rdv.getId());
            } catch (Exception e) {
                log.warn("Échec rappel H-2 e-mail pour le rendez-vous {} : {}", rdv.getId(), e.getMessage());
            }
        }
    }

    private void envoyerRappelsSmsJ1(LocalDateTime now) {
        if (!smsService.isConfigured()) {
            return;
        }
        LocalDateTime debut = now.plusHours(23);
        LocalDateTime fin = now.plusHours(25);
        List<RendezVous> candidats = rendezVousRepository.findPendingJ1SmsReminders(debut, fin);
        for (RendezVous rdv : candidats) {
            try {
                if (smsService.envoyerRappelJ1(rdv)) {
                    rdv.setRappelJ1SmsEnvoye(true);
                    rendezVousRepository.save(rdv);
                    log.info("Rappel J-1 SMS envoyé pour le rendez-vous {}", rdv.getId());
                }
            } catch (Exception e) {
                log.warn("Échec rappel J-1 SMS pour le rendez-vous {} : {}", rdv.getId(), e.getMessage());
            }
        }
    }

    private void envoyerRappelsSmsH2(LocalDateTime now) {
        if (!smsService.isConfigured()) {
            return;
        }
        LocalDateTime debut = now.plusMinutes(105);
        LocalDateTime fin = now.plusMinutes(135);
        List<RendezVous> candidats = rendezVousRepository.findPendingH2SmsReminders(debut, fin);
        for (RendezVous rdv : candidats) {
            try {
                if (smsService.envoyerRappelH2(rdv)) {
                    rdv.setRappelH2SmsEnvoye(true);
                    rendezVousRepository.save(rdv);
                    log.info("Rappel H-2 SMS envoyé pour le rendez-vous {}", rdv.getId());
                }
            } catch (Exception e) {
                log.warn("Échec rappel H-2 SMS pour le rendez-vous {} : {}", rdv.getId(), e.getMessage());
            }
        }
    }
}
