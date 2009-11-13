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
    private JmxLogConfigStore config;

    public JmxLogFilter(){
        config = new JmxLogConfigStore();
    }
    public JmxLogFilter(JmxLogConfigStore cfg){
        config = cfg;
    }
    public void setLogFilterConfig(JmxLogConfigStore cfg){
        config = cfg;
    }
    public JmxLogConfigStore getLogFilterConfig() {
        return config;
    }

    public boolean isLogAllowed(JmxEventWrapper eventWrapper){
        Map<String,Object> event = eventWrapper.unwrap();
        return false;
    }

}
