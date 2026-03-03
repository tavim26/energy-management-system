package com.energymanagement.customersupportservice.dto;

public class ChatRequest {

    private Long userId;
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(Long userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "userId=" + userId +
                ", message='" + message + '\'' +
                '}';
    }
}