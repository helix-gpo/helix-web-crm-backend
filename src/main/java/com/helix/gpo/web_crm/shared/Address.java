package com.helix.gpo.web_crm.shared;

import jakarta.persistence.Column;

public record Address(
        @Column(name = "street", length = 120)
        String street,

        @Column(name = "house_number", length = 20)
        String houseNumber,

        @Column(name = "postal_code", length = 10)
        String postalCode,

        @Column(name = "city", length = 80)
        String city,

        @Column(name = "country_code", length = 2)
        String countryCode
) {}
