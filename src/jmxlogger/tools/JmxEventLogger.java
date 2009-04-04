package jmxlogger.tools;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author vladimir.vivien
 */
public class JmxEventLogger {
    private volatile boolean started;
    private MBeanServer server;
    private ObjectName objectName;
    private JmxLogEmitter logMBean;
    
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
        return server;
    }

    public synchronized void setObjectName(ObjectName name){
        objectName = name;
    }

    public synchronized ObjectName getObjectName() {
        return objectName;
    }

    public synchronized void start(){
        if(started) return;
        ToolBox.registerMBean(getMBeanServer(), getObjectName(), logMBean);
        logMBean.start();
        started = true;
    }

    public synchronized void stop(){
        if(!started) return;
        ToolBox.unregisterMBean(getMBeanServer(), getObjectName());
        logMBean.stop();
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
}
