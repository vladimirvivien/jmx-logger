package org.simplius.jmx.logger.test;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * @author vladimir.vivien
 */
public class LogListener implements NotificationListener {

    private volatile long noteCount;

    public long getNoteCount() {
        return noteCount;
    }

    public synchronized void handleNotification(Notification notification, Object handback) {
        noteCount++;
    }
}
