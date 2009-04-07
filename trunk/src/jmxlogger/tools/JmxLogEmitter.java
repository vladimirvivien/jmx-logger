package jmxlogger.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**
 *
 * @author vladimir.vivien
 */
public class JmxLogEmitter extends NotificationBroadcasterSupport implements JmxLogEmitterMBean{
    Logger log = Logger.getLogger(JmxLogEmitter.class.getName());
    private volatile boolean started;
    private volatile long count;

    private Date startDate;

    public synchronized void start() {
        if(started) return;
        started = true;
        startDate = new Date();
        log.info("JMX Log Emitter component started on " + startDate);
    }

    public synchronized void stop() {
        if(!started) return;
        started = false;
        log.info("JMX Log Emitter component stopped.");
    }

    public synchronized boolean isStarted() {
        return started;
    }

    public String getStartDate() {
        return new SimpleDateFormat().format(startDate);
    }

    public synchronized long getLogCount() {
        return count;
    }

    public synchronized void sendLog(LogEvent event){
        super.sendNotification(buildNotification(event));
        count++;
        log.finest("Log event sent to JMX event bus.");
    }

    private Notification buildNotification(LogEvent event){
        Notification note = new Notification(
                ToolBox.getDefaultEventType(),
                event.getSource(),
                event.getSequenceNumber(),
                event.getTimeStamp(),
                event.getMessage());
        note.setUserData(event);
        log.finest("JMX notification to be sent [" + note.toString() + "]");
        return note;
    }
}
