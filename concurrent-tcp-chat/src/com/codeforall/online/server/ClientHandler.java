package com.codeforall.online.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Set;



/**
 * This class handles communication with a single client.
 */
public class ClientHandler extends Thread{
    private Socket socket;
    private Set<PrintWriter> clientWriters;
    private Map<String, PrintWriter> clientMap; //The map of usernames to PrintWriter instances for all connected clients.
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;


    /**
     * Constructs a new ClientHandler instance.
     *
     * @param socket        The socket for communicating with the client.
     * @param clientWriters The set of PrintWriter instances for all connected clients.
     * @param clientMap     The map of usernames to PrintWriter instances for all connected clients.
     */

    public ClientHandler(Socket socket, Set<PrintWriter> clientWriters, Map<String, PrintWriter> clientMap) {
        this.socket = socket;
        this.clientWriters = clientWriters;
        this.clientMap = clientMap; //The map of usernames to PrintWriter instances for all connected clients.

    }

    /**
     * Runs the client handler thread, listening for incoming messages and broadcasting them to all clients.
     */
    public void run() {
        try {
            // Initialize input and output streams for the socket.
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Synchronized block to safely add the writer to the shared set of client writers.
            synchronized (clientWriters) {
                clientWriters.add(writer);
            }

            while (true) {
                writer.println("Enter your name: ");
                username = reader.readLine();

                if (username == null || username.trim().isEmpty()) {
                    writer.println("Username Cannot be empty");
                    continue;
                }
                // Synchronized block to safely add the username and writer to the shared map of clients.
                synchronized (clientMap) {
                    if (clientMap.containsKey(username)) {
                        writer.println((username + " is already in use"));
                    } else {
                        clientMap.put(username, writer);
                        break;
                    }
                }
            }

            // Broadcast to all clients that a new user has joined.
            Server.broadcastMessage("Server: " + username + " has joined the chat");

            CommandHandler commandHandler = new CommandHandler(username, writer, clientWriters, clientMap, this);

            String message;

            // Continuously read messages from the client.
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("/quit")) {
                    break;
                }

                if (message.startsWith("/")) {
                    commandHandler.handleCommand(message);
                } else {
                    Server.broadcastMessage(username + ": " + message);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            // Clean up resources and update the shared collections.
            closeResources();
            synchronized (clientWriters) {
                clientWriters.remove(writer);
            }
            synchronized (clientMap) {
                clientMap.remove(username);
            }
            Server.broadcastMessage("Server: " + username + " has left the chat");
        }
    }

    /**
     * closing all resources related to client handler
     */
    private void closeResources() {
        try {
            if (reader != null) {
            reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing resources" + e.getMessage());
        }

    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }
}
