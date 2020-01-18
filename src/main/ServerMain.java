package main;

import server.Server;

import java.util.Scanner;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        server.start();

    }
}
