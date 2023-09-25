package com.nhnacademy.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;

import com.nhnacademy.database.BingoDB;
import com.nhnacademy.exception.AlreadySelectedException;
import com.nhnacademy.exception.DrawException;
import com.nhnacademy.exception.DuplicateNumberException;
import com.nhnacademy.exception.SelectOrderException;
import com.nhnacademy.exception.WinnerException;
import com.nhnacademy.message.Message;
import com.nhnacademy.message.MessageType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommunicationServer extends Thread {
    Socket socket;

    public CommunicationServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));) {
            // 아이디 저장
            String clientId = bufferedReader.readLine();

            // 유저 등록
            BingoGameManagement.INSTANCE.sendMessage(new Message(clientId, "", MessageType.CREATE), socket);

            // 인원이 찰 때 까지 대기
            synchronized (BingoGameManagement.INSTANCE) {
                try {
                    while (!BingoGameManagement.INSTANCE.isEnoughPersonCount()) {
                        BingoGameManagement.INSTANCE.wait();
                    }
                    BingoGameManagement.INSTANCE.notifyAll();
                } catch (InterruptedException e) {
                    log.error("error message: {}", e);
                    Thread.currentThread().interrupt();
                }
            }

            // TODO (ㅅㅇ)
            // 빙고판 숫자 입력
            while (!BingoGameManagement.INSTANCE.isCompletedInputBoard(clientId)) {
                try {
                    writeBoard(socket, clientId);
                    String number = bufferedReader.readLine();
                    BingoGameManagement.INSTANCE.sendMessage(new Message(clientId, number, MessageType.INPUT), socket);
                } catch (RuntimeException e) {
                    bufferedWriter.write(e.getMessage() + System.lineSeparator());
                    bufferedWriter.flush();
                }
            }

            // TODO (ㅅㅇ)
            // 모든 인원 빙고판 숫자 입력 대기
            synchronized (BingoGameManagement.INSTANCE) {
                try {
                    while (!BingoGameManagement.INSTANCE.isAllCompletedInputBoard()) {
                        BingoGameManagement.INSTANCE.wait();
                    }
                    BingoGameManagement.INSTANCE.notifyAll();
                } catch (InterruptedException e) {
                    log.error("error message: {}", e);
                    Thread.currentThread().interrupt();
                }
            }

            // 본 게임 시작
            BingoGameManagement.INSTANCE.randomOrder();

            // TODO (ㅅㅇ)
            // 모두에게 각자의 빙고판 보여주기
            Map<String, Socket> userInfo = BingoDB.INSTANCE.getUserInfo();
            writeBoard(socket, clientId);

            String input;
            while ((input = bufferedReader.readLine()) != null) {
                try {
                    BingoGameManagement.INSTANCE.sendMessage(new Message(clientId, input, MessageType.SELECT), socket);
                    for (String id : userInfo.keySet()) {
                        Socket clientSocket = userInfo.get(id);
                        writeBoard(clientSocket, id);
                    }
                } catch (NumberFormatException e) {
                    bufferedWriter.write("숫자를 입력해주세요." + System.lineSeparator());
                    bufferedWriter.flush();
                } catch (AlreadySelectedException | SelectOrderException e) {
                    bufferedWriter.write(e.getMessage() + System.lineSeparator());
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            log.error("error message: {}", e);
        } catch (WinnerException e) {
            Map<String, Socket> userInfo = BingoDB.INSTANCE.getUserInfo();
            for (String id : BingoDB.INSTANCE.getUserIds()) {
                Socket userSocket = userInfo.get(id);
                writeBoard(userSocket, id);
                try {
                    if (id.equals(BingoGameManagement.INSTANCE.getCurrentUser())) {
                        userSocket.getOutputStream().write(("당신이 이겼습니다." + System.lineSeparator()).getBytes());
                        userSocket.getOutputStream().flush();
                    } else {
                        userSocket.getOutputStream().write(("당신이 졌습니다." + System.lineSeparator()).getBytes());
                        userSocket.getOutputStream().flush();
                    }
                } catch (IOException er) {
                    log.error(er.getMessage());
                }
            }
        } catch (DrawException e) {
            Map<String, Socket> userInfo = BingoDB.INSTANCE.getUserInfo();
            for (String id : BingoDB.INSTANCE.getUserIds()) {
                Socket userSocket = userInfo.get(id);
                writeBoard(userSocket, id);
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()))) {
                    writer.write("비겼습니다. 게임을 종료합니다." + System.lineSeparator());
                    writer.flush();
                } catch (IOException er) {
                    log.error(er.getMessage());
                }
            }
        }
    }

    public void writeBoard(Socket socket, String id) {
        String[][] board = BingoDB.INSTANCE.getBoardById(id);

        try {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    socket.getOutputStream().write((board[i][j] + " ").getBytes());
                }
                socket.getOutputStream().write(System.lineSeparator().getBytes());
            }
            socket.getOutputStream().flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}