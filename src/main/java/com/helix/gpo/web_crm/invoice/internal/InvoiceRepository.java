package com.helix.gpo.web_crm.invoice.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findAllByTenantId(UUID tenantId);

    List<Invoice> findAllByProjectId(UUID projectId);

}
