package jmxlogger.tools.loghub;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import jmxlogger.tools.JmxLogEmitterMBean;
import jmxlogger.tools.ToolBox;

public class ClientService {
    private JMXConnector connector;
    private MBeanServerConnection server;

    public String connect(String url, String uname, String pwd){
        try{
            JMXServiceURL serviceUrl = ToolBox.createServiceUrlFromString(url);
            HashMap env = new HashMap();
            env.put (JMXConnector.CREDENTIALS, new String[] {uname, pwd});
            connector = JMXConnectorFactory.connect(serviceUrl, env);
            server = connector.getMBeanServerConnection();
            return connector.getConnectionId();
        }catch(IOException ioe){
            throw new RuntimeException("Error while connecting to MBeanServer: " + ioe.getMessage(), ioe);
        }catch(Exception ex){
            throw new RuntimeException("Unable to connect to server: " +  ex.getMessage(), ex);
        }
    }

    public void disconnect(){
        try {
            connector.close();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to close connection to JMX server: " + ex.getMessage(), ex);
        }
    }

    public JmxLogEmitterMBean getLogEmitter(ObjectName emitterName){
        if(server != null){
            return JMX.newMBeanProxy(server, emitterName, JmxLogEmitterMBean.class, true);
        }
        return null;
    }

    public void addListenerToLogEmitter(ObjectName emitterName, NotificationListener listener){
        try {
            server.addNotificationListener(emitterName, listener, null, null);
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException("Error adding log emitter listener: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new RuntimeException("Error adding log emitter listener: " + ex.getMessage(), ex);
        }
    }
}
