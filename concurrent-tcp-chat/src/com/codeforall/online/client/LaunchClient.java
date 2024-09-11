package com.codeforall.online.client;

import java.io.*;
import java.net.Socket;

public class LaunchClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 7777;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to chat server");
            new Thread(new Client(in)).start();

            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                out.println(userInput);
                if (userInput.equalsIgnoreCase("/quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
