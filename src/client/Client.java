package client;

import models.Common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

    private Socket socket = null;
    private InputStream in = null ;
    private OutputStream out = null;
    private volatile boolean running;

    String hostAdress;

    public Client(){
        hostAdress = Common.HOST_NAME;
    }

    public Client(String hostAdress){
        this.hostAdress = hostAdress;
    }

    /**
     * Connect to the distant server
     */
    public void connect(){
        try {
            socket = new Socket(Common.HOST_NAME, Common.PORT);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            running = true;
            listen();
            write();
        } catch (ConnectException e){
            reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reconnect() {
        System.out.println("Erreur de connexion, voulez-vous retenter une connexion ? (Y-N)");
        Scanner scanner = new Scanner(System.in);
        if(scanner.nextLine().toUpperCase().equals("Y")){
            connect();
        }
    }

    /**
     * Starts a thread in order to read data from the server
     */
    public void listen(){
        Thread t = new Thread(() -> {
            while (running){
                int bufferSize;
                byte[] buffer = new byte[Common.BUFFER_SIZE];
                try {
                    bufferSize = in.read(buffer);
                    //Check la taille du buffer car si le client quitte la taille sera de -1
                    if(bufferSize > 0){
                        String data = new String(buffer, 0, bufferSize).trim();
                        System.out.println(data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setName("Client-Listen");
        t.start();
    }

    /**
     * Starts a thread in order to write to the server.
     */
    public void write(){
        Thread t = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String msg;
            while(running){
                try {
                    msg = scanner.nextLine();
                    send(msg);
                }catch (NoSuchElementException e){
                    //Si le client quitte avec ctrl+c
                    send(Common.QUIT_COMMAND);
                }
            }
        });
        t.setName("Client-Write");
        t.start();
    }


    public void send(String msg){
        byte[] buffer = msg.getBytes();
        try {
            out.write(buffer);
            if (msg.toUpperCase().equals(Common.QUIT_COMMAND)){
                quit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void quit(){
        running = false;
    }

}
