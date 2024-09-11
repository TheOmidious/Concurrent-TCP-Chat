package com.codeforall.online.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class represents the server in the online chat application.
 */
public class Server {

    private static final int port = 7777;
    private static Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet(); // Thread-safe set of client writers
    private static Map<String, PrintWriter> clientMap = new ConcurrentHashMap<>(); // Thread-safe map of usernames to client writers

    /**
     * Starts a new server and listens for incoming client connections.
     */
    public static void newServer() {
        try {
            // Create a new ServerSocket to listen for client connections
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Server started: " + serverSocket);
            System.out.println("Listening on port " + port);

            // Start a new thread to listen for server console input and broadcast messages
            new Thread(() -> {
                try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
                    String serverMessage;
                    while ((serverMessage = consoleReader.readLine()) != null) {
                        broadcastMessage("Server: " + serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Continuously accept new client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientWriters, clientMap);
                clientHandler.start();
            }

        } catch (IOException e) {
            System.out.println("Server failed to listen on port " + port);
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to broadcast.
     */
    public static void broadcastMessage(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                writer.println(message);
            }
        }
        System.out.println(message);  // Also print to server console
    }
}
