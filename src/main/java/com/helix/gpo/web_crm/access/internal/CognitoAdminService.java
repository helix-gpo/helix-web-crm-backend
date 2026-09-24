package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.config.CognitoAdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
class CognitoAdminService {

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!?#";

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoAdminProperties properties;

    String createUser(String email) {
        String temporaryPassword = generateTemporaryPassword();

        try {
            cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .username(email)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("email_verified").value("true").build()
                    )
                    .temporaryPassword(temporaryPassword)
                    .messageAction(MessageActionType.SUPPRESS)
                    .build());
        } catch (UsernameExistsException e) {
            throw new IllegalStateException("Für diese E-Mail-Adresse existiert bereits ein Zugang.", e);
        }

        return temporaryPassword;
    }

    void addToGroup(String email, String groupName) {
        cognitoClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(email)
                .groupName(groupName)
                .build());
    }

    void removeFromGroup(String email, String groupName) {
        cognitoClient.adminRemoveUserFromGroup(AdminRemoveUserFromGroupRequest.builder()
                .userPoolId(properties.userPoolId())
                .username(email)
                .groupName(groupName)
                .build());
    }

    void deleteUser(String email) {
        try {
            cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .username(email)
                    .build());
        } catch (UserNotFoundException ignored) {
            // already gone - fine
        }
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(random.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

}
