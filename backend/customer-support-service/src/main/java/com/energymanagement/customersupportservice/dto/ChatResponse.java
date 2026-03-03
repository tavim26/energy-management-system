package com.energymanagement.customersupportservice.dto;

public class ChatResponse {

    private String response;
    private boolean isAutomatic;
    private String source; // "RULE" sau "AI" sau "ADMIN"

    public ChatResponse() {
    }

    public ChatResponse(String response, boolean isAutomatic, String source) {
        this.response = response;
        this.isAutomatic = isAutomatic;
        this.source = source;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public void setAutomatic(boolean automatic) {
        isAutomatic = automatic;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "response='" + response + '\'' +
                ", isAutomatic=" + isAutomatic +
                ", source='" + source + '\'' +
                '}';
    }
}