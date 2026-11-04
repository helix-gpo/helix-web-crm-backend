package com.helix.gpo.web_crm.invoice.internal;

import com.helix.gpo.web_crm.invoice.internal.config.InvoiceBootstrapProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

// continues an existing numbering: only applies while no number has been issued in the current year
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(InvoiceBootstrapProperties.class)
class InvoiceSequenceBootstrap implements ApplicationRunner {

    private final InvoiceBootstrapProperties properties;
    private final InvoiceSequenceRepository sequenceRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long startNumber = properties.invoiceStartNumber();
        if (startNumber == null || startNumber <= 1) {
            return;
        }

        int year = Year.now().getValue();
        if (sequenceRepository.existsById(year)) {
            return;
        }

        sequenceRepository.save(new InvoiceSequence(year, startNumber - 1));
        log.info("Rechnungsnummern {} starten bei {}.", year, startNumber);
    }

}
