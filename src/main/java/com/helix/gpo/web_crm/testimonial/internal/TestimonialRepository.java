package com.helix.gpo.web_crm.testimonial.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

interface TestimonialRepository extends JpaRepository<Testimonial, UUID> {

    List<Testimonial> findAllByTenantId(UUID tenantId);

    long countByVisibleOnWebsiteTrue();

    List<Testimonial> findAllByVisibleOnWebsiteTrueOrderByCreatedAtDesc();

}
