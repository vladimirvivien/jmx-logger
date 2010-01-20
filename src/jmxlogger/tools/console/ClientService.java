package jmxlogger.tools.console;

import java.io.IOException;
import java.util.HashMap;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import jmxlogger.tools.JmxLogEmitterMBean;
import jmxlogger.tools.ToolBox;

public class ClientService {
    private JMXServiceURL serviceUrl;
    private JMXConnector connector;
    private MBeanServerConnection server;
    private ClientConnectionListener connListener;

    public ClientService(){}
    public ClientService(ClientConnectionListener l){
        connListener = l;
    }

    public String connect(String url, String uname, String pwd){
        try{
            serviceUrl = ToolBox.createServiceUrlFromString(url);

            HashMap env = new HashMap();
            env.put (JMXConnector.CREDENTIALS, new String[] {uname, pwd});
            connector = JMXConnectorFactory.connect(serviceUrl, env);
            server = connector.getMBeanServerConnection();

            // set connection listener
            if(connListener != null){
                NotificationListener noteListener = new NotificationListener(){
                    @Override
                    public void handleNotification(Notification notification, Object handback) {
                        if(notification.getType().equals("jmx.remote.connection.opened")){
                            connListener.onConnectionOpened();
                        }
                        if(notification.getType().equals("jmx.remote.connection.closed")){
                            connListener.onConnectionClosed();
                        }
                        if(notification.getType().equals("jmx.remote.connection.failed")){
                            connListener.onConnectionFailed();
                        }
                    }
                };

                connector.addConnectionNotificationListener(noteListener, null, null);

            }

            return connector.getConnectionId();

        }catch(IOException ioe){
            throw new RuntimeException("Error while connecting to MBeanServer: " + ioe.getMessage(), ioe);
        }catch(Exception ex){
            throw new RuntimeException("Unable to connect to server: " +  ex.getMessage(), ex);
        }
    }

    public String getServiceUrl(){
        return (serviceUrl != null) ? serviceUrl.toString() : null;
    }

    public String getConnectionId() {
        try{
            return (connector != null) ? connector.getConnectionId() : null;
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }

    public void disconnect(){
        try {
            connector.close();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to close connection to JMX server: " + ex.getMessage(), ex);
        }
    }

    public void setConnectionListener(ClientConnectionListener l){
        connListener = l;
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