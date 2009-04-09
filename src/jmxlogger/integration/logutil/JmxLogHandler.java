package jmxlogger.integration.logutil;

import java.lang.management.ManagementFactory;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jmxlogger.tools.JmxEventLogger;
import jmxlogger.tools.LogEvent;
import jmxlogger.tools.ToolBox;

/**
 *
 * @author VVivien
 */
public class JmxLogHandler extends Handler{
    LogManager manager = LogManager.getLogManager();
    private JmxEventLogger logger;

    private final static String KEY_LEVEL = "jmxlogger.Handler.level";
    private final static String KEY_FILTER = "jmxlogger.Handler.filter";
    private final static String KEY_LOGPATTERN = "jmxlogger.Handler.logPattern";
    private final static String KEY_FORMATTER = "jmxlogger.Handler.formatter";
    private final static String KEY_OBJNAME = "jmxlogger.Handler.objectName";
    private final static String KEY_SERVER = "jmxlogger.Handler.serverSelection";

    public JmxLogHandler(){
        initializeLogger();
        configure();
    }

    public JmxLogHandler(ObjectName objectName){
        initializeLogger();
        configure();
        setObjectName(objectName);
    }

    public JmxLogHandler(MBeanServer server){
        initializeLogger();
        configure();
        setMBeanServer(server);
    }

    public JmxLogHandler(MBeanServer server, ObjectName objectName){
        initializeLogger();
        configure();
        setMBeanServer(server);
        setObjectName(objectName);
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

    public void start() {
        if(logger != null && !logger.isStarted()){
            logger.start();
            if(!logger.isStarted()){
                reportError("Unable to start JMX Log Handler, you will not get logg messages.", null, ErrorManager.OPEN_FAILURE);
            }
        }
    }

    public void stop() {
        if(logger != null && logger.isStarted()){
            logger.stop();
        }
    }

    @Override
    public void publish(LogRecord record) {
        if(!logger.isStarted()){
            start();
        }
        if (!isLoggable(record)) {
            reportError("Unable to log message, check configuration" ,
                    null, ErrorManager.CLOSE_FAILURE);
            return;
        }

        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception ex) {
            reportError("Unable to format message properly.  " +
                    "Ensure that a formatter is specified.",
                    ex, ErrorManager.FORMAT_FAILURE);
            return;
        }
        try{
            LogEvent event = prepareLogEvent(msg,record);
            logger.log(event);
        }catch(Exception ex){
            reportError("Unable to send log message to JMX event bus.",
                    ex, ErrorManager.GENERIC_FAILURE);
        }
    }

    @Override
    public void flush() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws SecurityException {
        stop();
    }

    @Override
    public boolean isLoggable(LogRecord record){
        return (logger != null &&
                logger.isStarted() &&
                logger.getMBeanServer() != null &&
                logger.getObjectName() != null &&
                super.isLoggable(record)
                );
    }

    private void configure() {
        // configure level (default INFO)
        String value;
        value = manager.getProperty(KEY_LEVEL);
        super.setLevel(value != null ? Level.parse(value) : Level.INFO);

        // configure filter (default none)
        value = manager.getProperty(KEY_FILTER);
        if (value != null && !value.isEmpty()) {
            // assume it's a class and load it.
            try {
                Class cls = ClassLoader.getSystemClassLoader().loadClass(value);
                super.setFilter((Filter) cls.newInstance());
            } catch (Exception ex) {
                reportError("Unable to load filter class " + value + ". Filter will be set to null" ,
                    ex, ErrorManager.CLOSE_FAILURE);
                // ignore it and load SimpleFormatter.
                super.setFilter(null);
            }
        } else {
            super.setFilter(null);
        }

        value = manager.getProperty(KEY_LOGPATTERN);
        if(value != null){
            // logger.setLogPattern(value);
        }

        // configure formatter (default SimpleFormatter)
        value = manager.getProperty(KEY_FORMATTER);
        if (value != null && !value.isEmpty()) {
            // assume it's a class and load it.
            try {
                Class cls = ClassLoader.getSystemClassLoader().loadClass(value);
                super.setFormatter((Formatter) cls.newInstance());
            } catch (Exception ex) {
                reportError("Unable to load formatter class " + value + ". Will default to SimpleFormatter" ,
                    ex, ErrorManager.CLOSE_FAILURE);
                // ignore it and load SimpleFormatter.
                super.setFormatter(new SimpleFormatter());
            }

        } else {
            super.setFormatter(new SimpleFormatter());
        }

        // configure internal Jmx ObjectName (default provided by JmxEventLogger)

        value = manager.getProperty(KEY_OBJNAME);
        if(value != null && !value.isEmpty()){
            logger.setObjectName(ToolBox.buildObjectName(value));
        }else{
            logger.setObjectName(ToolBox.buildDefaultObjectName(Integer.toString(this.hashCode())));
        }

        // configure server used
        value = manager.getProperty(KEY_SERVER);
        if(value != null){
            if(value.equalsIgnoreCase("platform")) {
                // use existing platform server
                logger.setMBeanServer(ManagementFactory.getPlatformMBeanServer());
            }else{
                setMBeanServer(ToolBox.findMBeanServer(value));
            }
        }else{
            setMBeanServer(ManagementFactory.getPlatformMBeanServer());
        }
    }

    private void initializeLogger() {
        logger = (logger == null) ? JmxEventLogger.createInstance() : logger;
    }


    private LogEvent prepareLogEvent(String fmtMsg, LogRecord record){
        LogEvent<LogRecord> event = new LogEvent<LogRecord>();
        event.setLogRecord(record);
        event.setSource(this);
        event.setLevelName(record.getLevel().getName());
        event.setLoggerName(record.getLoggerName());
        event.setMessage(fmtMsg);
        event.setSequenceNumber(record.getSequenceNumber());
        event.setSourceClassName(record.getSourceClassName());
        event.setSourceMethodName(record.getSourceMethodName());
        event.setSourceThreadId(Integer.toString(record.getThreadID()));
        event.setSourceThrowable(record.getThrown());
        event.setTimeStamp(record.getMillis());

        return event;
    }
}
