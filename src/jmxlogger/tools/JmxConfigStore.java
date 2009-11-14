/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JmxConfigStore {
    private ConcurrentHashMap<String,Object> config;
    private ArrayList<JmxConfigStore.ConfigEventListener> listeners;
    private ExecutorService publisher;;
    
    public JmxConfigStore(){
        config = new ConcurrentHashMap<String,Object>();
        listeners = new ArrayList<JmxConfigStore.ConfigEventListener>();
        setupPublisher();
    }
    public JmxConfigStore(Map<String,Object> values){
        config = new ConcurrentHashMap<String,Object>(values);
        listeners = new ArrayList<JmxConfigStore.ConfigEventListener>();
        setupPublisher();
    }

    public synchronized void addListener(JmxConfigStore.ConfigEventListener l){
        listeners.add(l);
    }

    public synchronized void removeListener(JmxConfigStore.ConfigEventListener l){
        listeners.remove(l);
    }

    public List<JmxConfigStore.ConfigEventListener> getListeners() {
        return listeners;
    }

    public synchronized void putValue(String key, Object value){
        config.put(key, value);
    }
    
    public synchronized Object getValue(String key){
        return config.get(key);
    }

    public synchronized Map getValues() {
        return config;
    }
    
    private void setupPublisher() {
        publisher = Executors.newSingleThreadExecutor();
    }

    public void postEvent(JmxConfigStore.ConfigEvent event){
        if(event.getSource() == null){
            throw new IllegalArgumentException("Config Event must have a source.");
        }
        publishPutEventToListeners(event, listeners);
    }

    private void publishPutEventToListeners(final JmxConfigStore.ConfigEvent event, final List<JmxConfigStore.ConfigEventListener> listeners){
        publisher.execute(new Runnable() {
            public void run() {
                for(JmxConfigStore.ConfigEventListener l : listeners){
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
    
    public static interface ConfigEventListener extends java.util.EventListener{
        public void onValueChanged(JmxConfigStore.ConfigEvent event);
    }

}
