package com.helix.gpo.web_crm.invoice.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {

    List<InvoiceLineItem> findAllByMilestoneIdIn(List<UUID> milestoneIds);

}
