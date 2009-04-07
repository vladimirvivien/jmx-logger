package jmxlogger.tools;

import java.io.Serializable;

/**
 * @author vladmir.vivien
 */
public class LogEvent<LOG_TYPE> implements Serializable{
    private LOG_TYPE logRecord;
    private Object source;
    private String loggerName;
    private String levelName;
    private String message;
    private String sourceClassName;
    private String sourceMethodName;
    private String sourceThreadId;
    private long timeStamp;
    private long seqNumber;
    private Throwable ex;

    public LogEvent() {}

    public LogEvent(Object source, String message, long timeStamp, long seqNumber) {
        setSource(source);
        setMessage(message);
        setTimeStamp(timeStamp);
        setSequenceNumber(seqNumber);
    }
    
    public synchronized void setSource(Object src){
        source = src;
    }

    public Object getSource(){
        return source;
    }

    public Throwable getSourceThrowable() {
        return ex;
    }

    public synchronized void setSourceThrowable(Throwable ex) {
        this.ex = ex;
    }

    public String getLevelName() {
        return levelName;
    }

    public synchronized void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public LOG_TYPE getLogRecord() {
        return logRecord;
    }

    public synchronized void setLogRecord(LOG_TYPE logRecord) {
        this.logRecord = logRecord;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public synchronized void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getMessage() {
        return message;
    }

    public synchronized void setMessage(String message) {
        this.message = message;
    }

    public long getSequenceNumber() {
        return seqNumber;
    }

    public void setSequenceNumber(long seqNumber) {
        this.seqNumber = seqNumber;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getSourceThreadId() {
        return sourceThreadId;
    }

    public void setSourceThreadId(String sourceThreadId) {
        this.sourceThreadId = sourceThreadId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return String.format(
                "[source=%s, " +
                "loggerName=%s, " +
                "levelName=%s, " +
                "sourceClass=%s, " +
                "sourceMethod=%s, " +
                "threadId=%s, " +
                "timeStapm=%s, " +
                "seqNumber=%s]",
                getSource().getClass().getName(),
                getLoggerName(),
                getLevelName(),
                getSourceClassName(),
                getSourceMethodName(),
                getSourceThreadId(),
                getTimeStamp(),
                getSequenceNumber());
    }
}
