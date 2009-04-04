package jmxlogger.integration.log4j;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import jmxlogger.tools.JmxEventLogger;

/**
 * @author vladimir.vivien
 */
public class JmxLogAppender extends AppenderSkeleton{
    private JmxEventLogger logger;

    public JmxLogAppender() {
        initializeLogger();
    }

    public JmxLogAppender(ObjectName name){
        initializeLogger();
        logger.setObjectName(name);
    }

    public JmxLogAppender(MBeanServer server){
        initializeLogger();
        logger.setMBeanServer(server);
    }

    public JmxLogAppender(MBeanServer server, ObjectName name){
        initializeLogger();
        logger.setMBeanServer(server);
        logger.setObjectName(name);
    }

    public void setObjectName(ObjectName objName){
        logger.setObjectName(objName);
    }

    public void setObjectName(String objName){
        logger.setObjectName(buildObjectName(objName));
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


    @Override
    public void activateOptions() {
        
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

    private ObjectName buildObjectName(String name) {
        ObjectName objName = null;
        try {
            objName = new ObjectName(name);
        } catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex);
        } catch (NullPointerException ex) {
            throw new RuntimeException(ex);
        }
        return objName;
    }

}
