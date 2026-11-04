package com.helix.gpo.web_crm.testimonial.internal;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TestimonialInvitationTest {

    private TestimonialInvitation invitation(Instant expiresAt) {
        return TestimonialInvitation.builder().tokenHash("hash").expiresAt(expiresAt).build();
    }

    private TestimonialInvitation valid() {
        return invitation(Instant.now().plus(Duration.ofDays(30)));
    }

    @Test
    void pendingAndNotExpiredIsUsable() {
        assertThat(valid().isUsable()).isTrue();
    }

    @Test
    void expiredIsNotUsable() {
        assertThat(invitation(Instant.now().minusSeconds(1)).isUsable()).isFalse();
    }

    @Test
    void usedLinkCannotBeUsedAgain() {
        TestimonialInvitation invitation = valid();
        invitation.markUsed();

        assertThat(invitation.isUsable()).isFalse();
        assertThat(invitation.getUsedAt()).isNotNull();
    }

    @Test
    void revokedIsNotUsable() {
        TestimonialInvitation invitation = valid();
        invitation.revoke();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.REVOKED);
        assertThat(invitation.isUsable()).isFalse();
    }

    @Test
    void revokeAndExpireDoNotOverwriteAUsedLink() {
        TestimonialInvitation invitation = valid();
        invitation.markUsed();

        invitation.revoke();
        invitation.expire();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.USED);
    }

}
