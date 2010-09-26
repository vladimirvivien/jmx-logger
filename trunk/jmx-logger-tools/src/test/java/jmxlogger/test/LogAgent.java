/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import jmxlogger.tools.JmxLogService;
import jmxlogger.tools.ToolBox;

/**
 *
 * @author vladimir
 */
public class LogAgent {
    private Registry reg;
    private JMXConnectorServer server;
    private JmxLogService logService;
    private LogGenerator logGen;
    private ObjectName loggerName;
    private int port;

    public LogAgent(ObjectName name, int port){
        this.loggerName = name;
        this.port = port;
    }

    public void start() throws Exception{
        System.setProperty("java.rmi.server.randomIDs", "true");
        System.out.println("Starting LogAgent on RMI port " + port);
        reg = LocateRegistry.createRegistry(port);

        // setup log service
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        logService = JmxLogService.createInstance();
        logService.getDefaultConfigurationStore().putValue(ToolBox.KEY_CONFIG_JMX_SERVER, mbs);
        logService.getDefaultConfigurationStore().putValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME, loggerName);
        logService.start();
        System.out.println ("Exposing LogEmitter bean with name " + loggerName);


        // export connector server
        HashMap<String,Object> env = new HashMap<String,Object>();
        env.put("jmx.remote.x.password.file", "../jmx-password.properties");
        env.put("jmx.remote.x.access.file", "../jmx-access.properties");
        System.out.println("Create an RMI connector server");
        JMXServiceURL url =
            new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + port + "/jmxrmi");
        server = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
        System.out.println("LogAgent RMI connector started with url " + url.toString());
        server.start();

        // start generating test logs
        logGen = new LogGenerator(logService);
        logGen.start();
    }

    public void stop() throws Exception {
        System.out.println("Shutting down server and RMI");
        if(logGen != null) logGen.stop();
        server.stop();
        UnicastRemoteObject.unexportObject(reg, true);
    }

    public static String LOGGER_NAME = "jmxlogger:type=LogEmitter";
    public static void main(String[] args) throws Exception {
        new LogAgent(ToolBox.buildObjectName(LOGGER_NAME), 7070).start();
    }
}

class LogGenerator {

    private static AtomicLong counter = new AtomicLong();
    private static Map<String, String> logs = new HashMap<String, String>();
    private static Object[] entries;
    static {
        logs.put("INFO", "I am happy!");
        logs.put("WARNING", "I am concerned...");
        logs.put("WARN", "I am concerned...");
        logs.put("SEVERE", "I am in trouble, something went wrong.");
        logs.put("ERROR", "I am in trouble, something went wrong.");
        logs.put("FATAL", "O crap, it's over, help!");
        logs.put("FINE", "I am up, I am down, I am all around!");
        logs.put("DEBUG", "I am up, I am down, I am all around!");
        logs.put("TRACE", "I went to the store, bought cake, drove back, now I am here.");
        entries = logs.entrySet().toArray();
    }

    private JmxLogService logger;
    public LogGenerator(JmxLogService svc) {
        logger = svc;
    }

    private volatile boolean started;
    public void start() {
        started = true;
        new Thread(new Runnable() {
            public void run() {
                while (started) {
                    int next = next = new Random().nextInt(logs.size());
                    Map.Entry<String, String> entry = (Entry<String, String>) entries[next];
                    Map<String, Object> event = prepareLogEvent(entry.getKey(), entry.getValue());
                    logger.log(event);
                    try {
                        Thread.currentThread().sleep(new Random().nextInt(3) * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LogGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    public void stop(){
        started = false;
    }
    
    private static Map<String, Object> prepareLogEvent(String level, String msg) {
        Map<String, Object> event = new HashMap<String, Object>();
        event.put(ToolBox.KEY_EVENT_SOURCE, LogGenerator.class.getName());
        event.put(ToolBox.KEY_EVENT_LEVEL, level);
        event.put(ToolBox.KEY_EVENT_LOGGER, "TestLogger");
        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, "<b>" + msg + "</b>");
        event.put(ToolBox.KEY_EVENT_RAW_MESSAGE, msg);
        event.put(ToolBox.KEY_EVENT_SEQ_NUM, counter.getAndIncrement());
        event.put(ToolBox.KEY_EVENT_SOURCE_CLASS, LogGenerator.class.getName());
        event.put(ToolBox.KEY_EVENT_SOURCE_METHOD, "testMethod()");
        event.put(ToolBox.KEY_EVENT_SOURCE_THREAD, new Long(Thread.currentThread().getId()));
        event.put(ToolBox.KEY_EVENT_THROWABLE, "Exception");
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(System.currentTimeMillis()));

        return event;
    }
}
