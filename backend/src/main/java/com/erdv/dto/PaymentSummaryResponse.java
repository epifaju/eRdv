package com.erdv.dto;

import com.erdv.entity.Payment;

import java.math.BigDecimal;

public class PaymentSummaryResponse {

    private Long id;
    private BigDecimal montant;
    private String devise;
    private String statut;

    public static PaymentSummaryResponse from(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentSummaryResponse r = new PaymentSummaryResponse();
        r.id = payment.getId();
        r.montant = payment.getMontant();
        r.devise = payment.getDevise();
        r.statut = payment.getStatut() != null ? payment.getStatut().name() : null;
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getDevise() {
        return devise;
    }

    public void setDevise(String devise) {
        this.devise = devise;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
