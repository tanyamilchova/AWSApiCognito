package com.task11.entity;

import org.json.JSONObject;
import java.util.Optional;
import java.util.UUID;

public class Table {
    private  int id;
    private  int number;
    private  int places;
    private  boolean isVip;
    private  Optional<Integer> minOrder;

    public Table(int id, int number, int places, boolean isVip, Integer minOrder) {
        this.id = id;
        this.number = number;
        this.places = places;
        this.isVip = isVip;
        this.minOrder = Optional.ofNullable(minOrder);
    }
    public Table(){

    }

    public int getId() { return id; }
    public int getNumber() { return number; }
    public int getPlaces() { return places; }
    public boolean isVip() { return isVip; }
    public Optional<Integer> getMinOrder() { return minOrder; }

    public static Table fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return new Table(
                json.getInt("id"),
                json.getInt("number"),
                json.getInt("places"),
                json.getBoolean("isVip"),
                json.has("minOrder") ? json.getInt("minOrder") : null
        );
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("number", number);
        json.put("places", places);
        json.put("isVip", isVip);
        minOrder.ifPresent(value -> json.put("minOrder", value));
        return json;
    }
}
