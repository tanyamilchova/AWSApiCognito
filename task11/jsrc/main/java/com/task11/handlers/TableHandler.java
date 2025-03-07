package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.task11.entity.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

public class TableHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLES_TABLE_NAME = System.getenv("tables_table");
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        context.getLogger().log("Enter TABLE handleRequest " + requestEvent);

        String httpMethod = requestEvent.getHttpMethod();
        String path = requestEvent.getPath();

        if ("GET".equals(httpMethod) && path.equals("/tables")) {
            return getAllTables();
        } else if ("POST".equals(httpMethod) && path.equals("/tables")) {
            return addTable(requestEvent.getBody());
        } else if ("GET".equals(httpMethod) && path.matches("/tables/\\d+")) {
            try {
                return getTableById(path.substring(path.lastIndexOf("/") + 1));
            } catch (Exception e) {
                context.getLogger().log("Parse error " + e.getMessage());
            }
        }
        /// /
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent()
                .withStatusCode(405)
                .withBody(new JSONObject().put("error", "Method Not Allowed").toString());
        /// /
//        return new APIGatewayProxyResponseEvent()
//                .withStatusCode(405)
//                .withBody(new JSONObject().put("error", "Method Not Allowed").toString());
        context.getLogger().log("APIGatewayProxyResponseEvent responseEvent " + responseEvent);
        return responseEvent;
    }

    private APIGatewayProxyResponseEvent getAllTables() {
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(TABLES_TABLE_NAME)
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            JSONArray tablesArray = new JSONArray(scanResponse.items().stream().map(this::mapToTableJson)
                    .collect(Collectors.toList()));

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject().put("tables", tablesArray).toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody(new JSONObject().put("error", "Failed to fetch tables").toString());
        }
    }

    private APIGatewayProxyResponseEvent getTableById(String tableId) {
        try {
            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(TABLES_TABLE_NAME)
                    .key(Map.of("id", AttributeValue.builder().s(tableId).build()))
                    .build();

            GetItemResponse getItemResponse = dynamoDbClient.getItem(getItemRequest);

            if (!getItemResponse.hasItem()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404)
                        .withBody(new JSONObject().put("error", "Table not found").toString());
            }

            JSONObject tableJson = mapToTableJson(getItemResponse.item());

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(tableJson.toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody(new JSONObject().put("error", "Failed to fetch table").toString());
        }
    }

    private APIGatewayProxyResponseEvent addTable(String body) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        try {
                        Table table = Table.fromJson(body);


            System.out.println("Table.fromJson(body) " + table);

            ///
            if (table.getId() != 0) {
//                item.put("id", AttributeValue.builder().s(String.valueOf(table.getId())).build());
                item.put("id", AttributeValue.builder().n(String.valueOf(table.getId())).build());
            } else {
                throw new IllegalArgumentException("ID cannot be null");
            }
            /// ///

//            item.put("id", AttributeValue.builder().s(String.valueOf(table.getId())).build());
            item.put("id", AttributeValue.builder().n(String.valueOf(table.getId())).build());
            item.put("number", AttributeValue.builder().n(String.valueOf(table.getNumber())).build());
            item.put("places", AttributeValue.builder().n(String.valueOf(table.getPlaces())).build());
            item.put("isVip", AttributeValue.builder().bool(table.isVip()).build());

            System.out.println("MinOrder: " + table.getMinOrder());

            table.getMinOrder().ifPresent(minOrder ->
                    item.put("minOrder", AttributeValue.builder().n(String.valueOf(minOrder)).build())
            );

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLES_TABLE_NAME)
                    .item(item)
                    .build();

            System.out.println("Inserting into DynamoDB: " + item);
            dynamoDbClient.putItem(putItemRequest);

            JSONObject response = new JSONObject().put("id", table.getId());
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", "Invalid Request Data").toString());
        }
    }

    private JSONObject mapToTableJson(Map<String, AttributeValue> item) {
        JSONObject jsonObject = new JSONObject()
                .put("id", Integer.parseInt(item.get("id").s()))
                .put("number", Integer.parseInt(item.get("number").n()))
                .put("places", Integer.parseInt(item.get("places").n()))
                .put("isVip", item.get("isVip").bool());

        if (item.containsKey("minOrder")) {
            jsonObject.put("minOrder", Integer.parseInt(item.get("minOrder").n()));
        }

        return jsonObject;
    }
}