package jmxlogger.tools;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**
 *
 * @author vladimir.vivien
 */
public class JmxLogEmitter extends NotificationBroadcasterSupport implements JmxLogEmitterMBean{
    private volatile boolean started;
    private volatile long count;
    private volatile long seq;

    private Date startDate;

    public synchronized void start() {
        if(started) return;
        started = true;
    }

    public synchronized void stop() {
        if(!started) return;
        started = false;
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
    }

    private Notification buildNotification(LogEvent event){
        Notification note = new Notification("Hello", this, 12);
        return note;
    }
}
