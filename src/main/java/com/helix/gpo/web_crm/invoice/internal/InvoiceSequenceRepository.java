package com.helix.gpo.web_crm.invoice.internal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Integer> {

    // PESSIMISTIC_WRITE = SELECT ... FOR UPDATE - avoid two invoice generations simultaneously
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InvoiceSequence> findById(Integer year);

}
