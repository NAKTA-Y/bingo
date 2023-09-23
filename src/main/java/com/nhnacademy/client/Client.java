package com.nhnacademy.client;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
  private String id;
  private final String host;
  private final int port;

  public Client(String id, String host, int port) {
    this.id = id;
    this.host = host;
    this.port = port;
  }

  @Override
  public void run() {
    try (Socket socket = new Socket(host, port)) {
      Sender sender = new Sender(socket);
      Receiver receiver = new Receiver(socket);

      socket.getOutputStream().write((id + System.lineSeparator()).getBytes());
      socket.getOutputStream().flush();

      sender.send();
      receiver.receive();

      sender.join();
      receiver.join();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
