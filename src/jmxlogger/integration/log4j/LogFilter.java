/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.integration.log4j;

import java.util.regex.Pattern;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author vvivien
 */
public class LogFilter extends Filter{
    private Pattern logPattern;
    private String sourceClass;
    private String sourceMethod;
    private String sourceThread;
    private String thrownClass;
    private long timestampHi;
    private long timestampLo;

    @Override
    public int decide(LoggingEvent e) {
        return ACCEPT;
    }

    public String getLogPattern() {
        return logPattern.toString();
    }

    public void setLogPattern(String logPattern) {
        this.logPattern = Pattern.compile(logPattern);
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(String sourceClass) {
        this.sourceClass = sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public void setSourceMethod(String sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    public String getSourceThread() {
        return sourceThread;
    }

    public void setSourceThread(String sourceThread) {
        this.sourceThread = sourceThread;
    }

    public String getThrownClass() {
        return thrownClass;
    }

    public void setThrownClass(String thrownClass) {
        this.thrownClass = thrownClass;
    }

    public long getTimestampHi() {
        return timestampHi;
    }

    public void setTimestampHi(long timestampHi) {
        this.timestampHi = timestampHi;
    }

    public long getTimestampLo() {
        return timestampLo;
    }

    public void setTimestampLo(long timestampLo) {
        this.timestampLo = timestampLo;
    }

}
