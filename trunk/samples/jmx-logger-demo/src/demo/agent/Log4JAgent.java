package demo.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author vladimir
 */
public class Log4JAgent {
    private static Logger logger = Logger.getLogger(Log4JAgent.class);
    private static String[] messages = {
        "I am happy!",
        "I am concerned...",
        "I am in trouble, something went wrong.",
        "I am up, I am down, I am all around!"
    };
    private static Level[] levels = {Level.INFO, Level.WARN, Level.ERROR, Level.DEBUG, Level.TRACE, Level.FATAL};
    public static void main (String[] args) {
        for(;;){
            int nextMsg = new Random().nextInt(4);
            int nextLevel = new Random().nextInt(6);
            logger.log(levels[nextLevel], messages[nextMsg]);
            try {
                Thread.currentThread().sleep(700);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(Log4JAgent.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }
}
