/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import java.util.HashMap;
import java.util.Map;

public class JmxLogConfig {
    private HashMap<String,Object> config;
    public JmxLogConfig(){
        config = new HashMap<String,Object>();
    }
    public JmxLogConfig(Map<String,Object> values){
        config = (HashMap) values;
    }
    public void putValues(final Map<String,Object> values){
        config = (HashMap) values;
    }
    public void putValue(String key, Object value){
        config.put(key, value);
    }
    public Object getValue(String key){
        return config.get(key);
    }
    public Map<String,Object> getValues() {
        return config;
    }
}
