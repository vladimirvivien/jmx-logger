package jmxlogger.integration.log4j;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import jmxlogger.tools.JmxEventLogger;
import jmxlogger.tools.LogEvent;
import jmxlogger.tools.ToolBox;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.ErrorCode;

/**
 * @author vladimir.vivien
 */
public class JmxLogAppender extends AppenderSkeleton{
    private JmxEventLogger logger;
    private String logPattern;
    private String serverSelection="platform";
    private Layout logLayout = new PatternLayout("%-4r [%t] %-5p %c %x - %m%n");

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

    public void setObjectName(String objName){
        logger.setObjectName(ToolBox.buildObjectName(objName));
    }

    public String getObjectName() {
        return (logger.getObjectName() != null) ? logger.getObjectName().toString() : null;
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
    protected void append(LoggingEvent log4jEvent) {
        if (!isLoggable()) {
            errorHandler.error("Unable to log message, check configuration",
                     null, ErrorCode.GENERIC_FAILURE);
            return;
        }

        if(layout == null){
             errorHandler.error("No layout found for JmxLoggerAppender",
                     null, ErrorCode.MISSING_LAYOUT);
             return;
        }

        String msg;
        try {
            msg = layout.format(log4jEvent);
            LogEvent event = prepareLogEvent(msg,log4jEvent);
            logger.log(event);
        }catch(Exception ex){
           errorHandler.error("Unable to send log to JMX.", ex, ErrorCode.GENERIC_FAILURE);
        }
    }

    public void close() {
        logger.stop();
    }

    public boolean requiresLayout() {
        return true;
    }

    private boolean isLoggable(){
        return logger != null &&
                logger.isStarted() &&
                logger.getMBeanServer() != null &&
                logger.getObjectName() != null;
    }

    private void initializeLogger() {
      logger = (logger == null) ? JmxEventLogger.createInstance() : logger;
    }

    private void configure() {
        if (super.getLayout() == null) {
            super.setLayout(logLayout);
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

    private LogEvent prepareLogEvent(String fmtMsg, LoggingEvent record){
        LogEvent<LoggingEvent> event = new LogEvent<LoggingEvent>();
        event.setLogRecord(record);
        event.setSource(this);
        event.setLevelName(record.getLevel().toString());
        event.setLoggerName(record.getLoggerName());
        event.setMessage(fmtMsg);
        event.setSequenceNumber(record.getTimeStamp());
        event.setSourceClassName((record.locationInformationExists())
                ? record.getLocationInformation().getClassName()
                : "Unavailable");
        event.setSourceMethodName((record.locationInformationExists())
                ? record.getLocationInformation().getMethodName()
                : "Unavailable" );
        event.setSourceThreadId(record.getThreadName());
        event.setSourceThrowable((record.getThrowableInformation() != null) 
                ? record.getThrowableInformation().getThrowable()
                : null);
        event.setTimeStamp(record.getTimeStamp());

        return event;
    }

}
