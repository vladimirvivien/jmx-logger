package jmxlogger.integration.log4j;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import jmxlogger.tools.JmxEventLogger;
import jmxlogger.tools.ToolBox;
import org.apache.log4j.PatternLayout;

/**
 * @author vladimir.vivien
 */
public class JmxLogAppender extends AppenderSkeleton{
    private JmxEventLogger logger;
    private String logPattern;
    private String serverSelection="platform";

    public JmxLogAppender() {
        initializeLogger();
        configure();
    }

    public JmxLogAppender(ObjectName name){
        initializeLogger();
        logger.setObjectName(name);
        configure();
    }

    public JmxLogAppender(MBeanServer server){
        initializeLogger();
        logger.setMBeanServer(server);
        configure();
    }

    public JmxLogAppender(MBeanServer server, ObjectName name){
        initializeLogger();
        logger.setMBeanServer(server);
        logger.setObjectName(name);
        configure();
    }

    public void setObjectName(ObjectName objName){
        logger.setObjectName(objName);
    }

    public void setObjectName(String objName){
        logger.setObjectName(ToolBox.buildObjectName(objName));
    }

    public ObjectName getObjectName() {
        return (logger.getObjectName() != null) ? logger.getObjectName() : null;
    }

    public void setMBeanServer(MBeanServer server){
        logger.setMBeanServer(server);
    }
    public MBeanServer getMBeanServer() {
        return logger.getMBeanServer();
    }

    public synchronized void setLogPattern(String pattern){
        logPattern = pattern;
    }
    public synchronized String getLogPattern(){
        return logPattern;
    }

    public synchronized void setServerSelection(String selection){
        serverSelection = selection;
    }

    public synchronized String getServerSelection(){
        return serverSelection;
    }


    @Override
    public void activateOptions() {
        configure();
        if(!logger.isStarted()){
            logger.start();
        }
    }
    

    @Override
    protected void append(LoggingEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean requiresLayout() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initializeLogger() {
      logger = (logger == null) ? JmxEventLogger.createInstance() : logger;
    }

    private void configure() {
        if (super.getLayout() == null) {
            super.setLayout(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n"));
        }

        if (logger.getMBeanServer() == null) {
            if (getServerSelection().equalsIgnoreCase("platform")) {
                logger.setMBeanServer(ManagementFactory.getPlatformMBeanServer());
            } else {
                logger.setMBeanServer(ToolBox.findMBeanServer(getServerSelection()));
            }
        }
        if(logger.getObjectName() == null){
            logger.setObjectName(ToolBox.buildDefaultObjectName(Integer.toString(this.hashCode())));
        }
    }
}
