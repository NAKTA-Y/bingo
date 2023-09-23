package com.nhnacademy.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Receiver implements Runnable {
  Thread thread;
  Socket socket;

  public Receiver(Socket socket) {
    this.socket = socket;
    thread = new Thread(this);
  }

  @Override
  public void run() {
    String line;

    try (BufferedReader socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter terminalOut = new BufferedWriter(new OutputStreamWriter(System.out))) {

      while ((line = socketIn.readLine()) != null) {
        terminalOut.write(line + System.lineSeparator());
        terminalOut.flush();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void receive() {
    thread.start();
  }

  public void join() {
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
