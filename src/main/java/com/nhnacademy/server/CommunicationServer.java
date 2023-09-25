package com.nhnacademy.server;

import com.nhnacademy.database.BingoDB;
import com.nhnacademy.exception.AlreadySelectedException;
import com.nhnacademy.exception.DrawException;
import com.nhnacademy.exception.SelectOrderException;
import com.nhnacademy.exception.WinnerException;
import com.nhnacademy.message.Message;
import com.nhnacademy.message.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Map;

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
                        bufferedWriter.write("게임 인원 대기중..." + System.lineSeparator());
                        bufferedWriter.flush();
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
                    bufferedWriter.write("1~25사이의 숫자를 입력해 주세요!" + System.lineSeparator());
                    bufferedWriter.flush();

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
                        bufferedWriter.write("다른사람들이 숫자를 입력할 때까지 대기해주세요!" + System.lineSeparator());
                        bufferedWriter.flush();

                        BingoGameManagement.INSTANCE.wait();
                    }
                    BingoGameManagement.INSTANCE.notifyAll();
                } catch (InterruptedException e) {
                    log.error("error message: {}", e);
                    Thread.currentThread().interrupt();
                }
            }

            // 본 게임 시작
            bufferedWriter.write("게임을 시작합니다!" + System.lineSeparator() + System.lineSeparator());
            BingoGameManagement.INSTANCE.randomOrder();

            // TODO (ㅅㅇ)
            // 모두에게 각자의 빙고판 보여주기
            Map<String, Socket> userInfo = BingoDB.INSTANCE.getUserInfo();
            writeBoard(socket, clientId);
            writeOrder(socket, clientId);
            writeSelectInfomation(socket, clientId);

            String input;
            while ((input = bufferedReader.readLine()) != null) {
                try {
                    BingoGameManagement.INSTANCE.sendMessage(new Message(clientId, input, MessageType.SELECT), socket);
                    for (String id : userInfo.keySet()) {
                        Socket clientSocket = userInfo.get(id);
                        writeBoard(clientSocket, id);
                        writeOrder(clientSocket, id);
                        writeSelectInfomation(clientSocket, id);
                    }
                } catch (NumberFormatException e) {
                    bufferedWriter.write("숫자를 입력해주세요." + System.lineSeparator());
                    bufferedWriter.flush();
                } catch (AlreadySelectedException | SelectOrderException e) {
                    bufferedWriter.write(e.getMessage() + System.lineSeparator());
                    bufferedWriter.flush();
                } catch (WinnerException e) {
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

                            userSocket.close();
                        } catch (IOException er) {
                            log.error(er.getMessage());
                        }
                    }
                } catch (DrawException e) {
                    for (String id : BingoDB.INSTANCE.getUserIds()) {
                        Socket userSocket = userInfo.get(id);
                        writeBoard(userSocket, id);
                        try (BufferedWriter writer = new BufferedWriter(
                                new OutputStreamWriter(userSocket.getOutputStream()))) {
                            writer.write("비겼습니다. 게임을 종료합니다." + System.lineSeparator());
                            writer.flush();
                        } catch (IOException er) {
                            log.error(er.getMessage());
                        }

                        userSocket.close();
                    }
                }
            }
        } catch (IOException e) {
            log.error("error message: {}", e);
        }
    }

    public void writeBoard(Socket socket, String id) {
        String[][] board = BingoDB.INSTANCE.getBoardById(id);

        try {
            socket.getOutputStream().write(System.lineSeparator().getBytes());
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    String centerFormat = StringUtils.center(board[i][j], 4);
                    socket.getOutputStream().write(centerFormat.getBytes());
                }
                socket.getOutputStream().write(System.lineSeparator().getBytes());
            }
            socket.getOutputStream().flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void writeOrder(Socket socket, String userId) throws IOException {
        if (BingoGameManagement.INSTANCE.getCurrentUser().equals(userId)) {
            socket.getOutputStream().write(("상대방 차례입니다." + System.lineSeparator()).getBytes());
            socket.getOutputStream().flush();
        } else {
            socket.getOutputStream().write(("당신 차례입니다." + System.lineSeparator()).getBytes());
            socket.getOutputStream().flush();
        }
    }

    private void writeSelectInfomation(Socket socket, String userId) throws IOException {
        if (!BingoGameManagement.INSTANCE.getCurrentUser().equals(userId)) {
            socket.getOutputStream().write(("보드 내에 있는 원하는 숫자를 입력해주세요." + System.lineSeparator()).getBytes());
            socket.getOutputStream().flush();
        }
    }
}