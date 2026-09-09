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

    // secure that only one invoice is saved at one time - avoid saving a new invoice during save process of another invoice
    @Transactional(propagation = Propagation.MANDATORY)
    String generateNext() {
        int currentYear = Year.now().getValue();

        InvoiceSequence sequence = sequenceRepository.findById(currentYear)
                .orElseGet(() -> sequenceRepository.save(new InvoiceSequence(currentYear)));

        long nextNumber = sequence.nextValue();

        return "%s-%d-%05d".formatted(PREFIX, currentYear, nextNumber);
    }

}
