package com.task11.handlers;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;

public class RouteNotImplementedHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

@Override
public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
    context.getLogger().log("Incoming request events " + requestEvent);

    String message = String.format(
            "Handler for the %s method on the %s path is not implemented.",
            requestEvent.getHttpMethod(),
            requestEvent.getPath()
    );

    return new APIGatewayProxyResponseEvent()
            .withStatusCode(501)
            .withBody(
                    new JSONObject().put("message", message).toString()
            );
}
}