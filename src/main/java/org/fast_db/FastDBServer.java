package org.fast_db;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FastDBServer {
    private static final Logger LOGGER = Logger.getLogger(FastDBServer.class.getName());

    private FastDBServer() {
    }

    public static void start(int port) {
        try (var server = new ServerSocket(port);
             var service = Executors.newVirtualThreadPerTaskExecutor()) {

            try (Socket clientSocket = server.accept();
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    if (inputLine.equalsIgnoreCase("exit")) {
                        writer.println("Good Bye");
                        break;
                    }

                    final String newLine = inputLine;
                    service.submit(() -> {
                        Database.processScript(newLine, writer);
                    });
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, "FastDBServer.start :: Client disconnected: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "FastDBServer.start :: Client disconnected: " + e.getMessage());
        }
    }
}
