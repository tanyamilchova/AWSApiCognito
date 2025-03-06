package com.task11.entity;

import org.json.JSONObject;
import java.util.UUID;

public class Reservation {
    private final String reservationId;
    private final int tableNumber;
    private final String clientName;
    private final String phoneNumber;
    private final String date;
    private final String slotTimeStart;
    private final String slotTimeEnd;

    public Reservation(String reservationId, int tableNumber, String clientName, String phoneNumber, String date, String slotTimeStart, String slotTimeEnd) {
        this.reservationId = (reservationId != null) ? reservationId : UUID.randomUUID().toString();
        this.tableNumber = tableNumber;
        this.clientName = clientName;
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.slotTimeStart = slotTimeStart;
        this.slotTimeEnd = slotTimeEnd;
    }

    public Reservation(int tableNumber, String clientName, String phoneNumber, String date, String slotTimeStart, String slotTimeEnd) {
        this(UUID.randomUUID().toString(), tableNumber, clientName, phoneNumber, date, slotTimeStart, slotTimeEnd);
    }

    public String getReservationId() { return reservationId; }
    public int getTableNumber() { return tableNumber; }
    public String getClientName() { return clientName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getDate() { return date; }
    public String getSlotTimeStart() { return slotTimeStart; }
    public String getSlotTimeEnd() { return slotTimeEnd; }

    public static Reservation fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return new Reservation(
                json.getInt("tableNumber"),
                json.getString("clientName"),
                json.getString("phoneNumber"),
                json.getString("date"),
                json.getString("slotTimeStart"),
                json.getString("slotTimeEnd")
        );
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", reservationId);
        json.put("reservationId", reservationId);
        json.put("tableNumber", tableNumber);
        json.put("clientName", clientName);
        json.put("phoneNumber", phoneNumber);
        json.put("date", date);
        json.put("slotTimeStart", slotTimeStart);
        json.put("slotTimeEnd", slotTimeEnd);
        return json;
    }
}
