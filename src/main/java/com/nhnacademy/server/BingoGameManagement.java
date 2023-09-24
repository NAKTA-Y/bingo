package com.nhnacademy.server;

import com.nhnacademy.message.Message;

public enum BingoGameManagement {
    INSTANCE;

    // 클라이언트 생성 요청
    public void requestGenerateClient(){
    }

    // 클라이언트 빙고맵 기록
    public void recordBingoBoard() {
    }

    // 클라이언트 빙고맵 선택
    public void selectBingoBoard() {
    }

    // 클라이언트 빙고 중복 여부 체크
    private boolean isDuplicated() {
    }

    // 클라이언트 인원 충족 여부 체크
    public boolean isEnoughPersonCount() {
    }

    // 클라이언트 모든 플레이어들의 빙고맵 입력 여부 체크
    public boolean isAllCompletedInputBoard() {
    }

    // 빙고 승리 여부 체크
    private boolean isWinner() {
    }

    // 빙고 무승부 여부 체크
    private boolean isDraw() {
    }

    // 메시지 타입 체크 메서드 및 호출
    public void sendMessage(Message message) {
    }
}