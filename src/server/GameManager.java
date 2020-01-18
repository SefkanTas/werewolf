package server;

import exceptions.InvalidVoteException;
import exceptions.NotYourTurnException;
import models.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager implements Runnable {

    MyLogger logger = new MyLogger("server.GameManager", true);

    private Server server;
    private Game game;
    private Role turn;

    private ArrayList<Player> playerListForSeer;
    private Map<Player, Integer> voteMap;
    private Timer turnTimer;
    private int werewolvesTurnDuration = 30;
    private int seerTurnDuration = 30;


    public GameManager(Server server, Game game){
        this.server = server;
        this.game = game;
    }

    @Override
    public void run() {
        game.startGame(server.getPlayerManagerMap());
        server.sendAll("\n\nLa partie commence !");
        displayRoles();
        //while (game.isPlaying()){
            werewolvesfTurn();
        //}
    }

    /**
     * Affiche le role de chaque joueur individuellement.
     */
    private void displayRoles(){
        for(String username : server.getPlayerManagerMap().keySet()){
            server.getPlayerManagerMap().get(username).displayRole();
        }
    }

    public void processData(PlayerManager pm, String dataReceived){
        try {
            if (turn == Role.WEREWOLF){
                processWerewolvesData(pm, dataReceived);
            }
            else if (turn == Role.SEER){
                processSeerData(pm, dataReceived);
            }
        } catch (NotYourTurnException e) {
            logger.log("processData", pm.getPlayer().getUsername() + " " + e.toString());
            pm.send("Ce n'est pas à votre tour de jouer");
        } catch (InvalidVoteException e) {
            logger.log("processData", pm.getPlayer().getUsername() + " " + e.toString());
            pm.send("Votre vote est invalide");
        }
    }


    private void processWerewolvesData(PlayerManager pm, String dataReceived) throws NotYourTurnException,
            InvalidVoteException {

        if(pm.getPlayer().getRole() != Role.WEREWOLF){
            throw new NotYourTurnException();
        }

        if(dataReceived.toUpperCase().startsWith(Common.VOTE_COMMAND)){
            werewolfAddVote(pm, dataReceived);
        }
        else if(dataReceived.toUpperCase().startsWith(Common.REMOVE_VOTE_COMMAND)){
            werewolfRemoveVote(pm);
        }
        else if(dataReceived.toUpperCase().startsWith(Common.VOTE_LIST_COMMAND)){
            werewolfRemoveVote(pm);
        }
        else {
            sendDataToAllWerewolves(pm.getPlayer().getUsername() + " : " + dataReceived);
        }
    }

    /**
     * LG qui vote
     * @param pm
     * @param dataReceived
     * @throws InvalidVoteException
     */
    private void werewolfAddVote(PlayerManager pm, String dataReceived) throws InvalidVoteException {
        String voteString = dataReceived.substring(Common.VOTE_COMMAND.length()).trim();

        if(!isVoteValid(voteString)){
            throw new InvalidVoteException();
        }

        //Si le LG a déjà voté
        if(voteMap.containsKey(pm.getPlayer())){
            voteMap.remove(pm.getPlayer());
            sendDataToAllWerewolves(pm.getPlayer().getUsername() + " change de vote.");
        }

        int voteInt = Integer.parseInt(voteString) - 1;
        voteMap.put(pm.getPlayer(), voteInt);
        sendDataToAllWerewolves(pm.getPlayer().getUsername() + " a vote pour tuer " + game.getInnocentList().get(voteInt).getUsername());
        sendVoteCountToWerewolves();
        if (voteMap.keySet().size() == game.getNbWerewolvesAlive()){
            nextTurn();
        }
    }

    /**
     * LG qui retire son vote
     * @param pm
     */
    private void werewolfRemoveVote(PlayerManager pm){
        if(voteMap.containsKey(pm.getPlayer())){
            voteMap.remove(pm.getPlayer());
            sendDataToAllWerewolves(pm.getPlayer().getUsername() + " retire son vote");
            sendVoteCountToWerewolves();
        }
    }

    private void nextTurn(){
        if(turn == Role.VILLAGER){
            werewolvesfTurn();
        }
        else if(turn == Role.WEREWOLF){
            turnTimer.cancel();
            seerTurn();
        }
        else if(turn == Role.SEER){
            turnTimer.cancel();

        }
    }

    /**
     * Initialise le tour des loups
     */
    private void werewolvesfTurn(){
        turn = Role.WEREWOLF;
        server.sendAll("Le village s'endort et les loups se reveillent...");
        sendVoteMenuToWerewolves();
        voteMap = new ConcurrentHashMap<>();
        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                nextTurn();
            }
        }, werewolvesTurnDuration * 1000);
    }

    /**
     * Envoie le "menu" de vote à chaque loups-garoups.
     */
    private void sendVoteMenuToWerewolves(){
        StringBuilder sb = new StringBuilder();
        sb.append("Tu as " + werewolvesTurnDuration + " secondes pour voter pour tuer un de ces joueurs : \n");
        for (int i = 0; i<game.getInnocentList().size(); i++){
            sb.append(i+1).append(" : ").append(game.getInnocentList().get(i).getUsername()).append("\n");
        }
        sendDataToAllWerewolves(sb.toString());
    }

    private void sendVoteCountToWerewolves(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<game.getInnocentList().size(); i++){
            int finalI = i;
            long voteCount = voteMap.entrySet().stream()
                    .filter(e -> e.getValue() == finalI)
                    .count();

            sb.append(i+1).append(" : ")
                    .append(game.getInnocentList().get(i).getUsername())
                    .append(" --> ")
                    .append(voteCount)
                    .append(" vote(s)")
                    .append("\n");
        }
        sendDataToAllWerewolves(sb.toString());
    }

    private void sendDataToAllWerewolves(String dataToSend){
        game.getWerewolfList().forEach(player -> {
            server.getPlayerManagerMap().get(player.getUsername()).send(dataToSend);
        });
    }

    /**
     * Initialise le tour des voyantes
     */
    private void seerTurn(){
        turn = Role.SEER;
        server.sendAll("C'est au tour de la voyante");
        sendDataToSeer("Vous avez 30 secondes pour regarder le role d'un joueur de votre choix.");
        sendVoteMenuToSeer();
        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                nextTurn();
            }
        }, seerTurnDuration * 1000);
    }

    private void sendVoteMenuToSeer(){
        playerListForSeer = game.getPlayerExcept(Role.SEER);
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i<playerListForSeer.size(); i++){
            sb.append(i+1)
                    .append(" : ")
                    .append(game.getPlayerList().get(i).getUsername())
                    .append("\n");
        }
        sendDataToSeer(sb.toString());
    }

    private void processSeerData(PlayerManager pm, String dataReceived) throws NotYourTurnException,
            InvalidVoteException {

        if(pm.getPlayer().getRole() != Role.SEER){
            throw new NotYourTurnException();
        }

        if(!isVoteValid(dataReceived)){
            throw new InvalidVoteException();
        }

        int voteInt = Integer.parseInt(dataReceived) - 1;
        Player targetedPlayer = playerListForSeer.get(voteInt);
        StringBuilder sb = new StringBuilder();
        sb.append("Le role de ")
                .append(targetedPlayer.getUsername())
                .append(" est ")
                .append(targetedPlayer.getRole())
                .append(".");
        pm.send(sb.toString());

        nextTurn();
    }

    private void sendDataToSeer(String dataToSend){
        server.getPlayerManagerMap().get(game.getSeer().getUsername()).send(dataToSend);
    }

    /**
     * Check si un vote est valide
     * @param dataReceived
     * @return
     * @throws NumberFormatException
     */
    private boolean isVoteValid(String dataReceived) throws NumberFormatException{
        int vote;
        try{
            vote = Integer.parseInt(dataReceived);
        }
        catch (NumberFormatException e){
            return false;
        }
        if(vote <= 0){
            return false;
        }
        if (turn == Role.WEREWOLF && vote <= game.getInnocentList().size() ){
            return true;
        }
        else if(turn == Role.SEER && vote <= playerListForSeer.size()){
            return true;
        }
            return false;
    }
}
