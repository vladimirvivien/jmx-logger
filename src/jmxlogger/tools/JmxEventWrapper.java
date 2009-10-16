package jmxlogger.tools;

import java.util.Map;

public class JmxEventWrapper implements Comparable{
    private Map<String,Object> event;
    public JmxEventWrapper(Map<String,Object> event){
        this.event = event;
    }

    public Map<String,Object> unwrap() {
        return event;
    }
    public int compareTo(Object o) {
        Map<String,Object> otherEvent = ((JmxEventWrapper)o).unwrap();
        long thisTimeStamp = (Long)event.get(ToolBox.KEY_EVENT_TIME_STAMP);
        long otherTimeStamp = (Long)otherEvent.get(ToolBox.KEY_EVENT_TIME_STAMP);

        if(thisTimeStamp < otherTimeStamp)
            return -1;
        else if(thisTimeStamp > otherTimeStamp)
            return 1;
        else return 0;
    }

    @Override
    public String toString() {
        return event.toString();
    }

    @Override
    public boolean equals(Object otherObject){
        return event.equals(otherObject);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.event != null ? this.event.hashCode() : 0);
        return hash;
    }
}
