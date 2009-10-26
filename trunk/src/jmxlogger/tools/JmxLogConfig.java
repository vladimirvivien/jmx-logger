/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import java.util.regex.Pattern;

/**
 *
 * @author vvivien
 */
public class JmxLogConfig {
    private String level;
    private Pattern logPattern;
    private String sourceClass;
    private String sourceMethod;
    private String sourceThread;
    private String thrownClass;
    private long timestampHi = -1L;
    private long timestampLo = -1L;

    public String getLevel(){
        return level;
    }
    public void setLevel(String l){
        level = l;
    }
    public Pattern getLogPattern() {
        return logPattern;
    }

    public void setLogPattern(Pattern logPattern) {
        this.logPattern = logPattern;
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
