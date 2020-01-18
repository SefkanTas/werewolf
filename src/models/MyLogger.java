package models;

public class MyLogger {

    private boolean debug;
    private String name;

    public MyLogger(String name, boolean debug){
        this.name = "------------------- " + name;
        this.debug = debug;
    }

    public void log(String msg){
        if (debug){
            String log = name + " : " + msg;
            System.out.println(log);
        }
    }

    public void log(String methodName, String msg){
        if (debug){
            String log = name + "." +methodName + " : " + msg;
            System.out.println(log);
        }
    }

}
