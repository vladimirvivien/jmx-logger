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

package jmxlogger.integration.log4j;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jmxlogger.JmxLogger;
import jmxlogger.tools.JmxLogFilter;
import jmxlogger.tools.JmxLogConfig;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import jmxlogger.tools.JmxLogService;
import jmxlogger.tools.ToolBox;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.Filter;

/**
 * This class implements the Log4J appender for JmxLogger.  It can be used to broadcast
 * Log4J log events as JMX events.  When this class is initialized by the logging
 * framework, it creates a JMX MBean that emitts log event.
 *
 * @author vladimir.vivien
 */
public class JmxLogAppender extends AppenderSkeleton implements JmxLogger{
    private JmxLogService jmxLogService;
    private String msgPattern;
    private String serverSelection="platform";
    private Layout logLayout = new PatternLayout("%-4r [%t] %-5p %c %x - %m%n");
    private JmxLogFilter logFilter;
    /**
     * Default constructor.  Creates a new JMX MBean emitter and registers that
     * emitter on the underlying Platform MBeanServer.
     */
    public JmxLogAppender() {
        initializeLogger();
        configure();
    }

    /**
     * Constructor which takes a JMX ObjectName instance used to create the JMX
     * event emitter.
     * @param name - ObjectName instance used to register MBean emitter.
     */
    public JmxLogAppender(ObjectName name){
        initializeLogger();
        jmxLogService.setObjectName(name);
        configure();
    }

    /**
     * Constructor which takes an MBeanServer where the MBean will be created.
     * @param server
     */
    public JmxLogAppender(MBeanServer server){
        initializeLogger();
        jmxLogService.setMBeanServer(server);
        configure();
        
    }

    /**
     * Constructor which specifies a default MBeanServer and ObjectName for the J
     * JMX MBean event emitter.
     * @param server - default server to use.
     * @param name - JMX ObectName to use for JMX MBean emitter.
     */
    public JmxLogAppender(MBeanServer server, ObjectName name){
        initializeLogger();
        jmxLogService.setMBeanServer(server);
        jmxLogService.setObjectName(name);
        configure();
    }

    /**
     * Setter for the ObjectName to use.
     * @param objName - instance of ObjectName to use for JMX emitter MBean.
     */
    public void setObjectName(String objName){
        jmxLogService.setObjectName(ToolBox.buildObjectName(objName));
    }

    /**
     * Getter for ObjectName used for JMX emitter.
     * @return
     */
    public String getObjectName() {
        return (jmxLogService.getObjectName() != null) ? jmxLogService.getObjectName().toString() : null;
    }

    /**
     * Setter for MBeanServer used to register emitter MBean.
     * @param server - MBeanServer
     */
    public void setMBeanServer(MBeanServer server){
        jmxLogService.setMBeanServer(server);
    }

    /**
     * Getter of MBeanServer used JMX emitter MBean.
     * @return MBeanServer
     */
    public MBeanServer getMBeanServer() {
        return jmxLogService.getMBeanServer();
    }

    /**
     * To be implemented later.
     * @param pattern
     */
    public synchronized void setLogPattern(String pattern){
        msgPattern = pattern;
    }
    /**
     * To be implemented later.
     * @return String
     */
    public synchronized String getLogPattern(){
        return msgPattern.toString();
    }

    /**
     * Setter for server selection.  Valid values are "platform" which causes the
     * platform MBenServer to be used. Or the domain name of an existing MBeanServer
     * can be used.
     * @param selection ["platform"|"server domain name"]
     */
    public synchronized void setServerSelection(String selection){
        serverSelection = selection;
    }

    /**
     * Getter for MBeanServer selection.
     * @return the selection.
     */
    public synchronized String getServerSelection(){
        return serverSelection;
    }

    /**
     * Log4J life cycle method, called once all gettters/setters are called.
     */
    @Override
    public void activateOptions() {
        configure();
        if(!jmxLogService.isStarted()){
            jmxLogService.start();
        }

    }
    

    /**
     * Log4J framework method, called when a jmxLogService logs an event.  Here, it
     * sends the log message to the JMX event bus.
     * @param log4jEvent
     */
    @Override
    protected void append(LoggingEvent log4jEvent) {
        // check configuration
        if (!isConfiguredOk()) {
            errorHandler.error("Unable to log message, check configuration.");
            return;
        }

        // determine if we can go forward with log
        if(!isLoggable(log4jEvent)){
            return;
        }

        // log message
        String msg;
        try {
            msg = layout.format(log4jEvent);
            Map<String,Object> event = prepareLogEvent(msg,log4jEvent);
            jmxLogService.log(event);
        }catch(Exception ex){
           errorHandler.error("Unable to send log to JMX.",
                   ex, ErrorCode.GENERIC_FAILURE);
        }
    }

