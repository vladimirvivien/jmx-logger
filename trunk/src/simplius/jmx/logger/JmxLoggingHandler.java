package simplius.jmx.logger;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import simplius.jmx.logger.service.JmxEventLogger;
import simplius.jmx.logger.service.LogEvent;

/**
 *
 * @author VVivien
 */
public class JmxLoggingHandler extends Handler{
    LogManager manager = LogManager.getLogManager();
    private JmxEventLogger logger;

    private final static String KEY_LEVEL = "jmx.logger.Handler.level";
    private final static String KEY_FILTER = "jmx.logger.Handler.filter";
    private final static String KEY_FORMATTER = "jmx.logger.Handler.formatter";
    private final static String KEY_OBJNAME = "jmx.logger.Handler.objectName";
    private final static String KEY_SERVER = "jmx.logger.Handler.level";

    public JmxLoggingHandler(){
        initializeLogger();
        configure();
    }

    public JmxLoggingHandler(ObjectName objectName){
        initializeLogger();
        configure();
        setObjectName(objectName);
    }

    public JmxLoggingHandler(MBeanServer server){
        initializeLogger();
        configure();
        setMBeanServer(server);
    }

    public JmxLoggingHandler(MBeanServer server, ObjectName objectName){
        initializeLogger();
        configure();
        setMBeanServer(server);
        setObjectName(objectName);
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

    public void start() {
        if(logger != null && !logger.isStarted()){
            logger.start();
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
            return;
        }
        String msg;
        try {
            msg = getFormatter().format(record);
            LogEvent event = prepareLogEvent(msg,record);
            logger.log(event);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
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

    private ObjectName buildObjectName(String name){
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

    private void configure() {
        // configure level (default INFO)
        String value;
        value = manager.getProperty(KEY_LEVEL);
        super.setLevel(value != null ? Level.parse(value) : Level.INFO);

        // configure filter (default none)
        value = manager.getProperty(KEY_FILTER);
        if (value != null) {
            if (value.startsWith("/") && value.endsWith("/")) {
                // use regex filter
                } else {
                // assume it's a class and load it.
                try {
                    Class cls = ClassLoader.getSystemClassLoader().loadClass(value);
                    super.setFilter((Filter) cls.newInstance());
                } catch (Exception ex) {
                    // ignore it and load SimpleFormatter.
                    super.setFilter(null);
                }
            }
        } else {
            super.setFilter(null);
        }

        // configure formatter (default SimpleFormatter)
        value = manager.getProperty(KEY_FORMATTER);
        if (value != null) {
            // assume it's a class and load it.
            try {
                Class cls = ClassLoader.getSystemClassLoader().loadClass(value);
                super.setFormatter((Formatter) cls.newInstance());
            } catch (Exception ex) {
                // ignore it and load SimpleFormatter.
                super.setFormatter(new SimpleFormatter());
            }

        } else {
            super.setFormatter(new SimpleFormatter());
        }

        // configure internal Jmx ObjectName (default provided by JmxEventLogger)

        value = manager.getProperty(KEY_OBJNAME);
        if(value != null){
            logger.setObjectName(buildObjectName(value));
        }

        // configure server used
        value = manager.getProperty(KEY_SERVER);
        if(value != null){
            if(value.equalsIgnoreCase("platform")) {
                // use existing platform server
                logger.setMBeanServer(ManagementFactory.getPlatformMBeanServer());
            }else{
                // use server with given domain name
                ArrayList<MBeanServer> servers = javax.management.MBeanServerFactory.findMBeanServer(value);
                if (servers.size() > 0) {
                    logger.setMBeanServer(servers.get(0));
                } else {
                    setMBeanServer(ManagementFactory.getPlatformMBeanServer());
                }
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
        event.setSourceThreadId(record.getThreadID());
        event.setSourceThrowable(record.getThrown());
        event.setTimeStamp(record.getMillis());

        return event;
    }
}
