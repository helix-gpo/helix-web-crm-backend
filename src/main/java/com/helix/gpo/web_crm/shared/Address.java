package com.helix.gpo.web_crm.shared;

import jakarta.validation.constraints.Size;

public record Address(
        @Size(max = 120) String street,
        @Size(max = 20) String houseNumber,
        @Size(max = 10) String postalCode,
        @Size(max = 80) String city,
        @Size(max = 2) String countryCode
) {}
