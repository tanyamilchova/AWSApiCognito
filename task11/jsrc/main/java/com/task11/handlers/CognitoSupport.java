package com.task11.handlers;

import com.task11.entity.*;
import com.task11.auth.*;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminRespondToAuthChallengeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeliveryMediumType;

import java.util.Map;

    public abstract class CognitoSupport {
/// ////// ?????????? GET ENV COGNITO_ID ?????????
        private final String userPoolId = System.getenv("COGNITO_ID");
        private final String clientId = System.getenv("CLIENT_ID");
        private final CognitoIdentityProviderClient cognitoClient;

        protected CognitoSupport(CognitoIdentityProviderClient cognitoClient) {
            this.cognitoClient = cognitoClient;
        }

        protected AdminInitiateAuthResponse cognitoSignIn(String email, String password) {
            Map<String, String> authParams = Map.of(
                    "USERNAME", email,
                    "PASSWORD", password
            );

            return cognitoClient.adminInitiateAuth(AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParams)
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .build());
        }

        protected AdminCreateUserResponse cognitoSignUp(SignUp signUp) {

            return cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(signUp.getEmail())
                    .temporaryPassword(signUp.getPassword())
                    .userAttributes(
                            AttributeType.builder()
                                    .name("given_name")
                                    .value(signUp.getFirstName())
                                    .build(),
                            AttributeType.builder()
                                    .name("family_name")
                                    .value(signUp.getLastName())
                                    .build(),
                            AttributeType.builder()
                                    .name("email")
                                    .value(signUp.getEmail())
                                    .build(),
                            AttributeType.builder()
                                    .name("email_verified")
                                    .value("true")
                                    .build())
                    .desiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .messageAction("SUPPRESS")
                    .forceAliasCreation(Boolean.FALSE)
                    .build()
            );
        }

        protected AdminRespondToAuthChallengeResponse confirmSignUp(SignUp signUp) {
            AdminInitiateAuthResponse adminInitiateAuthResponse = cognitoSignIn(signUp.getEmail(), signUp.getPassword());

            if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(adminInitiateAuthResponse.challengeNameAsString())) {
                throw new RuntimeException("unexpected challenge: " + adminInitiateAuthResponse.challengeNameAsString());
            }

            Map<String, String> challengeResponses = Map.of(
                    "USERNAME", signUp.getEmail(),
                    "PASSWORD", signUp.getPassword(),
                    "NEW_PASSWORD", signUp.getPassword()
            );

            return cognitoClient.adminRespondToAuthChallenge(AdminRespondToAuthChallengeRequest.builder()
                    .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                    .challengeResponses(challengeResponses)
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .session(adminInitiateAuthResponse.session())
                    .build());
        }
    }