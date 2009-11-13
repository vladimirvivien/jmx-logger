/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JmxLogConfigStore {
    private ConcurrentHashMap<String,Object> config;
    private ArrayList<JmxLogConfigStore.EventListener> listeners;
    private ExecutorService publisher;;
    
    public JmxLogConfigStore(){
        config = new ConcurrentHashMap<String,Object>();
        listeners = new ArrayList();
        setupPublisher();
    }
    public JmxLogConfigStore(Map<String,Object> values){
        config = (ConcurrentHashMap) values;
        listeners = new ArrayList();
        setupPublisher();
    }

    public synchronized void addListener(JmxLogConfigStore.EventListener l){
        listeners.add(l);
    }

    public synchronized void putValue(String key, Object value){
        config.put(key, value);
    }
    
    public synchronized Object getValue(String key){
        return config.get(key);
    }
    
    private void setupPublisher() {
        publisher = Executors.newSingleThreadExecutor();
    }

    public void postEvent(JmxLogConfigStore.ConfigEvent event){
        publishPutEventToListeners(event, listeners);
    }

    private void publishPutEventToListeners(final JmxLogConfigStore.ConfigEvent event, final List<JmxLogConfigStore.EventListener> listeners){
        publisher.execute(new Runnable() {
            public void run() {
                for(JmxLogConfigStore.EventListener l : listeners){
                    l.onValueChanged(event);
                }
            }
        });
    }

    public static class ConfigEvent extends java.util.EventObject {
        private String key;
        private Object value;
        public ConfigEvent(Object source, String key, Object value){
            super(source);
            this.key = key;
            this.value = value;
        }
        public String getKey(){
            return key;
        }
        public Object getValue(){
            return value;
        }
    }
    public static interface EventListener extends java.util.EventListener{
        public void onValueChanged(JmxLogConfigStore.ConfigEvent event);
    }

}
