package server;

import models.Common;
import models.Game;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private ServerSocket ss = null;
    private String host;
    private int port;
    private boolean running;
    private Game game;
    private GameManager gameManager;

    private ConcurrentHashMap<String, PlayerManager> playerManagerMap;

    public Server(){
        host = Common.HOST_NAME;
        port = Common.PORT;
        playerManagerMap = new ConcurrentHashMap<>();
        game = new Game();
    }

    public void start(){
        try {
            InetAddress hostAdress = InetAddress.getByName(host);
            ss = new ServerSocket(port, 0, hostAdress);
            running = true;
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(){
        running = true;
        System.out.println("Serveur en écoute..");
        Socket clientSocket;
        while (running){
            try {
                clientSocket = ss.accept();
                System.out.println("Nouvelle connexion : " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                Thread t = new Thread(new PlayerManager(this, clientSocket));
                t.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ConcurrentHashMap<String, PlayerManager> getPlayerManagerMap() {
        return playerManagerMap;
    }

    public synchronized Game getGame() {
        return game;
    }

    public synchronized GameManager getGameManager() {
        return gameManager;
    }

    public void startGame(){
        this.gameManager =  new GameManager(this, game);
        Thread t = new Thread(gameManager);
        t.setName("Thread-Game");
        t.start();
    }


    /**
     * Send a message to every client connected to the server.
     * @param msg
     */
    public void sendAll(String msg){

        byte[] buffer = msg.getBytes();

        for(String key : getPlayerManagerMap().keySet()){
            getPlayerManagerMap().get(key).send(buffer);
        }
    }
}