    /**
     * Log4J life cycle method, stops the JMX MBean emitter.
     */
    public synchronized void close() {
        jmxLogService.stop();
        this.closed = true;
    }

    /**
     * Log4J convenience method to indicate the need for a Layout.
     * @return
     */
    public boolean requiresLayout() {
        return true;
    }

    /**
     * Determines whether the Appender's configuration is OK.
     * @return boolean
     */
    private boolean isConfiguredOk(){
        return jmxLogService != null &&
                jmxLogService.isStarted() &&
                jmxLogService.getMBeanServer() != null &&
                jmxLogService.getObjectName() != null &&
                layout != null &&
                this.getThreshold() != null;
    }

    /**
     * Figures out if appender can log event
     * @return
     */
    private boolean isLoggable(LoggingEvent event) {
        return (event.getLevel().isGreaterOrEqual(this.getThreshold()));
    }

    /**
     * Initialize the JMX Logger object.
     */
    private void initializeLogger() {
      jmxLogService = (jmxLogService == null) ? JmxLogService.createInstance() : jmxLogService;
      jmxLogService.setLogger(this);
    }

    /**
     * Configures the value objects for the appender.
     */
    private void configure() {
        // config layout
        if (super.getLayout() == null) {
            super.setLayout(logLayout);
        }
        // config level
        if(super.getThreshold() == null){
            super.setThreshold(Level.DEBUG);
        }

        // configure server
        if (jmxLogService.getMBeanServer() == null) {
            if (getServerSelection().equalsIgnoreCase("platform")) {
                jmxLogService.setMBeanServer(ManagementFactory.getPlatformMBeanServer());
            } else {
                jmxLogService.setMBeanServer(ToolBox.findMBeanServer(getServerSelection()));
            }
        }

        // configure internal object name
        if(jmxLogService.getObjectName() == null){
            jmxLogService.setObjectName(ToolBox.buildDefaultObjectName(Integer.toString(this.hashCode())));
        }

        // grab default log filter (if any)
        // note that filter is userd as a dto and its rule not calculated.
        // instead, filtering happens in the JmxLogFilter to take advantage of
        // async
        Filter filter = getFilter();
        while (filter != null){
            if(filter instanceof DefaultLog4jFilter){
                break;
            }
            filter = filter.getNext();
        }
        
        if(filter != null){
            JmxLogConfig cfg = ((DefaultLog4jFilter)filter).getLogFilterConfig();

            jmxLogService.setLogFilterConfig(cfg);
        }
    }

    /**
     * Transfers Log4J LoggingEvent data to a map to be passed to JMX event bus.
     * @param fmtMsg
     * @param record
     * @return Map containing the event to be logged.
     */
    private Map<String,Object> prepareLogEvent(String fmtMsg, LoggingEvent record){
        Map<String,Object> event = new HashMap<String,Object>();
        event.put(ToolBox.KEY_EVENT_SOURCE,this.getClass().getName());
        event.put(ToolBox.KEY_EVENT_LEVEL,record.getLevel().toString());
        event.put(ToolBox.KEY_EVENT_LOGGER,record.getLoggerName());
        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE,fmtMsg);
        event.put(ToolBox.KEY_EVENT_RAW_MESSAGE, record.getMessage());
        event.put(ToolBox.KEY_EVENT_SEQ_NUM, new Long(record.getTimeStamp()));
        event.put(ToolBox.KEY_EVENT_SOURCE_CLASS,
                (record.locationInformationExists())
                ? record.getLocationInformation().getClassName()
                : "Unavailable");
        event.put(ToolBox.KEY_EVENT_SOURCE_METHOD,
                (record.locationInformationExists())
                ? record.getLocationInformation().getMethodName()
                : "Unavailable" );
        event.put(ToolBox.KEY_EVENT_SOURCE_THREAD,
                record.getThreadName());
        event.put(ToolBox.KEY_EVENT_THROWABLE,
                (record.getThrowableInformation() != null)
                ? record.getThrowableInformation().getThrowable()
                : null);
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(record.getTimeStamp()));

        return event;
    }

    public void setLogLevel(String l) {
        Level level = Level.toLevel(l);
        setThreshold(level);
    }

    public String getLogLevel() {
        return getThreshold().toString();
    }

}
