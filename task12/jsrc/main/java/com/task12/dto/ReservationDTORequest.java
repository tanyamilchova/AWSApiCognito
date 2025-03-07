package com.task12.dto;

public class ReservationDTORequest {
    private Integer tableNumber;
    private String clientName;
    private String phoneNumber;
    private String date;  // Format: "yyyy-MM-dd"
    private String slotTimeStart;  // Format: "HH:mm"
    private String slotTimeEnd;  // Format: "HH:mm"

    // Parameterized constructor
    public ReservationDTORequest(Integer tableNumber, String clientName, String phoneNumber,
                                 String date, String slotTimeStart, String slotTimeEnd) {
        this.tableNumber = tableNumber;
        this.clientName = clientName;
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.slotTimeStart = slotTimeStart;
        this.slotTimeEnd = slotTimeEnd;
    }
    // No-argument constructor (for JSON serialization/deserialization)
    public ReservationDTORequest() {
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(Integer tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSlotTimeStart() {
        return slotTimeStart;
    }

    public void setSlotTimeStart(String slotTimeStart) {
        this.slotTimeStart = slotTimeStart;
    }

    public String getSlotTimeEnd() {
        return slotTimeEnd;
    }

    public void setSlotTimeEnd(String slotTimeEnd) {
        this.slotTimeEnd = slotTimeEnd;
    }

}