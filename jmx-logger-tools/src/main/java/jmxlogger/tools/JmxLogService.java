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

package jmxlogger.tools;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jmxlogger.tools.JmxConfigStore.ConfigEvent;

/**
 * This service level class manages the creation of JMX emitter MBean facilitates
 * logging of message by higher level frameworks.  In this case, this service
 * class serves as the interface between logging service framework and the JMX
 * management framework.  It's the glue between the two and normalizes the
 * abstraction level.
 * @author vladimir.vivien
 */
public class JmxLogService {
    private JmxLogEmitterMBean logMBean;
    private JmxScriptedLogFilter logFilter;
    private JmxConfigStore configStore;
    private AtomicLong totalLogCounter = new AtomicLong(0);
    private AtomicLong attemptedLogCounter = new AtomicLong(0);
    private ConcurrentHashMap<String,Long> logStatistics;

    private final PriorityBlockingQueue<JmxEventWrapper> queue =
            new PriorityBlockingQueue<JmxEventWrapper>(100);

    private ExecutorService noteConsumer;
    private ExecutorService noteProducers;
    private int producerSize = 5;
    private Date startTime;

    /**
     * Private Constructor.
     */
    private JmxLogService() {
        configStore = new JmxConfigStore();
        initializeService();
     }

    private JmxLogService(JmxConfigStore store){
        configStore = store;
        initializeService();
    }

    /**
     * Factory method to create instance of this class.
     * @return JmxLogService
     */
    public static JmxLogService createInstance(){
        return new JmxLogService();
    }

    public static JmxLogService createInstance(JmxConfigStore store){
        return new JmxLogService(store);
    }
    private void initializeService() {
        logMBean = new JmxLogEmitter(configStore);
        logFilter = new JmxScriptedLogFilter();
        logStatistics = new ConcurrentHashMap<String, Long>();

        // add listener to reset filterExpression and filterFile
        configStore.addListener(new JmxConfigStore.ConfigEventListener() {
            public void onValueChanged(ConfigEvent event) {
                if(!event.getSource().equals(JmxLogService.this) && event.getKey().equals(ToolBox.KEY_CONFIG_FILTER_EXP)){
                    logFilter.setFilterExpression((String)event.getValue());
                }
                if(!event.getSource().equals(JmxLogService.this) && event.getKey().equals(ToolBox.KEY_CONFIG_FILTER_SCRIPT)){
                    logFilter.setScriptFile((File)event.getValue());
                }
            }
        });
    }

    public JmxConfigStore getDefaultConfigurationStore() {
        return configStore;
    }


    /**
     * Life cycle method that starts the logger.  It registers the emitter MBean
     * with the ObjectName to the specified MBeanServer.
     */
    public void start(){
        this.setupNoteProducers();
        this.setupNoteConsumerTask();
        MBeanServer svr = (MBeanServer) configStore.getValue(ToolBox.KEY_CONFIG_JMX_SERVER);
        ObjectName objName = (ObjectName)configStore.getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME);
        if(svr == null || objName == null){
            throw new IllegalArgumentException("Unable to start log service - " +
                    "instance of MBeanServer and ObjectName must be " +
                    "provided.");
        }
        // setup
        ToolBox.registerMBean(svr, objName, logMBean);
        logMBean.start();
        startTime = new Date();
        logStatistics.put(ToolBox.KEY_EVENT_START_TIME, new Long(startTime.getTime()));
    }

    /**
     * Life cycle method that stops the JMX emitter and undergisters from the MBean.
     */
    public void stop(){
        noteConsumer.shutdownNow();
        noteProducers.shutdownNow();
        MBeanServer svr = (MBeanServer) configStore.getValue(ToolBox.KEY_CONFIG_JMX_SERVER);
        ObjectName objName = (ObjectName)configStore.getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME);
        ToolBox.unregisterMBean(svr, objName);
        logMBean.stop();
    }

    /**
     * Status reporter method.
     * @return booelean
     */
    public boolean isStarted(){
        return logMBean.isStarted();
    }

    /**
     * Loges a message by sending it to the emitter to place ont he JMX event bus.
     * @param event
     */
    public void log(final Map<String,Object> event){
        if(!logMBean.isStarted()){
            throw new IllegalStateException("JmxEventLogger has not been started." +
                    "Call JmxEventLogger.start() before you log messages.");
        }

        // add total log counted
        logStatistics.put(ToolBox.KEY_EVENT_LOG_COUNT_ATTEMPTED, new Long(attemptedLogCounter.incrementAndGet()));
        event.put(ToolBox.KEY_EVENT_LOG_STAT, Collections.unmodifiableMap(logStatistics));
        event.put(ToolBox.KEY_EVENT_SYS_STAT, Collections.unmodifiableMap(ToolBox.getSystemInfo()));

        noteProducers.execute(new Runnable(){
            public void run() {
                // apply filter configuration then put event not on queue
                if(logFilter.isLogAllowed(Collections.unmodifiableMap(event))){
                    // update LEVEL statistics
                    String level = (String) event.get(ToolBox.KEY_EVENT_LEVEL);
                    if(level != null){
                        Long levelCount = logStatistics.get(level);
                        if (levelCount == null) {
                            logStatistics.put(level, new Long(1));
                        } else {
                            long updatedVal = levelCount.longValue() + 1;
                            logStatistics.put(level, new Long(updatedVal));
                        }
                    }

                    // update logger value
                    String logger = (String)event.get(ToolBox.KEY_EVENT_LOGGER);
                    if(logger != null){
                    Long loggerCount = logStatistics.get(logger);
                        if (loggerCount == null) {
                            logStatistics.put(logger, new Long(1));
                        } else {
                            long updatedVal = loggerCount.longValue() + 1;
                            logStatistics.put(logger, new Long(updatedVal));
                        }
                    }

                    // update filtered Counter
                    logStatistics.put(ToolBox.KEY_EVENT_LOG_COUNTED, new Long(totalLogCounter.incrementAndGet()));                 
                    event.put(ToolBox.KEY_EVENT_LOG_STAT, Collections.unmodifiableMap(logStatistics));

                    // add system statiscs
                    HashMap sysStats = new HashMap<String,Object>();

                    // put in queue to be sent.
                    queue.put(new JmxEventWrapper(Collections.unmodifiableMap(event)));
                }
            }
        });
    }

    private void setupNoteProducers() {
        noteProducers = Executors.newFixedThreadPool(producerSize);
    }

    private void setupNoteConsumerTask() {
        noteConsumer = Executors.newSingleThreadExecutor();
        noteConsumer.execute(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        JmxEventWrapper eventWrapper = queue.take();
                        ((JmxLogEmitter)logMBean).sendLog(eventWrapper.unwrap());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
