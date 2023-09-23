package com.nhnacademy.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Sender implements Runnable {
  Thread thread;
  Socket socket;

  public Sender(Socket socket) {
    this.socket = socket;
    thread = new Thread(this);
  }

  @Override
  public void run() {
    String line;

    try (BufferedReader terminalIn = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

      while ((line = terminalIn.readLine()) != null) {
        socketOut.write(line + System.lineSeparator());
        socketOut.flush();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void send() {
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
