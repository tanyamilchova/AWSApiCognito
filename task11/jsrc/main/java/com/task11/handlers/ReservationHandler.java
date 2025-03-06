package com.task11.handlers;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.entity.Reservation;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class ReservationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String TABLE_NAME = System.getenv("reservations_table");
    private static final String TABLES_TABLE_NAME = System.getenv("tables_table");
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String method = request.getHttpMethod();
        switch (method) {
            case "POST":
                return createReservation(request.getBody(), context);
            case "GET":
                return getReservations(context);
            default:
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid Request");
        }
    }

    private APIGatewayProxyResponseEvent createReservation(String body, Context context) {
        try {
            context.getLogger().log("Received request body: " + body);

            Reservation reservation;
            try {
                reservation = Reservation.fromJson(body);
                context.getLogger().log("Parsed reservation: " + reservation.toJson().toString());
            } catch (Exception e) {
                context.getLogger().log("JSON Parsing Error: " + e.getMessage());
                return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Invalid JSON format: " + e.getMessage());
            }

            try {
                if (!tableExists(reservation.getTableNumber(), context)) {
                    context.getLogger().log("Table does not exist: " + reservation.getTableNumber());
                    return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Table does not exist");
                }
            } catch (Exception e) {
                context.getLogger().log("Table Existence Check Error: " + e.getMessage());
                return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Server Error - Table Check Failed - " + e.getMessage());
            }

            try {
                if (isOverlapping(reservation, context)) {
                    context.getLogger().log("Table already reserved for this time: " + reservation.getTableNumber());
                    return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Table already reserved for this time");
                }
            } catch (Exception e) {
                context.getLogger().log("Reservation Conflict Check Error: " + e.getMessage());
                return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Server Error - Conflict Check Failed - " + e.getMessage());
            }

            String reservationId;
            try {
                reservationId = UUID.randomUUID().toString();
                reservation = new Reservation(
                        reservationId,
                        reservation.getTableNumber(),
                        reservation.getClientName(),
                        reservation.getPhoneNumber(),
                        reservation.getDate(),
                        reservation.getSlotTimeStart(),
                        reservation.getSlotTimeEnd()
                );
            } catch (Exception e) {
                context.getLogger().log("Reservation ID Generation Error: " + e.getMessage());
                return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Server Error - Reservation ID Generation Failed - " + e.getMessage());
            }

            try {
                saveReservation(reservation, context);
            } catch (Exception e) {
                context.getLogger().log("Database Save Error: " + e.getMessage());
                return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Server Error - Could not save reservation - " + e.getMessage());
            }

            try {
                JSONObject response = new JSONObject().put("reservationId", reservationId);
                context.getLogger().log("Reservation created successfully: " + response.toString());
                return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(response.toString());
            } catch (Exception e) {
                context.getLogger().log("Response JSON Creation Error: " + e.getMessage());
                return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Server Error - Response Construction Failed - " + e.getMessage());
            }

        } catch (Exception e) {
            context.getLogger().log("Unexpected Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Server Error: " + e.getMessage());
        }
    }

    private boolean tableExists(int tableNumber, Context context) {
        context.getLogger().log("Checking if table exists by number: " + tableNumber);

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLES_TABLE_NAME)
                .filterExpression("#num = :tableNumber")
                .expressionAttributeNames(Map.of("#num", "number"))
                .expressionAttributeValues(Map.of(":tableNumber", AttributeValue.builder().n(String.valueOf(tableNumber)).build()))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        context.getLogger().log("ScanResponse: " + scanResponse.toString());

        return !scanResponse.items().isEmpty();
    }

    private boolean isOverlapping(Reservation newReservation, Context context) {
        context.getLogger().log("Checking reservation conflict for table: " + newReservation.getTableNumber() + " on " + newReservation.getDate());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("tableNumber = :tableNumber AND #date = :date")
                .expressionAttributeNames(Map.of("#date", "date"))
                .expressionAttributeValues(Map.of(
                        ":tableNumber", AttributeValue.builder().n(String.valueOf(newReservation.getTableNumber())).build(),
                        ":date", AttributeValue.builder().s(newReservation.getDate()).build()
                ))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        context.getLogger().log("Found " + scanResponse.count() + " reservations for this table and date.");

        for (Map<String, AttributeValue> item : scanResponse.items()) {
            String existingStart = item.get("slotTimeStart").s();
            String existingEnd = item.get("slotTimeEnd").s();

            context.getLogger().log("Existing reservation found: " + existingStart + " - " + existingEnd);

            boolean isOverlapping = !(
                    newReservation.getSlotTimeEnd().compareTo(existingStart) <= 0 ||
                            newReservation.getSlotTimeStart().compareTo(existingEnd) >= 0
            );

            if (isOverlapping) {
                context.getLogger().log("Is overlapping with existing reservation: " + existingStart + " - " + existingEnd);
                return true;
            }
        }

        context.getLogger().log("No reservation conflict found.");
        return false;
    }


    private void saveReservation(Reservation reservation, Context context) {
        context.getLogger().log("Saving reservation...");

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(reservation.getReservationId()).build());
        item.put("reservationId", AttributeValue.builder().s(reservation.getReservationId()).build());
        item.put("tableNumber", AttributeValue.builder().n(String.valueOf(reservation.getTableNumber())).build());
        item.put("clientName", AttributeValue.builder().s(reservation.getClientName()).build());
        item.put("phoneNumber", AttributeValue.builder().s(reservation.getPhoneNumber()).build());
        item.put("date", AttributeValue.builder().s(reservation.getDate()).build());
        item.put("slotTimeStart", AttributeValue.builder().s(reservation.getSlotTimeStart()).build());
        item.put("slotTimeEnd", AttributeValue.builder().s(reservation.getSlotTimeEnd()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        context.getLogger().log("PutItemRequest: " + putItemRequest.toString());

        try {
            dynamoDbClient.putItem(putItemRequest);
            context.getLogger().log("Event saved to DynamoDB table '" + System.getenv("target_table"));
        } catch (DynamoDbException e) {
            context.getLogger().log("Failed to save event to DynamoDB: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent getReservations(Context context) {
        try {
            context.getLogger().log("Fetching reservations from DynamoDB...");

            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
            List<Map<String, Object>> reservations = new ArrayList<>();

            for (Map<String, AttributeValue> item : scanResponse.items()) {
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("tableNumber", item.containsKey("tableNumber") ? Integer.parseInt(item.get("tableNumber").n()) : null);
                reservation.put("clientName", item.containsKey("clientName") ? item.get("clientName").s() : null);
                reservation.put("phoneNumber", item.containsKey("phoneNumber") ? item.get("phoneNumber").s() : null);
                reservation.put("date", item.containsKey("date") ? item.get("date").s() : null);
                reservation.put("slotTimeStart", item.containsKey("slotTimeStart") ? item.get("slotTimeStart").s() : null);
                reservation.put("slotTimeEnd", item.containsKey("slotTimeEnd") ? item.get("slotTimeEnd").s() : null);
                reservations.add(reservation);
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("reservations", reservations);

            String responseJson = objectMapper.writeValueAsString(responseBody);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseJson);

        } catch (DynamoDbException e) {
            context.getLogger().log("DynamoDB error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent().withStatusCode(500)
                    .withBody("{\"error\":\"DynamoDB Query Failed: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            context.getLogger().log("Unexpected error: " + (e.getMessage() != null ? e.getMessage() : "Unknown Error"));
            return new APIGatewayProxyResponseEvent().withStatusCode(500)
                    .withBody("{\"error\":\"Internal Server Error: " + (e.getMessage() != null ? e.getMessage() : "Unknown Error") + "\"}");
        }
    }

}