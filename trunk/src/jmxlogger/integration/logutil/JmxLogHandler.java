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
import jmxlogger.tools.JmxConfigStore;
import jmxlogger.tools.JmxConfigStore.ConfigEvent;
import jmxlogger.tools.JmxLogService;
import jmxlogger.tools.ToolBox;

/**
 * This class implements the Java Logging Handler for JmxLogConfigurer.  It can be used to broadcast
 * logging events as JMX events.  When this class is initialized by the logging
 * framework, it creates a JMX MBean that emitts log event.
 *
 * @author vladimir.vivien
 */
public class JmxLogHandler extends Handler {
    LogManager manager = LogManager.getLogManager();
    private JmxLogService logService;
    private JmxConfigStore configStore;
    private volatile boolean configured = false;

    private final static String KEY_LEVEL = "jmxlogger.Handler.level";
    private final static String KEY_FORMATTER = "jmxlogger.Handler.formatter";
    private final static String KEY_OBJNAME = "jmxlogger.Handler.objectName";
    private final static String KEY_SERVER = "jmxlogger.Handler.mbeanServer";
    private final static String KEY_FILTER = "jmxlogger.Handler.filter";
    private final static String KEY_FILTER_EXP = "jmxlogger.Handler.filterExpression";
    private final static String KEY_FILTER_SCRIPT = "jmxlogger.Handler.filterScript";


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

    @Override
    public void setLevel(Level l) {
        super.setLevel(l);
        configStore.putValue(ToolBox.KEY_CONFIG_LOG_LEVEL, l.getName());
        configStore.postEvent(new ConfigEvent(this, ToolBox.KEY_CONFIG_LOG_LEVEL, l.getName()));
    }

    
    /**
     * Setter for emitter MBean ObjectName.
     * @param objName
     */
    public void setObjectName(ObjectName objName){
        configStore.putValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME, objName);
    }

    /**
     * Getter of ObjectName for emitter MBean.
     * @return ObjectName instance
     */
    public ObjectName getObjectName() {
        return (ObjectName)configStore.getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME);
    }

    /**
     * Setter for MBeanServer used to register emitter MBean.
     * @param server
     */
    public void setMBeanServer(MBeanServer server){
        configStore.putValue(ToolBox.KEY_CONFIG_JMX_SERVER, server);
    }

    /**
     * Getter of MBeanServer.
     * @return MBeanServer
     */
    public MBeanServer getMBeanServer() {
        return (MBeanServer)configStore.getValue(ToolBox.KEY_CONFIG_JMX_SERVER);
    }

    public void setFilterExpression(String exp){
        configStore.putValue(ToolBox.KEY_CONFIG_FILTER_EXP, exp);
    }

    public String getFilterExpression(){
        return (String)configStore.getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME);
    }

    public void setFilterScript(String fileName) {
        configStore.putValue(ToolBox.KEY_CONFIG_FILTER_SCRIPT, fileName);
    }

    public String getFilterScript(){
        return (String)configStore.getValue(ToolBox.KEY_CONFIG_FILTER_SCRIPT);
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
        configured = false;
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

    public boolean isConfigured() {
        return configured;
    }

    private boolean isConfiguredOk() {
        return (isConfigured() &&
                logService != null &&
                logService.isStarted() &&
                configStore.getValue(ToolBox.KEY_CONFIG_JMX_SERVER) != null &&
                configStore.getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME) != null &&
                getFormatter() != null &&
                this.getLevel() != null);
    }

    /**
     * Method that configure the Java Logging Handler.
     */
    private void configure() {
        // configure level (default INFO)
        String value = manager.getProperty(KEY_LEVEL);
        setLevel(createLevelInstance(value));

        // configure filter (default none)
        value = manager.getProperty(KEY_FILTER);
        if(value != null)
            setFilter(createFilterInstance(value));

        value = manager.getProperty(KEY_FILTER_EXP);
        if(value != null)
            setFilterExpression(value);

        value = manager.getProperty(KEY_FILTER_SCRIPT);
        if(value != null)
            setFilterScript(value);

        
        // configure formatter (default SimpleFormatter)
        setFormatter(createFormatterInstance(manager.getProperty(KEY_FORMATTER)));

        // configure internal Jmx ObjectName
        value = manager.getProperty(KEY_OBJNAME);
        if(value != null && value.length() != 0){
            setObjectName(ToolBox.buildObjectName(value));
        }else{
            setObjectName(ToolBox.buildDefaultObjectName(Integer.toString(this.hashCode())));
        }

        // configure server used
        value = manager.getProperty(KEY_SERVER);
        if(value != null && value.length() != 0){
            if(value.equalsIgnoreCase("platform")) {
                // use existing platform server
                setMBeanServer(ManagementFactory.getPlatformMBeanServer());
            }else{
                setMBeanServer(ToolBox.findMBeanServer(value));
            }
        }else{
            setMBeanServer(ManagementFactory.getPlatformMBeanServer());
        }

        configured = true;
    }

    /**
     * Initializes the MBean logService object.
     */
    private void initializeLogger() {
        logService = (logService == null) ? JmxLogService.createInstance() : logService;
        configStore = logService.getDefaultConfigurationStore();

        // what to do when a value is update
        configStore.addListener(new JmxConfigStore.ConfigEventListener() {
            public void onValueChanged(JmxConfigStore.ConfigEvent event) {
                 if (event.getKey().equals(ToolBox.KEY_CONFIG_LOG_LEVEL)){
                    updateInternalLevel((String)event.getValue());
                }
            }
        } );
    }

    private void updateInternalLevel(String l){
        super.setLevel(createLevelInstance(l));
    }

    private Level createLevelInstance(String level){
        return level != null ? Level.parse(level) : Level.FINE;
    }
    private Filter createFilterInstance(String className){
        Filter f = null;
        if (className != null && className.length() != 0) {
            // assume it's a valid class name on the classpath and load it.
            try {
                Class cls = ClassLoader.getSystemClassLoader().loadClass(className);
                f = (Filter) cls.newInstance();
            } catch (Exception ex) {
                reportError("Unable to load filter class [" + className + "]. Filter will be set to null" ,
                    ex, ErrorManager.CLOSE_FAILURE);
            }
        }
        return f;
    }

    private Formatter createFormatterInstance(String className) {
        Formatter f = new SimpleFormatter();
        if (className != null && className.length() != 0) {
            // assume it's a class and load it.
            try {
                Class cls = ClassLoader.getSystemClassLoader().loadClass(className);
                f = (Formatter) cls.newInstance();
            } catch (Exception ex) {
                reportError("Unable to load formatter class [" + className + "]. Will default to SimpleFormatter" ,
                    ex, ErrorManager.CLOSE_FAILURE);
            }

        }
        return f;
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
        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE,fmtMsg);
        event.put(ToolBox.KEY_EVENT_SEQ_NUM, new Long(record.getSequenceNumber()));
        event.put(ToolBox.KEY_EVENT_SOURCE_CLASS,record.getSourceClassName());
        event.put(ToolBox.KEY_EVENT_SOURCE_METHOD,record.getSourceMethodName());
        event.put(ToolBox.KEY_EVENT_SOURCE_THREAD,Integer.toString(record.getThreadID()));
        event.put(ToolBox.KEY_EVENT_THROWABLE,record.getThrown());
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(record.getMillis()));

        return event;
    }
}
