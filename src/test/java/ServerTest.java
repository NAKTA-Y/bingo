import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.nhnacademy.server.CommunicationServer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerTest {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                CommunicationServer server = new CommunicationServer(socket);
                server.start();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
