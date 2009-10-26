/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.integration.log4j;

import java.util.regex.Pattern;
import jmxlogger.tools.JmxLogConfig;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author vvivien
 */
public class DefaultLog4jFilter extends Filter{
    private JmxLogConfig config;

    public DefaultLog4jFilter() {
        config = new JmxLogConfig();
    }

    public JmxLogConfig getLogFilterConfig() {
        return config;
    }

    @Override
    public int decide(LoggingEvent e) {
        return ACCEPT;
    }

    public String getLogPattern() {
        return config.getLogPattern().toString();
    }

    public void setLogPattern(String logPattern) {
        this.config.setLogPattern(Pattern.compile(logPattern != null ? logPattern : ""));
    }

    public String getSourceClass() {
        return config.getSourceClass();
    }

    public void setSourceClass(String sourceClass) {
        config.setSourceClass(sourceClass);
    }

    public String getSourceMethod() {
        return config.getSourceMethod();
    }

    public void setSourceMethod(String sourceMethod) {
        config.setSourceMethod(sourceMethod);
    }

    public String getSourceThread() {
        return config.getSourceThread();
    }

    public void setSourceThread(String sourceThread) {
        this.config.setSourceThread(sourceThread);
    }

    public String getThrownClass() {
        return config.getThrownClass();
    }

    public void setThrownClass(String thrownClass) {
        this.config.setThrownClass(thrownClass);
    }

    public long getTimestampHi() {
        return config.getTimestampHi();
    }

    public void setTimestampHi(long timestampHi) {
        this.config.setTimestampHi(timestampHi);
    }

    public long getTimestampLo() {
        return config.getTimestampLo();
    }

    public void setTimestampLo(long timestampLo) {
        this.config.setTimestampLo(timestampLo);
    }

}
