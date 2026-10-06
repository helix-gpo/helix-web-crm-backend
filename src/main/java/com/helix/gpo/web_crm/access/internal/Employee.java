package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "employees")
class Employee extends BaseEntity {

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "cognito_sub", length = 64)
    private String cognitoSub;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    void assignRole(Role role) {
        this.role = role;
    }

    void deactivate() {
        this.active = false;
    }

    void activate() {
        this.active = true;
    }

    void updateDetails(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    void linkCognitoUser(String cognitoSub) {
        this.cognitoSub = cognitoSub;
    }

}
