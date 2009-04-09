package jmxlogger.tools;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author vladimir.vivien
 */
public class JmxEventLogger {
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

    public void start(){
        ToolBox.registerMBean(getMBeanServer(), getObjectName(), logMBean);
        logMBean.start();
    }

    public void stop(){
        ToolBox.unregisterMBean(getMBeanServer(), getObjectName());
        logMBean.stop();
    }

    public boolean isStarted(){
        return logMBean.isStarted();
    }
    
    public synchronized void log(LogEvent event){
        if(!logMBean.isStarted()){
            throw new IllegalStateException("JmxEventLogger has not been started." +
                    "Call JmxEventLogger.start() before you log messages.");
        }
        logMBean.sendLog(event);
    }
}
