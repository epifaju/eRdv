package com.erdv.service;

import org.springframework.stereotype.Component;

/**
 * Gabarits HTML responsive pour les e-mails transactionnels eRDV.
 */
@Component
public class EmailTemplateRenderer {

    private static final String BRAND_COLOR = "#2563eb";
    private static final String TEXT_COLOR = "#1f2937";
    private static final String MUTED_COLOR = "#6b7280";

    public String render(
            String title,
            String greeting,
            String introHtml,
            String detailsHtml,
            String buttonLabel,
            String buttonUrl,
            String footerNote) {

        String buttonBlock = "";
        if (buttonLabel != null && buttonUrl != null && !buttonUrl.isBlank()) {
            buttonBlock =
                    """
                    <tr>
                      <td style="padding:28px 32px 8px;text-align:center;">
                        <a href="%s" style="display:inline-block;background:%s;color:#ffffff;text-decoration:none;font-weight:600;font-size:15px;padding:14px 28px;border-radius:8px;">
                          %s
                        </a>
                      </td>
                    </tr>
                    """
                            .formatted(escapeAttr(buttonUrl), BRAND_COLOR, escapeHtml(buttonLabel));
        }

        String footerBlock = footerNote != null && !footerNote.isBlank()
                ? "<p style=\"margin:16px 0 0;font-size:13px;color:" + MUTED_COLOR + ";\">"
                        + escapeHtml(footerNote)
                        + "</p>"
                : "";

        return """
                <!DOCTYPE html>
                <html lang="fr">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#f3f4f6;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:%s;">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f3f4f6;padding:24px 12px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 16px rgba(15,23,42,0.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#1d4ed8,#2563eb);padding:28px 32px;text-align:center;">
                              %s
                              <h1 style="margin:12px 0 0;font-size:22px;line-height:1.3;color:#ffffff;font-weight:700;">%s</h1>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:28px 32px 8px;font-size:16px;line-height:1.6;">
                              <p style="margin:0 0 16px;">%s</p>
                              %s
                              %s
                              %s
                            </td>
                          </tr>
                          %s
                          <tr>
                            <td style="padding:8px 32px 28px;font-size:13px;color:%s;text-align:center;border-top:1px solid #e5e7eb;">
                              <p style="margin:0;">Cordialement,<br/><strong>L'équipe eRDV</strong></p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """
                .formatted(
                        escapeHtml(title),
                        TEXT_COLOR,
                        logoBlock(),
                        escapeHtml(title),
                        escapeHtml(greeting),
                        introHtml != null ? introHtml : "",
                        detailsHtml != null ? detailsHtml : "",
                        footerBlock,
                        buttonBlock,
                        MUTED_COLOR);
    }

    public String detailsBlock(String... rows) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:20px 0 0;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden;\">");
        for (int i = 0; i < rows.length; i += 2) {
            if (i + 1 >= rows.length) {
                break;
            }
            String label = rows[i];
            String value = rows[i + 1];
            String bg = (i / 2) % 2 == 0 ? "#f9fafb" : "#ffffff";
            sb.append("<tr style=\"background:")
                    .append(bg)
                    .append(";\">")
                    .append("<td style=\"padding:12px 16px;font-size:13px;color:")
                    .append(MUTED_COLOR)
                    .append(";width:38%%;vertical-align:top;\"><strong>")
                    .append(escapeHtml(label))
                    .append("</strong></td>")
                    .append("<td style=\"padding:12px 16px;font-size:14px;color:")
                    .append(TEXT_COLOR)
                    .append(";\">")
                    .append(value)
                    .append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String logoBlock() {
        return """
                <div style="display:inline-block;width:56px;height:56px;border-radius:14px;background:rgba(255,255,255,0.18);line-height:56px;">
                  <span style="font-size:28px;">📅</span>
                </div>
                <div style="margin-top:8px;font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:rgba(255,255,255,0.85);font-weight:600;">eRDV</div>
                """;
    }

    static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String escapeAttr(String text) {
        return escapeHtml(text);
    }
}
