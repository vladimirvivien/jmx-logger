/**
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jmxlogger.integration.logutil;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
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
import jmxlogger.JmxLogger;
import jmxlogger.tools.JmxLogService;
import jmxlogger.tools.ToolBox;

/**
 * This class implements the Java Logging Handler for JmxLogger.  It can be used to broadcast
 * logging events as JMX events.  When this class is initialized by the logging
 * framework, it creates a JMX MBean that emitts log event.
 *
 * @author vladimir.vivien
 */
public class JmxLogHandler extends Handler implements JmxLogger{
    LogManager manager = LogManager.getLogManager();
    private JmxLogService logService;

    private final static String KEY_LEVEL = "jmxlogger.Handler.level";
    private final static String KEY_FILTER = "jmxlogger.Handler.filter";
    private final static String KEY_LOGPATTERN = "jmxlogger.Handler.logPattern";
    private final static String KEY_FORMATTER = "jmxlogger.Handler.formatter";
    private final static String KEY_OBJNAME = "jmxlogger.Handler.objectName";
    private final static String KEY_SERVER = "jmxlogger.Handler.serverSelection";

    /**
     * Default constructor.  Initializes a default MBeanServer (platform) and
     * a default emitter MBean object.
     */
    public JmxLogHandler(){
        initializeLogger();
        configure();
        start();
    }

    /**
     * Constructor with a default Object name for emitter MBean.
     * @param objectName
     */
    public JmxLogHandler(ObjectName objectName){
        initializeLogger();
        configure();
        setObjectName(objectName);
        start();
    }

    /**
     * Constructor with a default MBeanServer used to register emitter MBean in.
     * @param server
     */
    public JmxLogHandler(MBeanServer server){
        initializeLogger();
        configure();
        setMBeanServer(server);
        start();
    }

    /**
     * Constructor with MBeanServer and ObjectName for emitter MBean specified.
     * @param server
     * @param objectName
     */
    public JmxLogHandler(MBeanServer server, ObjectName objectName){
        initializeLogger();
        configure();
        setMBeanServer(server);
        setObjectName(objectName);
        start();
    }

    /**
     * Setter for emitter MBean ObjectName.
     * @param objName
     */
    public void setObjectName(ObjectName objName){
        logService.setObjectName(objName);
    }

    /**
     * Getter of ObjectName for emitter MBean.
     * @return ObjectName instance
     */
    public ObjectName getObjectName() {
        return (logService.getObjectName() != null) ? logService.getObjectName() : null;
    }

    /**
     * Setter for MBeanServer used to register emitter MBean.
     * @param server
     */
    public void setMBeanServer(MBeanServer server){
        logService.setMBeanServer(server);
    }

    /**
     * Getter of MBeanServer.
     * @return MBeanServer
     */
    public MBeanServer getMBeanServer() {
        return logService.getMBeanServer();
    }

    /**
     * Life cycle method.  Call this after all values are set.  This is necessary
     * because the util logging does not provide a built life cyle method.
     */
    public void start() {
        if(logService != null && !logService.isStarted()){
            logService.start();
            if(!logService.isStarted()){
                reportError("Unable to start JMX Log Handler, you will not get logg messages.", null, ErrorManager.OPEN_FAILURE);
            }
        }
    }

    /**
     * Life cycle method to call to stop logService.
     */
    public void stop() {
        if(logService != null && logService.isStarted()){
            logService.stop();
        }
    }

    /**
     * Java Logging framework method called when a logService logs a message.
     * @param LogRecord record
     */
    @Override
    public void publish(LogRecord record) {
        // validate configuration values
        if (!isConfiguredOk()) {
            reportError("Unable to log message, check your log configuration." ,
                    null, ErrorManager.CLOSE_FAILURE);
            return;
        }

        // start handler
        if(!logService.isStarted()){
            start();
        }
        if (!isLoggable(record)) {
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
            Map<String,Object> event = prepareLogEvent(msg,record);
            logService.log(event);
        }catch(Exception ex){
            reportError("Unable to send log message to JMX event bus.",
                    ex, ErrorManager.GENERIC_FAILURE);
        }
    }

