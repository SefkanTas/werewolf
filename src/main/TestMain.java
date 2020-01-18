package main;

import models.Common;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestMain {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger("test");

        logger.info("salut");

        /*
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(new Date());
                timer.cancel();
            }
        }, 1000);
        */


        String data = "::VOTE   salut  ";
        String res = data.substring(Common.VOTE_COMMAND.length()).trim();
        System.out.println("res=" + res + "end");
        logger.info(res);
        bonjour();
    }

    public static void bonjour(){
        Logger logger = Logger.getLogger("haha");
        logger.warning("c'est dangereux");
        logger.log(Level.WARNING, "e", "a");
    }
}
