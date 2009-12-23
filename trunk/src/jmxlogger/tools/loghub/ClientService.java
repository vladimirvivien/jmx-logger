package jmxlogger.tools.loghub;

import java.io.IOException;
import java.util.HashMap;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import jmxlogger.tools.ToolBox;

public class ClientService {
    private JMXServiceURL serviceUrl;
    private JMXConnector connector;

    public void connect(String url, String uname, String pwd){
        try{
            serviceUrl = ToolBox.createServiceUrlFromString(url);
            HashMap   env = new HashMap();
            env.put (JMXConnector.CREDENTIALS, new String[] {uname, pwd});
            connector = JMXConnectorFactory.connect(serviceUrl, env);
        }catch(IOException ioe){
            throw new RuntimeException("Error while connecting to MBeanServer: ", ioe);
        }catch(Exception ex){
            throw new RuntimeException("Unable to connect to server: ", ex);
        }
    }
}
