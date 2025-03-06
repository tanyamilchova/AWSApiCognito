package com.task11.auth;

import org.json.JSONObject;



public class SignUp {
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    public SignUp(String email, String password, String firstName, String lastName) {
        if (email == null || password == null || firstName == null || lastName == null) {
            throw new IllegalArgumentException("Bad request, try again.");
        }
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static SignUp fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return new SignUp(
                json.optString("email", null),
                json.optString("password", null),
                json.optString("firstName", null),
                json.optString("lastName", null)
        );
    }

}