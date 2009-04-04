package jmxlogger.tools;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * @author vladimir.vivien
 */
public class JmxEventLogger {
    private volatile boolean started;
    private MBeanServer server;
    private ObjectName objectName;
    private JmxLogEmitter logMBean;
    private static String DEFAULT_NAME = "jmx.logger:type=logging";
    
    private JmxEventLogger() {
        logMBean = new JmxLogEmitter();
    }
    public static JmxEventLogger createInstance(){
        return new JmxEventLogger();
    }

    public synchronized void setMBeanServer(MBeanServer server){
        this.server = server;
    }

    public synchronized MBeanServer getMBeanServer() {
        if(server == null)
            server = ManagementFactory.getPlatformMBeanServer();
        return server;
    }

    public synchronized void setObjectName(ObjectName name){
        objectName = name;
    }

    public synchronized ObjectName getObjectName() {
        if(objectName == null){
            objectName = buildObjectName(DEFAULT_NAME + "@" + hashCode());
        }
        return objectName;
    }

    public synchronized void start(){
        if(started) return;
        registerLoggingMBean();
        started = true;
    }

    public synchronized void stop(){
        if(!started) return;
        unregisterLoggingMBean();
        started = false;
    }

    public synchronized boolean isStarted(){
        return started;
    }
    
    public synchronized void log(LogEvent event){
        if(!started){
            throw new IllegalStateException("JmxEventLogger has not been started." +
                    "Call JmxEventLogger.start() before you log messages.");
        }
        logMBean.sendLog(event);
    }

    private ObjectName buildObjectName(String name){
        ObjectName objName = null;
        try {
            objName = new ObjectName(name);
        } catch (MalformedObjectNameException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        return objName;
    }


    private void registerLoggingMBean() {
        try {
            if (getMBeanServer().isRegistered(getObjectName())) {
                getMBeanServer().unregisterMBean(getObjectName());
            }
            getMBeanServer().registerMBean(logMBean, getObjectName());
            logMBean.start();
        } catch (InstanceAlreadyExistsException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotCompliantMBeanException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstanceNotFoundException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MBeanRegistrationException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void unregisterLoggingMBean() {
        try {
            getMBeanServer().unregisterMBean(getObjectName());
        } catch (InstanceNotFoundException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MBeanRegistrationException ex) {
            Logger.getLogger(JmxEventLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
