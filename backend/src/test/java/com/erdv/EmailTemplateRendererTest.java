package com.erdv;

import com.erdv.service.EmailTemplateRenderer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTemplateRendererTest {

    private final EmailTemplateRenderer renderer = new EmailTemplateRenderer();

    @Test
    void renderInclutLogoEtBouton() {
        String html = renderer.render(
                "Test titre",
                "Bonjour Client,",
                "<p>Intro test</p>",
                renderer.detailsBlock("Prestataire", "Dr Martin"),
                "Voir mon rendez-vous",
                "http://localhost:3001/mes-rendez-vous",
                "Note bas de page");

        assertTrue(html.contains("eRDV"));
        assertTrue(html.contains("Voir mon rendez-vous"));
        assertTrue(html.contains("http://localhost:3001/mes-rendez-vous"));
        assertTrue(html.contains("Dr Martin"));
        assertTrue(html.contains("<!DOCTYPE html>"));
    }
}
