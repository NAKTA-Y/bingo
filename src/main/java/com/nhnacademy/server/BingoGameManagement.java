package com.nhnacademy.server;

import com.nhnacademy.database.BingoDB;
import com.nhnacademy.exception.DuplicateNumberException;
import com.nhnacademy.exception.OutOfRangeNumberException;
import com.nhnacademy.exception.UserAlreadyExistsException;
import com.nhnacademy.message.Message;
import com.nhnacademy.message.MessageType;

import java.net.Socket;
import java.util.Map;

public enum BingoGameManagement {
    INSTANCE;

    // 클라이언트 생성 요청
    private void requestGenerateClient(String userId, Socket socket){
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

            if (inputState) break;
        }
    }

    // TODO (ㅅㅇ)
    // 클라이언트 빙고맵 선택
    private void selectBingoBoard() {
    }

    // TODO (ㅅㅇ)
    // 빙고 선택 여부 체크
    private boolean isSelected() {
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
    private boolean isWinner() {
    }

    // TODO (ㅅㅇ)
    // 빙고 무승부 여부 체크
    private boolean isDraw() {
    }

    // 메시지 타입 체크 메서드 및 호출
    public void sendMessage(Message message, Socket socket) {
        MessageType type = message.getMessageType();

        switch (type) {
            case CREATE:
                requestGenerateClient(message.getPayload(), socket);
                break;
            case INPUT:
                String[] messageSplit = message.getPayload().split(",");
                String userId = messageSplit[0];
                int number = Integer.parseInt(messageSplit[1]);

                recordBingoBoard(userId, number);
                break;
            case SELECT:
        }
    }
}