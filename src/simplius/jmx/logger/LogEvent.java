package simplius.jmx.logger;

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
    private int sourceThreadId;
    private long timeStamp;
    private long seqNumber;
    private Throwable ex;

    public LogEvent() {}

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

    public int getSourceThreadId() {
        return sourceThreadId;
    }

    public void setSourceThreadId(int sourceThreadId) {
        this.sourceThreadId = sourceThreadId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
