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

      sender.send();
      receiver.receive();

      sender.join();
      receiver.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
