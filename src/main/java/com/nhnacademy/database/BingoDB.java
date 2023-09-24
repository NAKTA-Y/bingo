package com.nhnacademy.database;

import lombok.Getter;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public enum BingoDB {
    INSTANCE;

    // Default Board Size = 5 x 5
    private static final String[][] BINGO_BOARD_FRAME = {{"00","00","00","00","00"},
                                                         {"00","00","00","00","00"},
                                                         {"00","00","00","00","00"},
                                                         {"00","00","00","00","00"},
                                                         {"00","00","00","00","00"}};

    private final Map<String, Socket> userInfo = new HashMap<>();
    private final Map<String, String[][]> userBoards = new HashMap<>();

    public void createUser(String id, Socket socket) {
        userInfo.put(id, socket);
        String[][] bingoBoard = BINGO_BOARD_FRAME.clone();
        userBoards.put(id, bingoBoard);
    }

    public String[][] getBoardById(String id) {
        return userBoards.get(id);
    }

    public Set<String> getUserIds() {
        return userInfo.keySet();
    }
}
