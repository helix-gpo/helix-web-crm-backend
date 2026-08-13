package com.helix.gpo.web_crm.invoice.internal;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class InvoicePdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final TemplateEngine templateEngine;
    private String cachedLogoBase64;

    public byte[] render(Invoice invoice) {
        Context context = new Context(Locale.GERMANY);

        context.setVariable("logoBase64", logoBase64());
        context.setVariable("invoiceNumber", invoice.getInvoiceNumber());
        context.setVariable("issueDate", invoice.getIssueDate() != null ? invoice.getIssueDate().format(DATE_FORMAT) : "-");
        context.setVariable("dueDate", invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMAT) : "-");
        context.setVariable("paymentTermsDays", invoice.getPaymentTermsDays());
        context.setVariable("buyerReference", invoice.getBuyerReference());

        context.setVariable("seller", invoice.getSeller());
        context.setVariable("buyer", invoice.getBuyer());
        context.setVariable("sellerName", invoice.getSeller().name());
        context.setVariable("sellerAddressLine", addressLine(invoice.getSeller()));
        context.setVariable("buyerAddressLine", addressLine(invoice.getBuyer()));

        context.setVariable("lineItems", invoice.getLineItems().stream().map(item -> Map.of(
                "positionNumber", item.getPositionNumber(),
                "description", item.getDescription(),
                "quantity", item.getQuantity().stripTrailingZeros().toPlainString(),
                "unitPriceFormatted", formatMoney(item.getUnitPrice().amount()),
                "taxRatePercentage", item.getTaxRatePercentage().stripTrailingZeros().toPlainString(),
                "grossAmountFormatted", formatMoney(item.grossAmount().amount())
        )).toList());

        context.setVariable("netTotalFormatted", formatMoney(invoice.netTotal().amount()));
        context.setVariable("grossTotalFormatted", formatMoney(invoice.grossTotal().amount()));
        context.setVariable("taxBreakdown", buildTaxBreakdown(invoice));

        String html = templateEngine.process("invoice/invoice-pdf", context);
        return convertToPdf(html);
    }

    private List<Map<String, String>> buildTaxBreakdown(Invoice invoice) {
        return invoice.getLineItems().stream()
                .collect(Collectors.groupingBy(item -> item.getTaxRatePercentage().stripTrailingZeros()))
                .entrySet().stream()
                .sorted((a, b) -> b.getKey().compareTo(a.getKey()))
                .map(entry -> {
                    BigDecimal net = entry.getValue().stream()
                            .map(i -> i.netAmount().amount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal tax = entry.getValue().stream()
                            .map(i -> i.taxAmount().amount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return Map.of(
                            "rate", entry.getKey().toPlainString(),
                            "netFormatted", formatMoney(net),
                            "taxFormatted", formatMoney(tax)
                    );
                })
                .toList();
    }

    private String addressLine(BillingParty party) {
        if (party.address() == null) return "";
        var a = party.address();
        return String.format("%s %s, %s %s",
                a.street() != null ? a.street() : "",
                a.houseNumber() != null ? a.houseNumber() : "",
                a.postalCode() != null ? a.postalCode() : "",
                a.city() != null ? a.city() : "").trim();
    }

    private String formatMoney(BigDecimal amount) {
        return String.format(Locale.GERMANY, "%,.2f €", amount);
    }

    private synchronized String logoBase64() {
        if (cachedLogoBase64 == null) {
            try {
                byte[] bytes = new ClassPathResource("static/images/logo-invoice.png").getInputStream().readAllBytes();
                cachedLogoBase64 = Base64.getEncoder().encodeToString(bytes);
            } catch (IOException e) {
                throw new IllegalStateException("Logo für PDF-Erzeugung konnte nicht geladen werden", e);
            }
        }
        return cachedLogoBase64;
    }

    private byte[] convertToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("PDF-Erzeugung fehlgeschlagen", e);
        }
    }

}
