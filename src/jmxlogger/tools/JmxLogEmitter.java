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

import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import jmxlogger.tools.JmxConfigStore.ConfigEvent;

/**
 * This is the emitter MBean class.  It is actually registered as the management
 * MBean that provide the log emitter service on the MBean event bus.
 * @author vladimir.vivien
 */
public class JmxLogEmitter extends NotificationBroadcasterSupport implements JmxLogEmitterMBean{
    private volatile boolean started = false;
    private AtomicLong count = new AtomicLong(0);
    private Date startDate;
    private JmxConfigStore configStore;
    private String logLevel;
    private String filterExp;
    private Map <String,Long> statistics;
    

    public JmxLogEmitter(JmxConfigStore store) {
        configStore = store;
        initializeBean();
    }

    private void initializeBean() {
        // add listener to reset filterExpression and filterFile
        configStore.addListener(new JmxConfigStore.ConfigEventListener() {
            public void onValueChanged(ConfigEvent event) {
                if(!event.getSource().equals(JmxLogEmitter.this) && event.getKey().equals(ToolBox.KEY_CONFIG_LOG_LEVEL)){
                    logLevel = (String) event.getValue();
                }
                if(!event.getSource().equals(JmxLogEmitter.this) && event.getKey().equals(ToolBox.KEY_CONFIG_FILTER_EXP)){
                    filterExp = (String) event.getValue();
                }
            }
        });
    }
    /**
     * Life cycle method to start the MBean.
     */
    public synchronized void start() {
        if(started) return;
        started = true;
        startDate = new Date();
    }

    /**
     * Life cycle method to stop the MBean from sending log event.
     */
    public synchronized void stop() {
        if(!started) return;
        started = false;
    }

    /**
     * Life cycle reporter method.
     * @return boolean
     */
    public synchronized boolean isStarted() {
        return started;
    }

    /**
     * Returns the date when the MBean was last started.
     * @return String
     */
    public String getStartDate() {
        return new SimpleDateFormat().format(startDate);
    }

    /**
     * Returns the number events that have been emitted by the MBean.
     * @return
     */
    public long getLogCount() {
        return count.longValue();
    }

    public long getStats(String key){
        return statistics.get(key);
    }

    /**
     * Calls the sendNotification() method to send the log information to the
     * MBeanServer's event bus.  The log event is queued on internaal priority
     * by executor threads.  They are picked up by a consumer thread
     * as they become available.
     * @param event
     */
    public void sendLog(final Map<String,Object> event){
        if(!started) {
            throw new IllegalStateException("JmxLogEmitter must be started before" +
                    " you can invoke sendLog().");
        }
        sendNotification(buildNotification(event));
        count.incrementAndGet();
    }


    /**
     * Prepares event information as Notification object.
     * @param event
     * @return Notification
     */
    private Notification buildNotification(Map<String,Object> event){
        long seqnum = (event.get(ToolBox.KEY_EVENT_SEQ_NUM) != null) ? (Long)event.get(ToolBox.KEY_EVENT_SEQ_NUM) : 0L;
        long timestamp  = (event.get(ToolBox.KEY_EVENT_TIME_STAMP) != null) ? (Long)event.get(ToolBox.KEY_EVENT_TIME_STAMP) : 0L;
        event.put(ToolBox.KEY_EVENT_LOG_COUNTED, new Long(count.get()));
        event.put(ToolBox.KEY_EVENT_START_TIME, new Long(startDate.getTime()));

        // keep a copy of the stats
        statistics = (Map<String, Long>) event.get(ToolBox.KEY_EVENT_LOG_STAT);

        Notification note = new Notification(
                ToolBox.getDefaultEventType(),
                (String)event.get(ToolBox.KEY_EVENT_SOURCE),
                seqnum,
                timestamp,
                (String)event.get(ToolBox.KEY_EVENT_FORMATTED_MESSAGE));
        note.setUserData(event);
        return note;
    }

    public void setLevel(String level) {
        logLevel = level;
        configStore.putValue(ToolBox.KEY_CONFIG_LOG_LEVEL, level);
        configStore.postEvent(new ConfigEvent(this, ToolBox.KEY_CONFIG_LOG_LEVEL, level));
    }

    public String getLevel() {
        return logLevel;
    }

    public String getFilterExpression() {
        return filterExp;
    }

    public void setFilterExpression(String exp) {
        filterExp = exp;
        configStore.putValue(ToolBox.KEY_CONFIG_FILTER_EXP, filterExp);
        configStore.postEvent(new ConfigEvent(this, ToolBox.KEY_CONFIG_FILTER_EXP, filterExp));
    }

    public String getFilterScriptFile() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFilterScriptFile(String file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}