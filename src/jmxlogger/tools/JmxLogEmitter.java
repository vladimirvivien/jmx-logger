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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**
 * This is the emitter MBean class.  It is actually registered as the management
 * MBean that provide the log emitter service on the MBean event bus.
 * @author vladimir.vivien
 */
public class JmxLogEmitter extends NotificationBroadcasterSupport implements JmxLogEmitterMBean{
    private volatile boolean started = false;
    private AtomicLong count = new AtomicLong(0);
    private Date startDate;
    private JmxLogService logService;
    
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

        Notification note = new Notification(
                ToolBox.getDefaultEventType(),
                (String)event.get(ToolBox.KEY_EVENT_SOURCE),
                seqnum,
                timestamp,
                (String)event.get(ToolBox.KEY_EVENT_FORMATTED_MESSAGE));
        note.setUserData(event);
        return note;
    }

    public void setLogService(JmxLogService ls){
        this.logService = ls;
    }

    public void setLogLevel(String level) {
        logService.setLoggerLevel(level);
    }

    public String getLogLevel() {
        return logService.getLoggerLevel();
    }

    public void setFilteredPattern(String pattern) {
        logService.getLogFilterConfig().setLogPattern(Pattern.compile(pattern));
    }

    public String getFilteredPattern() {
        return logService.getLogFilterConfig().getLogPattern().toString();
    }

    public void setFilteredSourceClass(String name) {
        logService.getLogFilterConfig().setSourceClass(name);
    }

    public String getFilteredSourceClass() {
        return logService.getLogFilterConfig().getSourceClass();
    }

    public void setFilteredSourceMethod(String name) {
        logService.getLogFilterConfig().setSourceMethod(name);
    }

    public String getFilteredSourceMethod() {
        return logService.getLogFilterConfig().getSourceMethod();
    }

    public void setFilteredSourceThread(String name) {
        logService.getLogFilterConfig().setSourceThread(name);
    }

    public String getFilteredSourceThread() {
        return logService.getLogFilterConfig().getSourceThread();
    }

    public void setFilteredThrownClass(String name) {
        logService.getLogFilterConfig().setThrownClass(name);
    }

    public String getFilteredThrownClass() {
        return logService.getLogFilterConfig().getThrownClass();
    }

    public void setFilteredTimestampHi(long value) {
        logService.getLogFilterConfig().getTimestampHi();
    }

    public long getFilteredTimestampHi() {
        return logService.getLogFilterConfig().getTimestampHi();
    }

    public void setFilteredTimestampLo(long value) {
        logService.getLogFilterConfig().getTimestampLo();
    }

    public long getFilteredTimestampLo() {
        return logService.getLogFilterConfig().getTimestampLo();
    }
}