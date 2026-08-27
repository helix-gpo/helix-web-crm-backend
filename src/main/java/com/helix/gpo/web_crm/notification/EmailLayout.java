package com.helix.gpo.web_crm.notification;

public final class EmailLayout {

    private EmailLayout() {
    }

    public static String wrap(String logoUrl, String preheader, String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="de">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0; padding:0; background-color:#f4f5f7; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                  <span style="display:none; max-height:0; overflow:hidden;">%s</span>
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7; padding: 32px 0;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow: 0 1px 3px rgba(13,20,36,0.08); max-width:560px;">
                          <tr>
                            <td style="padding: 28px 40px; border-bottom: 1px solid #eef0f3;">
                              <img src="%s" alt="Helix GPO" height="32" style="display:block; height:32px;">
                            </td>
                          </tr>
                          <tr>
                            <td style="padding: 36px 40px; color:#333c4d; font-size:15px; line-height:1.6;">
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style="padding: 24px 40px; background-color:#f9fafb; border-top: 1px solid #eef0f3; color:#8a93a6; font-size:12px; line-height:1.6;">
                              Helix GPO &middot; Erftstra&szlig;e 50, 41460 Neuss<br>
                              Diese E-Mail wurde automatisch generiert.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(preheader, logoUrl, bodyHtml);
    }

    public static String button(String label, String url) {
        return """
                <table role="presentation" cellpadding="0" cellspacing="0" style="margin: 24px 0;">
                  <tr>
                    <td style="border-radius:8px; background-color:#0d1424;">
                      <a href="%s" target="_blank" style="display:inline-block; padding:12px 24px; color:#ffffff; text-decoration:none; font-weight:600; font-size:14px; border-radius:8px;">%s</a>
                    </td>
                  </tr>
                </table>
                """.formatted(url, label);
    }
}
