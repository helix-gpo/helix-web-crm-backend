package com.helix.gpo.web_crm.testimonial.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface TestimonialInvitationRepository extends JpaRepository<TestimonialInvitation, java.util.UUID> {

    Optional<TestimonialInvitation> findByTokenHash(String tokenHash);

    List<TestimonialInvitation> findAllByTenantIdOrderByCreatedAtDesc(java.util.UUID tenantId);

}
