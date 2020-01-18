package models;

public class Common {

    private Common(){}

    public static final String HOST_NAME = "localhost";
    //public static final String HOST_NAME = "192.169.1.19";
    //public static final String HOST_NAME = "0.0.0.0";
    public static final int PORT = 10000;
    public static final int BUFFER_SIZE = 1024;

    private static final String COMMAND = "::";
    public static final String QUIT_COMMAND = COMMAND + "QUIT";
    public static final String VOTE_COMMAND = COMMAND + "VOTE";
    public static final String REMOVE_VOTE_COMMAND = COMMAND + "RVOTE";
    public static final String VOTE_LIST_COMMAND = COMMAND + "VOTELIST";

}
