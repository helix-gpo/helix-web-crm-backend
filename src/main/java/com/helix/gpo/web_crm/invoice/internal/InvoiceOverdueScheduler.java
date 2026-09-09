package com.helix.gpo.web_crm.invoice.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class InvoiceOverdueScheduler {

    private final InvoiceRepository invoiceRepository;

    // daily cronjob at 03:00 am updates each invoice status (compare to due date)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    void markOverdueInvoices() {
        List<Invoice> candidates = invoiceRepository.findAllByStatusInAndDueDateBefore(
                List.of(InvoiceStatus.ISSUED, InvoiceStatus.SENT),
                LocalDate.now()
        );

        if (candidates.isEmpty()) {
            return;
        }

        candidates.forEach(Invoice::markOverdue);
        invoiceRepository.saveAll(candidates);

        log.info("{} Rechnung(en) automatisch als überfällig markiert.", candidates.size());
    }

}
