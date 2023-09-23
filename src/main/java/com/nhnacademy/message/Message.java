package com.nhnacademy.message;

public class Message {
  private String userId;
  private String payload;
  private MessageType messageType;

  public Message(String userId, String payload, MessageType messageType) {
    this.userId = userId;
    this.payload = payload;
    this.messageType = messageType;
  }

  public String getUserId() {
    return userId;
  }

  public String getPayload() {
    return payload;
  }

  public MessageType getMessageType() {
    return messageType;
  }
}