    /**
     * Life cycle message called by the Java Logging framework.
     */
    @Override
    public void flush() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Life cycle method called by the Java Logging framework.
     * @throws java.lang.SecurityException
     */
    @Override
    public void close() throws SecurityException {
        stop();
    }

    /**
     * Determines if an event can be logged based on several criteria.
     * @param record
     * @return boolean
     */
    @Override
    public boolean isLoggable(LogRecord record){
        return (super.isLoggable(record));
    }

    private boolean isConfiguredOk() {
        return (logService != null &&
                logService.isStarted() &&
                logService.getMBeanServer() != null &&
                logService.getObjectName() != null &&
                getFormatter() != null &&
                this.getLevel() != null);
    }

    /**
     * Method that configure the Java Logging Handler.
     */
    private void configure() {
        // configure level (default INFO)
        String value;
        value = manager.getProperty(KEY_LEVEL);
        super.setLevel(value != null ? Level.parse(value) : Level.FINE);

        // configure filter (default none)
        value = manager.getProperty(KEY_FILTER);
        if (value != null && value.length() != 0) {
            // assume it's a class name and load it.
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
            // logService.setLogPattern(value);
        }

        // configure formatter (default SimpleFormatter)
        value = manager.getProperty(KEY_FORMATTER);
        if (value != null && value.length() != 0) {
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

        // configure internal Jmx ObjectName (default provided by JmxLogService)

        value = manager.getProperty(KEY_OBJNAME);
        if(value != null && value.length() != 0){
            logService.setObjectName(ToolBox.buildObjectName(value));
        }else{
            logService.setObjectName(ToolBox.buildDefaultObjectName(Integer.toString(this.hashCode())));
        }

        // configure server used
        value = manager.getProperty(KEY_SERVER);
        if(value != null && value.length() != 0){
            if(value.equalsIgnoreCase("platform")) {
                // use existing platform server
                logService.setMBeanServer(ManagementFactory.getPlatformMBeanServer());
            }else{
                setMBeanServer(ToolBox.findMBeanServer(value));
            }
        }else{
            setMBeanServer(ManagementFactory.getPlatformMBeanServer());
        }
    }

    /**
     * Initializes the MBean logService object.
     */
    private void initializeLogger() {
        logService = (logService == null) ? JmxLogService.createInstance() : logService;
    }

    /**
     * Transfers Java Logging LogRecord data to a map to be passed to JMX event bus.
     * @param fmtMsg
     * @param record
     * @return Map containing the event to be logged.
     */
    private Map<String,Object> prepareLogEvent(String fmtMsg, LogRecord record){
        Map<String,Object> event = new HashMap<String,Object>();
        event.put(ToolBox.KEY_EVENT_SOURCE,this.getClass().getName());
        event.put(ToolBox.KEY_EVENT_LEVEL,record.getLevel().getName());
        event.put(ToolBox.KEY_EVENT_LOGGER,record.getLoggerName());
        event.put(ToolBox.KEY_EVENT_MESSAGE,fmtMsg);
        event.put(ToolBox.KEY_EVENT_SEQ_NUM, new Long(record.getSequenceNumber()));
        event.put(ToolBox.KEY_EVENT_SOURCE_CLASS,record.getSourceClassName());
        event.put(ToolBox.KEY_EVENT_SOURCE_METHOD,record.getSourceMethodName());
        event.put(ToolBox.KEY_EVENT_THREAD,Integer.toString(record.getThreadID()));
        event.put(ToolBox.KEY_EVENT_THROWABLE,record.getThrown());
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(record.getMillis()));

        return event;
    }

    /**
     * This method abstracts out the log level.
     * @param level
     */
    public void setLogLevel(String level) {
        this.setLevel(Level.parse(level));
    }

    /**
     * This method abstracts out the log level.
     * @return
     */
    public String getLogLevel() {
        return this.getLevel().getName();
    }
    
}
