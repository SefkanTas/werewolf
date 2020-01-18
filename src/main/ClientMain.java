package main;

import client.Client;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {

        Client client = new Client();
        client.connect();
    }
}
