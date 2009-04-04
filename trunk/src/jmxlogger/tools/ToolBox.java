package jmxlogger.tools;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class ToolBox {
    private static String DEFAULT_NAME = "jmxlogger:type=LogEmitter";

    /**
     * Find an MBeanServer based on agentId.
     * If no server is found based on agent id, it looks for any server created
     * using management factory method.  If none is found, it returns the platform server.
     * @param agentId
     * @return
     */
    public static MBeanServer findMBeanServer(String agentId) {
        MBeanServer server = null;
        ArrayList<MBeanServer> servers = javax.management.MBeanServerFactory.findMBeanServer(agentId);
        if (servers.size() > 0) {
            server = servers.get(0);
        } else {
            servers = javax.management.MBeanServerFactory.findMBeanServer(null);
            if(servers.size() > 0){
                server = servers.get(0);
            }else{
                server = ManagementFactory.getPlatformMBeanServer();
            }
        }
        return server;
    }

    public static ObjectName buildObjectName(String name){
        ObjectName objName = null;
        try {
            objName = new ObjectName(name);
        } catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex);
        } catch (NullPointerException ex) {
            throw new RuntimeException(ex);
        }
        return objName;
    }

    public static ObjectName buildDefaultObjectName(String id){
        String seed = (id != null) ? id : Long.toString(System.currentTimeMillis());
        return ToolBox.buildObjectName(DEFAULT_NAME + "@" + seed);
    }


   public static void registerMBean(MBeanServer server, ObjectName beanName, Object object) {
        try {
            if (server.isRegistered(beanName)) {
                server.unregisterMBean(beanName);
            }
            server.registerMBean(object, beanName);
        } catch (InstanceAlreadyExistsException ex) {
            throw new RuntimeException(ex);
        } catch (NotCompliantMBeanException ex) {
            throw new RuntimeException(ex);
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (MBeanRegistrationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void unregisterMBean(MBeanServer server, ObjectName beanName) {
        try {
            if(server.isRegistered(beanName)){
                server.unregisterMBean(beanName);
            }
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (MBeanRegistrationException ex) {
            throw new RuntimeException(ex);
        }
    }


}
