package com.helix.gpo.web_crm.invoice.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Component
@RequiredArgsConstructor
class InvoiceNumberGenerator {

    private static final String PREFIX = "RE";

    private final InvoiceSequenceRepository sequenceRepository;

    // MANDATORY statt REQUIRED: erzwingt, dass diese Methode NUR innerhalb
    // einer bestehenden Transaktion aufgerufen wird (nämlich der von
    // InvoiceService.issue()) - läuft sie in einer eigenen Transaktion,
    // würde der Lock schon vor dem Rechnung-Speichern wieder freigegeben,
    // und die Atomaritäts-Garantie wäre futsch
    @Transactional(propagation = Propagation.MANDATORY)
    String generateNext() {
        int currentYear = Year.now().getValue();

        InvoiceSequence sequence = sequenceRepository.findById(currentYear)
                .orElseGet(() -> sequenceRepository.save(new InvoiceSequence(currentYear)));

        long nextNumber = sequence.nextValue();

        return "%s-%d-%05d".formatted(PREFIX, currentYear, nextNumber);
    }

}
