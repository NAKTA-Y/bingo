package com.nhnacademy.server;

import com.nhnacademy.database.BingoDB;
import com.nhnacademy.exception.AlreadySelectedException;
import com.nhnacademy.exception.DrawException;
import com.nhnacademy.exception.DuplicateNumberException;
import com.nhnacademy.exception.OutOfRangeNumberException;
import com.nhnacademy.exception.UserAlreadyExistsException;
import com.nhnacademy.exception.WinnerException;
import com.nhnacademy.message.Message;
import com.nhnacademy.message.MessageType;

import java.net.Socket;
import java.util.Map;

public enum BingoGameManagement {
    INSTANCE;

    // 클라이언트 생성 요청
    private void requestGenerateClient(String userId, Socket socket) {
        if (BingoDB.INSTANCE.getUserIds().contains(userId)) {
            throw new UserAlreadyExistsException("이미 존재하는 유저 입니다.");
        }

        BingoDB.INSTANCE.createUser(userId, socket);
    }

    // 클라이언트 빙고맵 기록
    private void recordBingoBoard(String userId, int number) {
        if (number < 1 || number > 25) {
            throw new OutOfRangeNumberException("숫자 범위를 벗어났습니다. 1 ~ 25 사이의 숫자만 입력해주세요.");
        }

        String numberFormat = String.format("%02d", number);
        String[][] board = BingoDB.INSTANCE.getBoardById(userId);
        boolean inputState = false;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (isDuplicated(board[i][j], numberFormat)) {
                    throw new DuplicateNumberException("이미 입력된 숫자입니다.");
                }

                if (board[i][j].equals("00")) {
                    board[i][j] = numberFormat;
                    inputState = true;
                    break;
                }
            }

            if (inputState)
                break;
        }
    }

    // TODO (ㅅㅇ)
    // 클라이언트 빙고맵 선택
    private void selectBingoBoard(String id, int number) {
        String numberFormat = String.format("%02d", number);

        if (isSelected(numberFormat, BingoDB.INSTANCE.getBoardById(id))) {
            throw new AlreadySelectedException("이미 선택된 숫자입니다.");
        }

        Map<String, String[][]> userBoards = BingoDB.INSTANCE.getUserBoards();

        for (String userId : userBoards.keySet()) {
            if (userId.equals(id)) {
                String[][] board = BingoDB.INSTANCE.getBoardById(userId);

                for (int i = 0; i < board.length; i++) {
                    for (int j = 0; j < board[i].length; j++) {
                        if (board[i][j].equals(numberFormat)) {
                            board[i][j] = "[" + numberFormat + "]";
                        }
                    }
                }
            } else {
                String[][] board = BingoDB.INSTANCE.getBoardById(userId);

                for (int i = 0; i < board.length; i++) {
                    for (int j = 0; j < board[i].length; j++) {
                        if (board[i][j].equals(numberFormat)) {
                            board[i][j] = "xx";
                        }
                    }
                }
            }
        }

        if (isWinner(id)) {
            throw new WinnerException(id);
        }

        if (isDraw(id)) {
            throw new DrawException("비겼습니다.");
        }
    }

    // TODO (ㅅㅇ)
    // 빙고 선택 여부 체크
    private boolean isSelected(String number, String[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (number.equals(board[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    // 클라이언트 빙고 중복 여부 체크
    private boolean isDuplicated(String boardNumber, String number) {
        return boardNumber.equals(number);
    }

    // 클라이언트 인원 충족 여부 체크
    public boolean isEnoughPersonCount() {
        return BingoDB.INSTANCE.getUserIds().size() == 2;
    }

    // 클라이언트 모든 플레이어들의 빙고맵 입력 여부 체크
    public boolean isAllCompletedInputBoard() {
        Map<String, String[][]> userBoards = BingoDB.INSTANCE.getUserBoards();

        for (String userId : userBoards.keySet()) {
            String[][] board = userBoards.get(userId);

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j].equals("00")) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // TODO (ㅅㅇ)
    // 빙고 승리 여부 체크
    private boolean isWinner(String id) {
        boolean isVictory = false;
        String[][] board = BingoDB.INSTANCE.getBoardById(id);

        for (int i = 0; i < board.length; i++) {
            if (board[i][0].equals("[\\d\\d]") && board[i][1].equals("[\\d\\d]") && board[i][2].equals("[\\d\\d]")
                    && board[i][3].equals("[\\d\\d]") && board[i][4].equals("[\\d\\d]")) {
                board[i][0] = "B";
                board[i][1] = "I";
                board[i][2] = "N";
                board[i][3] = "G";
                board[i][4] = "O";
                isVictory = true;
            }
            if (board[0][i].equals("[\\d\\d]") && board[1][i].equals("[\\d\\d]") && board[2][i].equals("[\\d\\d]")
                    && board[3][i].equals("[\\d\\d]") && board[4][i].equals("[\\d\\d]")) {
                board[0][i] = "B";
                board[1][i] = "I";
                board[2][i] = "N";
                board[3][i] = "G";
                board[4][i] = "O";
                isVictory = true;
            }
        }

        if (board[0][0].equals("[\\d\\d]") && board[1][1].equals("[\\d\\d]") && board[2][2].equals("[\\d\\d]")
                && board[3][3].equals("[\\d\\d]") && board[4][4].equals("[\\d\\d]")) {
            board[0][0] = "B";
            board[1][1] = "I";
            board[2][2] = "N";
            board[3][3] = "G";
            board[4][4] = "O";
            isVictory = true;
        }

        return isVictory;
    }

    // TODO (ㅅㅇ)
    // 빙고 무승부 여부 체크
    private boolean isDraw(String id) {
        boolean isDraw = true;
        String[][] board = BingoDB.INSTANCE.getBoardById(id);

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].equals("\\d\\d")) {
                    isDraw = false;
                }
            }
        }

        return isDraw;
    }

    // 메시지 타입 체크 메서드 및 호출
    public void sendMessage(Message message, Socket socket) {
        MessageType type = message.getMessageType();
        String[] messageSplit;
        String userId;
        int number;

        switch (type) {
            case CREATE:
                requestGenerateClient(message.getPayload(), socket);
                break;
            case INPUT:
                messageSplit = message.getPayload().split(",");
                userId = messageSplit[0];
                number = Integer.parseInt(messageSplit[1]);

                recordBingoBoard(userId, number);
                break;
            case SELECT:
                messageSplit = message.getPayload().split(",");
                userId = messageSplit[0];
                number = Integer.parseInt(messageSplit[1]);
                selectBingoBoard(userId, number);
        }
    }
}