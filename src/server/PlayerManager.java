package server;

import exceptions.InvalidUsernameException;
import exceptions.UsernameIsTakenException;
import models.Common;
import models.MyLogger;
import models.Player;
import models.Role;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class PlayerManager implements Runnable {

    private MyLogger logger = new MyLogger("models.PlayerManger", true);

    private Server server;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Boolean running;
    private Player player;

    public PlayerManager(Server server, Socket socket){
        this.server = server;
        this.socket = socket;
        player = new Player();
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = true;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void run() {
        String dataReceived;
        askForUsername();
        while(running){
            dataReceived = listen();
            processData(dataReceived);
        }
    }

    /**
     * Retourne la donnée sous un string
     * @return
     */
    private String listen(){
        byte[] buffer = new byte[Common.BUFFER_SIZE];
        int bufferLength = 0;
        try {
            bufferLength = in.read(buffer);
        }catch (SocketException e) {
            String errorMsg = socket.getInetAddress() + " " + player.getUsername() + " terminated the program";
            logger.log("listen", errorMsg);
            quit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buffer, 0, bufferLength).trim();
    }

    private void askForUsername(){
        logger.log("Asking for username");
        boolean usernameIsSet = false;
        String dataReceived;

        while (running && !usernameIsSet){
            send("Quel est votre nom d'utilisateur ?");
            try {
                dataReceived = listen();

                if(isQuitting(dataReceived)){
                    quit();
                    return;
                }

                if(dataReceived.isBlank()){
                    throw new InvalidUsernameException();
                }

                if(server.getPlayerManagerMap().containsKey(dataReceived))
                    throw new UsernameIsTakenException();

                server.getPlayerManagerMap().put(dataReceived, this);
                player.setUsername(dataReceived);
                usernameIsSet = true;
                logger.log("askForUsername", player.getUsername() + " connected");
                send("Bonjour " + player.getUsername() + " !");
                if(!server.getGame().isPlaying()){
                    sendAll(player.getUsername() + " est en ligne.");
                }
                else {
                    send("Une partie est deja en cours, vous ne pouvez pas jouer pour l'instant.");
                }

                if(server.getPlayerManagerMap().size() == server.getGame().getNbPlayers()){
                    server.startGame();
                }
            }
            catch (InvalidUsernameException e) {
                logger.log(e.toString());
                send(e.toString());
            }
            catch (UsernameIsTakenException e) {
                //e.printStackTrace();
                logger.log(e.toString());
                send(e.toString());
            }
        }
    }

    public void processData(String dataReceived){
        logger.log(player.getUsername() + " sent " + dataReceived);
        if(dataReceived.isBlank()){
            return;
        }
        if(isQuitting(dataReceived)){
            quit();
            return;
        }
        server.getGameManager().processData(this, dataReceived);
    }

    public void send(byte[] buffer){
        try {
            out.write(buffer);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String msg)  {
        byte[] buffer = msg.getBytes();
        send(buffer);
    }

    /**
     * Send a message to every client connected to the server except the
     * client that is managed by the current object.
     * @param msg
     */
    public void sendAll(String msg){

        byte[] buffer = msg.getBytes();

        for(String key : server.getPlayerManagerMap().keySet()){
            if(!key.equals(player.getUsername())){
                server.getPlayerManagerMap().get(key).send(buffer);
            }
        }
    }

    /**
     * Close connection and remove client from servers client list.
     */
    private void quit(){
        System.out.println(player.getUsername() + " is quitting");
        try {
            socket.close();
            running = false;
            if (player.getUsername() != null && server.getPlayerManagerMap().containsKey(player.getUsername())){
                server.getPlayerManagerMap().remove(player.getUsername());
            }
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifie si le client veut quitter.
     *
     * @param dataReceived
     * @return
     */
    private boolean isQuitting(String dataReceived){
        return dataReceived.toUpperCase().equals(Common.QUIT_COMMAND);
    }

    /**
     * Affiche le role du joueur sur le client.
     */
    public void displayRole(){
        String playerRole = player.getRole().toString();
        logger.log("displayRole", player.getUsername() + " is " + playerRole);
        send("Votre role est : " + playerRole);
        displayWerewolvesPartner();
    }

    /**
     *  Affiche la liste des autres LG si le joueur est LG
     */
    private void displayWerewolvesPartner(){
        if(player.getRole() == Role.WEREWOLF){
            StringBuilder sb = new StringBuilder("Les autres loups-garous sont : ");
            server.getGame().getWerewolfList().forEach(p -> {
                if(!p.getUsername().equals(player.getUsername())){
                    sb.append(p.getUsername()).append(", ");
                }
            });
            send(sb.toString());
        }
    }

}
