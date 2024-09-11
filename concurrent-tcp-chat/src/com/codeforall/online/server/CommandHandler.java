package com.codeforall.online.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

/**
 * This class handles user commands sent by clients in the chat application.
 */
public class CommandHandler {

    private String username; // The username of the client
    private final PrintWriter writer; // The PrintWriter to send messages to the client
    private final Set<PrintWriter> clientWriters; // The set of PrintWriter instances for all connected clients
    private final Map<String, PrintWriter> clientMap; //The map of usernames to PrintWriter instances for all connected clients
    private ClientHandler clientHandler;

    public CommandHandler(String username, PrintWriter writer, Set<PrintWriter> clientWriters, Map<String, PrintWriter> clientMap, ClientHandler clientHandler) {
        this.username = username;
        this.writer = writer;
        this.clientWriters = clientWriters;
        this.clientMap = clientMap;
        this.clientHandler = clientHandler;
    }

    /**
     * Handles a command sent by a client.
     *
     * @param message The command message.
     */
    public void handleCommand(String message) {

        if (message.startsWith("/")) {
            // Split the message to extract the command
            String[] tokens = message.split(" ", 3);
            String command = tokens[0].toLowerCase();

            switch (command) {
                case "/list":
                    listUsers();
                    break;
                case "/whisper":
                    if (tokens.length >= 3) {
                        whisper(tokens[1], tokens[2]);
                    } else {
                        writer.println("Usage: /whisper <username> <message>");
                    }
                    break;
                case "/name":
                    if (tokens.length == 2) {
                        changeUserName(tokens[1]);
                    } else {
                        writer.println("Usage: /name <new_username>");
                    }
                    break;
                case "/help":
                    showHelp();
                    break;
                default:
                    writer.println("Unknown command. Type /help for more information.");
                    break;
            }
        } else {
            Server.broadcastMessage(username + ": " + message);
        }
    }

    /**
     * Lists all connected users.
     */
    private void listUsers() {
        writer.println("Connected users:");
        synchronized (clientMap) {
            for (String user : clientMap.keySet()) {
                writer.println(user);
            }
        }
    }

    /**
     * Changes the username of the client.
     *
     * @param newUsername The new username.
     */
    private void changeUserName(String newUsername) {
        synchronized (clientMap) {
            if (clientMap.containsKey(newUsername)) {
                writer.println("Username " + newUsername + " is already taken.");
            } else {
                clientMap.remove(username); //remove old username
                clientMap.put(newUsername, writer); //Add new username
                writer.println("Username changed to " + newUsername);
                Server.broadcastMessage("Server: " + username + " has changed their username to " + newUsername);
                clientHandler.setUsername(newUsername);
                this.username = newUsername;
            }
        }
    }

    /**
     * Sends a private message to a specific user.
     *
     * @param targetUser The username of the target user.
     * @param message    The message to send.
     */
    private void whisper(String targetUser, String message) {
        PrintWriter targetWriter;
        synchronized (clientMap) {
            targetWriter = clientMap.get(targetUser);
        }
        if (targetWriter != null) {
            targetWriter.println("[Whispering from " + username + "] " + message);
        } else {
            writer.println("User " + targetUser + " does not exist.");
        }
    }

    /**
     * Shows the help message with available commands.
     */
    private void showHelp() {
        writer.println("Available commands:");
        writer.println("/list - List all connected users");
        writer.println("/whisper <username> <message> - Send a private message to a user");
        writer.println("/name <new_username> - Change your username");
        writer.println("/help - Show this help message");
    }
}
