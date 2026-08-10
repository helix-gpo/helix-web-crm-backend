package com.helix.gpo.web_crm.invoice.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Integer> {

    // PESSIMISTIC_WRITE = SELECT ... FOR UPDATE - sperrt die Zeile bis zum
    // Transaktionsende, damit zwei gleichzeitige Rechnung-Erstellungen
    // garantiert nacheinander (nicht parallel) eine Nummer ziehen
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InvoiceSequence> findById(Integer year);

}
