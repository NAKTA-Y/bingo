package com.nhnacademy.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nhnacademy.client.Client;
import com.nhnacademy.server.CommunicationServer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bingo {
    public static void main(String[] args) {
        Options options = new Options();
        String host = "localhost";
        String userId = UUID.randomUUID().toString();
        int port = 12345;

        options.addOption("l", false, "서버 모드 동작");
        options.addOption("p", "port", true, "포트 번호");
        options.addOption("h", "host", true, "호스트 이름");
        options.addOption("i", "id", true, "유저 이름");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                host = cmd.getOptionValue("h");
            }

            if (cmd.hasOption("p")) {
                port = Integer.parseInt(cmd.getOptionValue("p"));
            }

            if (cmd.hasOption("i")) {
                userId = cmd.getOptionValue("i");
            }

            if (cmd.hasOption("l")) {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    Socket socket;
                    while ((socket = serverSocket.accept()) != null) {
                        CommunicationServer server = new CommunicationServer(socket);
                        server.start();
                    }
                } catch (IOException | NumberFormatException e) {
                    log.error(e.getMessage());
                }
            } else {
                Client client = new Client(userId, host, port);
                client.start();
            }
        } catch (ParseException e) {
            log.error(e.getMessage());
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("사용 설명", options);
        }
    }
}