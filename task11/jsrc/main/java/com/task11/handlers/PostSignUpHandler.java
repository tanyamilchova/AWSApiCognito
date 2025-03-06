package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import com.task11.auth.*;



public class PostSignUpHandler extends CognitoSupport implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    public PostSignUpHandler(CognitoIdentityProviderClient cognitoClient) {
        super(cognitoClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        try {


            System.out.println("**...handleRequest SIGN_UP : " + requestEvent);
            /// //////////////
            JSONObject json = new JSONObject(requestEvent.getBody());
            SignUp signUp = new SignUp(
                    json.optString("email", null),
                    json.optString("password", null),
                    json.optString("firstName", null),
                    json.optString("lastName", null)
            );
            context.getLogger().log("signup " + signUp);
            /// ////////////////

            String userId = cognitoSignUp(signUp)
                    .user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .map(AttributeType::value)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Sub not found."));
            String idToken = confirmSignUp(signUp)
                    .authenticationResult()
                    .idToken();

            context.getLogger().log("signup " + signUp);
            context.getLogger().log("Id token" + idToken);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject()
                            .put("message", "User has been successfully signed up.")
                            .put("userId", userId)
                            .put("accessToken", idToken)
                            .toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

}