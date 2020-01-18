package models;

import server.PlayerManager;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Game {
    private String gameName;
    private int nbPlayers;
    private int nbWerewolves;
    private int nbVillagers;
    private int nbSeers;
    private int nbWitches;
    private int nbWerewolvesAlive;
    private int nbInnocentAlive;
    private boolean isPlaying;

    private CopyOnWriteArrayList<Player> playerList;
    private CopyOnWriteArrayList<Player> werewolfList;
    private CopyOnWriteArrayList<Player> villagerList;
    private CopyOnWriteArrayList<Player> innocentList;
    private Player seer;
    private Player witch;

    public Game(){
        nbPlayers = 4; //7
        nbWerewolves = 2; //2
        nbSeers = 1; //1
        nbWitches = 0; //1
        nbVillagers = nbPlayers - (nbWerewolves + nbSeers + nbWitches);
        nbWerewolvesAlive = nbWerewolves;
        nbInnocentAlive = nbSeers + nbWitches + nbVillagers;

        playerList = new CopyOnWriteArrayList<>();
        werewolfList = new CopyOnWriteArrayList<>();
        villagerList = new CopyOnWriteArrayList<>();
        innocentList = new CopyOnWriteArrayList<>();

        isPlaying = false;
    }

    public void startGame(ConcurrentHashMap<String, PlayerManager> playerManagerMap){
        if (isPlaying){
            return;
        }

        //set player list
        for(String key : playerManagerMap.keySet()){
            Player p = playerManagerMap.get(key).getPlayer();
            playerList.add(p);
        }

        setRoles();
        playerList.forEach(p -> {
            p.setAlive(true);
            p.setInGame(true);
        });
        isPlaying = true;
    }

    public void werewolvesVotes(){

    }

    private void setRoles(){
        CopyOnWriteArrayList<Player> playerLeft = new CopyOnWriteArrayList<>(playerList);

        setListRole(playerLeft, werewolfList, Role.WEREWOLF, nbWerewolves);
        seer = setRole(playerLeft, Role.SEER);
        //setRole(playerLeft, witch, Role.WITCH);
        setListRole(playerLeft, villagerList, Role.VILLAGER, nbVillagers);
    }

    /**
     * Donne un role aléatoirement aux joueurs de la partie pour les rôles ayant plusieurs joueurs.
     * @param playerLeft la liste des joueurs n'ayant pas de rôle
     * @param roleList liste de joueurs d'un certain rôle
     * @param role le rôle a assigner
     * @param nbPlayer le nombre de joueur nécessaire pour ce rôle
     */
    private void setListRole( CopyOnWriteArrayList<Player> playerLeft,
                                  CopyOnWriteArrayList<Player> roleList, Role role, int nbPlayer){
        int rand;
        for (int i = nbPlayer; i>0; i--){
            rand = (int)(Math.random() * playerLeft.size());
            playerLeft.get(rand).setRole(role);
            roleList.add(playerLeft.get(rand));
            if (role != Role.WEREWOLF){
                innocentList.add(playerLeft.get(rand));
            }
            playerLeft.remove(rand);
        }
    }

    /**
     * Donne un rôle pour les rôles ne nécessitant qu'un seul joueur
     * @param playerLeft la liste des joueurs n'ayant pas de rôle
     * @param role le rôle a assigner
     */
    private Player setRole(CopyOnWriteArrayList<Player> playerLeft, Role role){
        Player p;
        int rand = (int)(Math.random() * playerLeft.size());
        p = playerLeft.get(rand);
        p.setRole(role);
        innocentList.add(p);
        playerLeft.remove(rand);
        return p;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getNbPlayers() {
        return nbPlayers;
    }

    public CopyOnWriteArrayList<Player> getPlayerList() {
        return playerList;
    }

    public CopyOnWriteArrayList<Player> getWerewolfList() {
        return werewolfList;
    }

    public CopyOnWriteArrayList<Player> getInnocentList() {
        return innocentList;
    }

    public Player getSeer() {
        return seer;
    }

    public Player getWitch() {
        return witch;
    }

    public int getNbWerewolvesAlive() {
        return nbWerewolvesAlive;
    }

    public int getNbInnocentAlive() {
        return nbInnocentAlive;
    }

    public ArrayList<Player> getPlayerExcept(Role role){
        return (ArrayList<Player>) getPlayerList().stream()
                .filter(e -> e.getRole() != role)
                .collect(Collectors.toList());
    }
}
