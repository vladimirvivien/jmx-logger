/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author vvivien
 */
public class JmxLogFilter {
    private Pattern logPattern;
    private String sourceClass;
    private String sourceMethod;
    private String sourceThread;
    private String thrownClass;
    private long timestampHi = -1L;
    private long timestampLo = -1L;

    public boolean isLogAllowed(JmxEventWrapper eventWrapper){
        Map<String,Object> event = eventWrapper.unwrap();
        return (logPattern != null && isLogPatternAllowed(event)) ||
                (sourceClass != null && isSourceClassAllowed(event)) ||
                (sourceMethod != null && isSourceMethodAllowed(event)) ||
                (sourceThread != null && isSourceThreadAllowed(event)) ||
                (thrownClass != null && isThrownClassAllowed(event)) ||
                (timestampLo > -1L && isTimestampGeLoBound(event)) ||
                (timestampHi > -1L && isTimestampLeHiBound(event));
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

    private boolean isLogPatternAllowed(Map<String,Object> event){
        String fmtMsg = (String)event.get(ToolBox.KEY_EVENT_FORMATTED_MESSAGE);
        String rawMsg = (String)event.get(ToolBox.KEY_EVENT_RAW_MESSAGE);
        if(fmtMsg != null && rawMsg == null)
            return logPattern.matcher(fmtMsg).find();
        else if (rawMsg != null && fmtMsg == null)
            return logPattern.matcher(rawMsg).find();
        else if(fmtMsg != null && rawMsg != null)
            return logPattern.matcher(fmtMsg).find() ||
                    logPattern.matcher(rawMsg).find();
        return false;
    }

    private boolean isSourceClassAllowed(Map<String,Object> event){
        String srcClass = (String)event.get(ToolBox.KEY_EVENT_SOURCE_CLASS);
        return (srcClass != null && srcClass.equals(this.sourceClass));
    }

    private boolean isSourceMethodAllowed(Map<String,Object> event){
        String srcMeth = (String)event.get(ToolBox.KEY_EVENT_SOURCE_METHOD);
        return (srcMeth != null && srcMeth.equals(this.sourceMethod));
    }

    private boolean isSourceThreadAllowed(Map<String,Object> event){
        String srcThread = (String)event.get(ToolBox.KEY_EVENT_SOURCE_THREAD);
        return (srcThread != null && srcThread.equals(this.sourceThread));
    }

    private boolean isThrownClassAllowed(Map<String,Object> event){
        String cls = (String)event.get(ToolBox.KEY_EVENT_THROWABLE);
        return (cls != null && cls.equals(this.thrownClass));
    }

    private boolean isTimestampGeLoBound(Map<String,Object> event){
        Long ts = (Long)event.get(ToolBox.KEY_EVENT_TIME_STAMP);
        return (ts != null && ts.longValue() >= this.timestampLo);
    }

    private boolean isTimestampLeHiBound(Map<String,Object> event){
        Long ts = (Long)event.get(ToolBox.KEY_EVENT_TIME_STAMP);
        return (ts != null && ts.longValue() <= this.timestampHi);
    }
}
