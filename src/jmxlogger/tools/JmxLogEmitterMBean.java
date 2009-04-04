package jmxlogger.tools;

/**
 *
 * @author vladimir.vivien
 */
public interface JmxLogEmitterMBean {
    public void start();
    public void stop();
    public boolean isStarted();
    public String getStartDate();
    public long getLogCount();
}
