package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.config.CompanyBillingProperties;
import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos;
import com.helix.gpo.web_crm.invoice.internal.dto.InvoiceDtos.*;
import com.helix.gpo.web_crm.notification.EmailAttachment;
import com.helix.gpo.web_crm.notification.EmailMessage;
import com.helix.gpo.web_crm.notification.NotificationApi;
import com.helix.gpo.web_crm.project.MilestoneSummary;
import com.helix.gpo.web_crm.project.ProjectApi;
import com.helix.gpo.web_crm.shared.Money;
import com.helix.gpo.web_crm.storage.StorageApi;
import com.helix.gpo.web_crm.tenant.TenantApi;
import com.helix.gpo.web_crm.tenant.TenantBillingDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final TenantApi tenantApi;
    private final ProjectApi projectApi;
    private final StorageApi storageApi;
    private final NotificationApi notificationApi;
    private final CompanyBillingProperties companyBillingProperties;
    private final InvoicePdfService invoicePdfService;

    InvoiceResponse create(CreateInvoiceRequest request) {
        TenantBillingDetails tenant = tenantApi.findBillingDetailsById(request.tenantId())
                .orElseThrow(() -> new EntityNotFoundException("Dieser Mandant wurde nicht gefunden."));

        String buyerReference = request.buyerReference() != null && !request.buyerReference().isBlank()
                ? request.buyerReference()
                : generateAutoReference(tenant);

        Invoice invoice = Invoice.builder()
                .tenantId(request.tenantId())
                .projectId(request.projectId())
                .buyerReference(buyerReference)
                .paymentTermsDays(request.paymentTermsDays() != null ? request.paymentTermsDays() : 14)
                .seller(InvoiceMapper.toSellerSnapshot(companyBillingProperties))
                .buyer(InvoiceMapper.toBuyerSnapshot(tenant))
                .build();

        request.lineItems().forEach(item -> appendLineItem(invoice, item));

        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    private String generateAutoReference(TenantBillingDetails tenant) {
        if (tenant.referenceCode() == null || tenant.referenceCode().isBlank()) {
            return null; // kein Kürzel gepflegt - keine automatische Referenz möglich
        }
        long existingCount = invoiceRepository.countByTenantId(tenant.tenantId());
        return tenant.referenceCode() + "." + (existingCount + 1);
    }

    InvoiceResponse addLineItem(UUID invoiceId, LineItemRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        appendLineItem(invoice, request);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    InvoiceResponse removeLineItem(UUID invoiceId, UUID lineItemId) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        invoice.removeLineItem(lineItemId);
        return InvoiceMapper.toResponse(invoice);
    }

    InvoiceResponse issue(UUID invoiceId, IssueInvoiceRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);

        TenantBillingDetails tenant = tenantApi.findBillingDetailsById(invoice.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Dieser Mandant wurde nicht gefunden."));
        invoice.freezeBillingSnapshots(
                InvoiceMapper.toSellerSnapshot(companyBillingProperties),
                InvoiceMapper.toBuyerSnapshot(tenant)
        );

        assertBillingDataComplete(invoice);
        assertMilestonesNotLockedElsewhere(invoice);

        LocalDate issueDate = request != null && request.issueDate() != null ? request.issueDate() : LocalDate.now();
        String invoiceNumber = invoiceNumberGenerator.generateNext();

        invoice.issue(invoiceNumber, issueDate);
        invoiceRepository.save(invoice);

        byte[] pdf = invoicePdfService.render(invoice);
        String documentKey = "invoices/" + invoice.getId() + "/" + invoiceNumber + ".pdf";
        storageApi.upload(documentKey, pdf, "application/pdf");
        invoice.attachDocument(documentKey);
        invoiceRepository.save(invoice);

        boolean sendDirectly = request != null && Boolean.TRUE.equals(request.sendEmailDirectly());
        if (sendDirectly) {
            String targetEmail = resolveTargetEmail(invoice, request.invoiceEmail());
            sendInvoiceEmail(invoice, targetEmail, pdf, invoiceNumber);
            invoice.markSent(targetEmail);
        }

        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    InvoiceResponse send(UUID invoiceId, SendInvoiceRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        String targetEmail = resolveTargetEmail(invoice, request != null ? request.email() : null);

        byte[] pdf = invoicePdfService.render(invoice);
        sendInvoiceEmail(invoice, targetEmail, pdf, invoice.getInvoiceNumber());
        invoice.markSent(targetEmail);

        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    InvoiceResponse markPaid(UUID invoiceId, MarkPaidRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        LocalDate paidDate = request != null && request.paidDate() != null ? request.paidDate() : LocalDate.now();
        invoice.markPaid(paidDate);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    private String resolveTargetEmail(Invoice invoice, String requestedEmail) {
        if (requestedEmail != null && !requestedEmail.isBlank()) {
            return requestedEmail;
        }
        if (invoice.getBuyer() != null && invoice.getBuyer().email() != null) {
            return invoice.getBuyer().email();
        }
        throw new IllegalArgumentException(
                "Keine Rechnungs-E-Mail-Adresse hinterlegt und keine angegeben.");
    }

    private void sendInvoiceEmail(Invoice invoice, String toEmail, byte[] pdfBytes, String invoiceNumber) {
        String subject = "Rechnung " + invoiceNumber + " – " + companyBillingProperties.name();
        String preheader = "Ihre Rechnung " + invoiceNumber + " finden Sie im Anhang.";
        String body = """
            <p style="margin:0 0 16px;">Sehr geehrte Damen und Herren,</p>
            <p style="margin:0 0 16px;">anbei erhalten Sie die Rechnung <strong>%s</strong> von %s.</p>
            <p style="margin:0 0 16px;">Bitte begleichen Sie den Betrag innerhalb der angegebenen Zahlungsfrist. Bei Rückfragen antworten Sie gerne direkt auf diese E-Mail.</p>
            <p style="margin:24px 0 0;">Mit freundlichen Grüßen<br/>%s</p>
            """.formatted(invoiceNumber, companyBillingProperties.name(), companyBillingProperties.name());

        notificationApi.send(new EmailMessage(
                toEmail,
                subject,
                preheader,
                body,
                List.of(new EmailAttachment(invoiceNumber + ".pdf", pdfBytes, "application/pdf"))
        ));
    }

    String getDocumentUrl(UUID invoiceId) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        if (invoice.getDocumentKey() == null) {
            throw new IllegalStateException("Für diese Rechnung existiert noch kein Dokument: " + invoiceId);
        }
        return storageApi.presignedUrl(invoice.getDocumentKey(), Duration.ofMinutes(15));
    }

    @Transactional(readOnly = true)
    InvoiceResponse findById(UUID id) {
        Invoice invoice = getInvoiceOrThrow(id);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            return InvoiceMapper.toResponse(invoice);
        }

        TenantBillingDetails tenant = tenantApi.findBillingDetailsById(invoice.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Dieser Mandant wurde nicht gefunden."));

        return InvoiceMapper.toResponse(
                invoice,
                InvoiceMapper.toSellerSnapshot(companyBillingProperties),
                InvoiceMapper.toBuyerSnapshot(tenant)
        );
    }

    @Transactional(readOnly = true)
    List<InvoiceResponse> findAll() {
        return invoiceRepository.findAll().stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    List<InvoiceResponse> findAllByTenant(UUID tenantId) {
        return invoiceRepository.findAllByTenantId(tenantId).stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    // Genau das, was das Frontend zum Vorbefüllen des Erstell-Formulars braucht:
    // vorgeschlagener Verkäufer (eure Stammdaten), vorgeschlagener Käufer (Mandant),
    // und alle wählbaren Meilensteine mit Preis + Info, ob schon abgerechnet
    @Transactional(readOnly = true)
    InvoicePrefillResponse prefill(UUID tenantId, UUID projectId) {
        TenantBillingDetails tenant = tenantApi.findBillingDetailsById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Dieser Mandant wurde nicht gefunden."));

        List<MilestoneOptionDto> milestones = projectId == null
                ? List.of()
                : buildMilestoneOptions(projectId);

        InvoiceDtos.BillingPartyDto buyerDto = InvoiceMapper.toDto(InvoiceMapper.toBuyerSnapshot(tenant));
        boolean buyerDataComplete = BillingDataValidator.missingFields(buyerDto, "Mandant", false).isEmpty();

        return new InvoicePrefillResponse(
                InvoiceMapper.toDto(InvoiceMapper.toSellerSnapshot(companyBillingProperties)),
                buyerDto,
                buyerDataComplete,
                generateAutoReference(tenant),
                milestones
        );
    }

    private void assertBillingDataComplete(Invoice invoice) {
        List<String> missing = new ArrayList<>();
        missing.addAll(BillingDataValidator.missingFields(
                InvoiceMapper.toDto(invoice.getSeller()), "Aussteller (eigene Firmendaten)", true));
        missing.addAll(BillingDataValidator.missingFields(
                InvoiceMapper.toDto(invoice.getBuyer()), "Rechnungsempfänger (Mandant)", false));

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Rechnung kann nicht ausgestellt werden - fehlende Pflichtangaben: " + String.join("; ", missing));
        }
    }

    private List<MilestoneOptionDto> buildMilestoneOptions(UUID projectId) {
        List<MilestoneSummary> milestones = projectApi.findMilestoneSummariesByProject(projectId);

        List<UUID> milestoneIds = milestones.stream().map(MilestoneSummary::id).toList();
        List<InvoiceLineItem> lineItems = invoiceLineItemRepository.findAllByMilestoneIdIn(milestoneIds);

        Set<UUID> invoicedMilestoneIds = lineItems.stream()
                .filter(li -> isLockingStatus(li.getInvoice().getStatus()))
                .map(InvoiceLineItem::getMilestoneId)
                .collect(Collectors.toSet());

        Set<UUID> draftMilestoneIds = lineItems.stream()
                .filter(li -> li.getInvoice().getStatus() == InvoiceStatus.DRAFT)
                .map(InvoiceLineItem::getMilestoneId)
                .collect(Collectors.toSet());

        return milestones.stream()
                .map(m -> new MilestoneOptionDto(
                        m.id(),
                        m.title(),
                        null,
                        m.price(),
                        invoicedMilestoneIds.contains(m.id()),
                        draftMilestoneIds.contains(m.id()),
                        m.status() != null ? m.status() : null
                ))
                .toList();
    }

    private boolean isLockingStatus(InvoiceStatus status) {
        return status == InvoiceStatus.ISSUED
                || status == InvoiceStatus.SENT
                || status == InvoiceStatus.PAID
                || status == InvoiceStatus.OVERDUE;
    }

    private void assertMilestonesNotLockedElsewhere(Invoice invoice) {
        List<UUID> milestoneIds = invoice.getLineItems().stream()
                .map(InvoiceLineItem::getMilestoneId)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (milestoneIds.isEmpty()) {
            return;
        }

        boolean lockedElsewhere = invoiceLineItemRepository.findAllByMilestoneIdIn(milestoneIds).stream()
                .filter(li -> !li.getInvoice().getId().equals(invoice.getId()))
                .anyMatch(li -> isLockingStatus(li.getInvoice().getStatus()));

        if (lockedElsewhere) {
            throw new IllegalStateException(
                    "Diese Rechnung enthält Meilensteine, die bereits in einer anderen ausgestellten Rechnung abgerechnet werden. "
                            + "Bitte entferne die betroffene Position, bevor du die Rechnung ausstellst.");
        }
    }


    private void appendLineItem(Invoice invoice, LineItemRequest request) {
        switch (request.source()) {
            case MILESTONE -> {
                if (request.milestoneId() == null) {
                    throw new IllegalArgumentException("Für Meilenstein-Positionen ist eine Meilenstein-Auswahl erforderlich.");
                }
                MilestoneSummary milestone = projectApi.findMilestoneSummaryById(request.milestoneId())
                        .orElseThrow(() -> new EntityNotFoundException("Dieser Meilenstein wurde nicht gefunden."));

                Money unitPrice = request.unitPrice() != null ? request.unitPrice() : milestone.price();
                if (unitPrice == null) {
                    throw new IllegalArgumentException(
                            "Milestone has no price and none was provided: " + request.milestoneId());
                }

                invoice.addLineItem(
                        LineItemSource.MILESTONE,
                        milestone.id(),
                        request.description() != null ? request.description() : milestone.title(),
                        request.quantity(),
                        request.unitCode(),
                        unitPrice,
                        request.taxRatePercentage()
                );
            }
            case CUSTOM -> {
                if (request.description() == null || request.unitPrice() == null) {
                    throw new IllegalArgumentException("Für freie Positionen sind Beschreibung und Einzelpreis erforderlich.");
                }
                invoice.addLineItem(
                        LineItemSource.CUSTOM,
                        null,
                        request.description(),
                        request.quantity(),
                        request.unitCode(),
                        request.unitPrice(),
                        request.taxRatePercentage()
                );
            }
        }
    }

    private String generateInvoiceNumber() {
        // Platzhalter - lückenlose Sequenz bauen wir, sobald wir konkret XRechnung umsetzen
        return "INV-" + LocalDate.now().getYear() + "-" + System.currentTimeMillis();
    }

    private Invoice getInvoiceOrThrow(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Diese Rechnung wurde nicht gefunden."));
    }

    InvoiceResponse updateHeader(UUID invoiceId, UpdateInvoiceHeaderRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        invoice.updateHeader(request.buyerReference(), request.paymentTermsDays());
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    InvoiceResponse updateLineItem(UUID invoiceId, UUID lineItemId, UpdateLineItemRequest request) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        invoice.updateLineItem(
                lineItemId,
                request.description(),
                request.quantity(),
                request.unitCode(),
                request.unitPrice(),
                request.taxRatePercentage()
        );
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    void delete(UUID invoiceId) {
        Invoice invoice = getInvoiceOrThrow(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Nur Entwürfe können gelöscht werden.");
        }
        invoiceRepository.delete(invoice);
    }

}
