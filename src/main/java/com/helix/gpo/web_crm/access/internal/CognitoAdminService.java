package com.helix.gpo.web_crm.access.internal;

import com.helix.gpo.web_crm.access.internal.config.CognitoAdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
class CognitoAdminService {

    private static final String UPPERCASE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE_CHARS = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGIT_CHARS = "23456789";
    private static final String SYMBOL_CHARS = "!?#$%";

    private final CognitoIdentityProviderClient cognitoClient;
    private final CognitoAdminProperties properties;

    record CreatedCognitoUser(String sub, String temporaryPassword) {
    }

    CreatedCognitoUser createUser(String email) {
        String temporaryPassword = generateTemporaryPassword();

        AdminCreateUserResponse response;
        try {
            response = cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
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

        String sub = response.user().attributes().stream()
                .filter(attribute -> "sub".equals(attribute.name()))
                .map(AttributeType::value)
                .findFirst()
                .orElse(null);

        return new CreatedCognitoUser(sub, temporaryPassword);
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

    void disableUser(String email) {
        try {
            cognitoClient.adminDisableUser(AdminDisableUserRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .username(email)
                    .build());
        } catch (UserNotFoundException ignored) {
            // legacy: the cognito user was already deleted by the former deactivate flow
        }
    }

    // false if the cognito user does not exist anymore
    boolean enableUser(String email) {
        try {
            cognitoClient.adminEnableUser(AdminEnableUserRequest.builder()
                    .userPoolId(properties.userPoolId())
                    .username(email)
                    .build());
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
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
        List<Character> chars = new ArrayList<>();

        // mindestens ein Zeichen aus jeder Kategorie garantieren,
        // statt rein zufällig zu ziehen und zu hoffen
        chars.add(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        chars.add(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        chars.add(DIGIT_CHARS.charAt(random.nextInt(DIGIT_CHARS.length())));
        chars.add(SYMBOL_CHARS.charAt(random.nextInt(SYMBOL_CHARS.length())));

        String allChars = UPPERCASE_CHARS + LOWERCASE_CHARS + DIGIT_CHARS + SYMBOL_CHARS;
        while (chars.size() < 16) {
            chars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder(chars.size());
        chars.forEach(sb::append);
        return sb.toString();
    }

}
