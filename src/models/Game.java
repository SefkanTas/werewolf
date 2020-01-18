package models;

import server.PlayerManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private CopyOnWriteArrayList<Player> seerList;
    private CopyOnWriteArrayList<Player> witchList;
    private CopyOnWriteArrayList<Player> innocentList;

    public Game(){
        nbPlayers = 4; //7
        nbWerewolves = 2; //2
        nbSeers = 0; //1
        nbWitches = 0; //1
        nbVillagers = nbPlayers - (nbWerewolves + nbSeers + nbWitches);
        nbWerewolvesAlive = nbWerewolves;
        nbInnocentAlive = nbSeers + nbWitches + nbVillagers;

        playerList = new CopyOnWriteArrayList<>();
        werewolfList = new CopyOnWriteArrayList<>();
        villagerList = new CopyOnWriteArrayList<>();
        seerList = new CopyOnWriteArrayList<>();
        witchList = new CopyOnWriteArrayList<>();
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

        setSpecificRole(playerLeft, werewolfList, Role.WEREWOLF, nbWerewolves);
        setSpecificRole(playerLeft, seerList, Role.SEER, nbSeers);
        setSpecificRole(playerLeft, witchList, Role.WITCH, nbWitches);
        setSpecificRole(playerLeft, villagerList, Role.VILLAGER, nbVillagers);
    }

    /**
     * Donne un role aléatoirement aux joueurs de la partie.
     * @param playerLeft
     * @param roleList
     * @param role
     * @param nbPlayer
     */
    private void setSpecificRole( CopyOnWriteArrayList<Player> playerLeft,
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

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public int getNbPlayers() {
        return nbPlayers;
    }

    public CopyOnWriteArrayList<Player> getWerewolfList() {
        return werewolfList;
    }

    public CopyOnWriteArrayList<Player> getSeerList() {
        return seerList;
    }

    public CopyOnWriteArrayList<Player> getInnocentList() {
        return innocentList;
    }


    public int getNbWerewolvesAlive() {
        return nbWerewolvesAlive;
    }

    public int getNbInnocentAlive() {
        return nbInnocentAlive;
    }
}
