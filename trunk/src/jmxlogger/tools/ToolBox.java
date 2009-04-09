package jmxlogger.tools;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.logging.ErrorManager;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class ToolBox {
    private static final ErrorManager EM = new ErrorManager();
    private static final String DEFAULT_NAME = "jmxlogger:type=LogEmitter";
    private static final String JMX_LOG_TYPE = "jmxlogger.log.event";

    /***
     * Returns the default event type of jmxlogger.log.event
     * @return jmxlogger.log.event
     */
    public static String getDefaultEventType() {
        return JMX_LOG_TYPE;
    }

    /**
     * Find an MBeanServer based on agentId.
     * If no server is found based on agent id, it looks for any server created
     * using management factory method.  If none is found, it returns the platform server.
     * @param agentId
     * @return instance of MBeanServer
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

    /**
     * Util method to build a standard JMX ObjectName.  It wraps all of the exceptions into a RuntimeException.
     * @param name - the string representation of ObjectName
     * @return new instance of ObjectName
     */
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

    /**
     * Builds a default ObjectName instance based on a provided ID (appended to the name).
     * @param id a string value
     * @return an ObjectName instance
     */
    public static ObjectName buildDefaultObjectName(String id){
        String seed = (id != null) ? id : Long.toString(System.currentTimeMillis());
        return ToolBox.buildObjectName(DEFAULT_NAME + "@" + seed);
    }


   /***
    * Registers an object as an mbean in the MBeanServer.
    * @param server - server
    * @param beanName - objectName to use
    * @param object - instance of management object
    */
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

   /**
    * Unregisters the specified MBean from the MBeanServer
    * @param server - server
    * @param beanName - object Name
    */
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

    /**
     * Internal Error reporter.
     * @param error - error to report
     */
    public static void reportError(String error, Exception ex){
        EM.error(error, ex, ErrorManager.GENERIC_FAILURE);
    }

}
