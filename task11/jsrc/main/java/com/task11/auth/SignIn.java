package com.task11.auth;

import org.json.JSONObject;

public class SignIn {
    private final String email;
    private final String password;

    public SignIn(String email, String password) {
        if (email == null || password == null) {
            throw new IllegalArgumentException("Bad request, try again");
        }
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public static SignIn fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return new SignIn(
                json.optString("email", null),
                json.optString("password", null)
        );
    }

}