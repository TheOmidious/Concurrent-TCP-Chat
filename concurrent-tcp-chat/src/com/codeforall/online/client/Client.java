package com.codeforall.online.client;

import java.io.BufferedReader;
import java.io.IOException;

public class Client implements Runnable {

    private BufferedReader input; //Bufferreder to read messages from the client

    /**
     * Constructs a new Client instance.
     *
     * @param input The BufferedReader to read messages from the server.
     */
    public Client(BufferedReader input) {
        this.input = input;
    }

    @Override
    public void run() {
        String line;

        try {
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
