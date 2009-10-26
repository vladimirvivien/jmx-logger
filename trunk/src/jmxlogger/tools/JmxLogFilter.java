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
    private JmxLogConfig config;

    public JmxLogFilter(){
        config = new JmxLogConfig();
    }
    public JmxLogFilter(JmxLogConfig cfg){
        config = cfg;
    }
    public void setLogFilterConfig(JmxLogConfig cfg){
        config = cfg;
    }
    public JmxLogConfig getLogFilterConfig() {
        return config;
    }

    public boolean isLogAllowed(JmxEventWrapper eventWrapper){
        Map<String,Object> event = eventWrapper.unwrap();
        return (config.getLogPattern() != null && isLogPatternAllowed(event)) ||
                (config.getSourceClass() != null && isSourceClassAllowed(event)) ||
                (config.getSourceMethod() != null && isSourceMethodAllowed(event)) ||
                (config.getSourceThread() != null && isSourceThreadAllowed(event)) ||
                (config.getThrownClass() != null && isThrownClassAllowed(event)) ||
                (config.getTimestampLo() > -1L && isTimestampGeLoBound(event)) ||
                (config.getTimestampHi() > -1L && isTimestampLeHiBound(event));
    }

    private boolean isLogPatternAllowed(Map<String,Object> event){
        String fmtMsg = (String)event.get(ToolBox.KEY_EVENT_FORMATTED_MESSAGE);
        String rawMsg = (String)event.get(ToolBox.KEY_EVENT_RAW_MESSAGE);
        if(fmtMsg != null && rawMsg == null)
            return config.getLogPattern().matcher(fmtMsg).find();
        else if (rawMsg != null && fmtMsg == null)
            return config.getLogPattern().matcher(rawMsg).find();
        else if(fmtMsg != null && rawMsg != null)
            return config.getLogPattern().matcher(fmtMsg).find() ||
                    config.getLogPattern().matcher(rawMsg).find();
        return false;
    }

    private boolean isSourceClassAllowed(Map<String,Object> event){
        String srcClass = (String)event.get(ToolBox.KEY_EVENT_SOURCE_CLASS);
        return (srcClass != null && srcClass.equals(config.getSourceClass()));
    }

    private boolean isSourceMethodAllowed(Map<String,Object> event){
        String srcMeth = (String)event.get(ToolBox.KEY_EVENT_SOURCE_METHOD);
        return (srcMeth != null && srcMeth.equalsIgnoreCase(config.getSourceMethod()));
    }

    private boolean isSourceThreadAllowed(Map<String,Object> event){
        String srcThread = (String)event.get(ToolBox.KEY_EVENT_SOURCE_THREAD);
        return (srcThread != null && srcThread.equalsIgnoreCase(config.getSourceThread()));
    }

    private boolean isThrownClassAllowed(Map<String,Object> event){
        String cls = (String)event.get(ToolBox.KEY_EVENT_THROWABLE);
        return (cls != null && cls.equalsIgnoreCase(config.getThrownClass()));
    }

    private boolean isTimestampGeLoBound(Map<String,Object> event){
        Long ts = (Long)event.get(ToolBox.KEY_EVENT_TIME_STAMP);
        return (ts != null && ts.longValue() >= config.getTimestampLo());
    }

    private boolean isTimestampLeHiBound(Map<String,Object> event){
        Long ts = (Long)event.get(ToolBox.KEY_EVENT_TIME_STAMP);
        return (ts != null && ts.longValue() <= config.getTimestampHi());
    }
}
