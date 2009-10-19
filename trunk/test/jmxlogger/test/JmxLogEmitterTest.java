/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import jmxlogger.integration.log4j.JmxLogAppender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import jmxlogger.tools.JmxLogEmitter;
import jmxlogger.tools.JmxLogService;
import jmxlogger.tools.ToolBox;

/**
 *
 * @author vvivien
 */
public class JmxLogEmitterTest {
    private MBeanServer server;
    ObjectName name;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        server = java.lang.management.ManagementFactory.getPlatformMBeanServer();
        try {
            name = new ObjectName("test:type=MBean");
        }catch (MalformedObjectNameException ex) {
            throw new RuntimeException(ex);
        } catch (NullPointerException ex) {
            throw new RuntimeException(ex);
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsValidMBean() {
        try {
            registerMBean(new JmxLogEmitter(), name);
            assert server.getObjectInstance(name) != null : "MBean is not registered";
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void registerMBean(Object o, ObjectName name) {
        try {
            if(server.isRegistered(name)){
                server.unregisterMBean(name);
            }
            server.registerMBean(o, name);
            assert server.getObjectInstance(name) != null : "MBean is not registered";
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (InstanceAlreadyExistsException ex) {
            throw new RuntimeException(ex);
        } catch (MBeanRegistrationException ex) {
            throw new RuntimeException(ex);
        } catch (NotCompliantMBeanException ex) {
            throw new RuntimeException(ex);
        } catch (NullPointerException ex) {
            throw new RuntimeException(ex);
        }        
    }

    @Test
    public void testLifecycleMethods() {
        JmxLogEmitter e = new JmxLogEmitter();
        e.start();
        assert e.isStarted() : "Emitter not started after start() call.";
        e.stop();
        assert !e.isStarted() : "Emitter not stopped after call to stop()";
    }

    @Test
    public void testLogCount() {
        JmxLogEmitter e = new JmxLogEmitter();
        e.start();
        Map<String,Object> event = new HashMap<String,Object>();
        event.put(ToolBox.KEY_EVENT_SOURCE, e.getClass().getName());
        event.put(ToolBox.KEY_EVENT_MESSAGE, "Hello, this is a logged message.");

        e.sendLog(event);
        // lets stall to give thread time to settle
        int count = 0;
        while(count < 10 && e.getLogCount() <= 0){
            try {
                Thread.currentThread().sleep(500);
                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
                count++;
            } catch (InterruptedException ex) {
                Logger.getLogger(JmxLogEmitterTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        assert e.getLogCount() > 0 : "Log count is not increasing";
        e.stop();
    }

    @Test
    public void testSendCount() {
        JmxLogEmitter e = new JmxLogEmitter();
        registerMBean(e, name);
        LogListener listener = new LogListener();
        try {
            server.addNotificationListener(name, listener, null, null);
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        Map<String,Object> event = new HashMap<String,Object>();
        event.put(ToolBox.KEY_EVENT_SOURCE, e.getClass().getName());
        event.put(ToolBox.KEY_EVENT_MESSAGE, "Hello, this is a logged message.");
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(System.currentTimeMillis()));
        event.put(ToolBox.KEY_EVENT_SEQ_NUM, new Long(System.currentTimeMillis()));
        e.start();
        e.sendLog(event);

        // lets stall to give thread time to settle
        int count = 0;
        while(count < 10 && listener.getNoteCount() <= 0){
            try {
                Thread.currentThread().sleep(500);
                count++;
                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
            } catch (InterruptedException ex) {
                Logger.getLogger(JmxLogEmitterTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assert listener.getNoteCount() > 0 : "JmxLogEmitter MBean not emitting sendLog() event";
        e.stop();
    }

    @Test
    public void testGetLogLevel() {
        JmxLogAppender a = new JmxLogAppender();
        JmxLogService  svc = JmxLogService.createInstance();
        svc.setLogger(a);
        JmxLogEmitter e = new JmxLogEmitter();
        e.setLogService(svc);
        assert e.getLogLevel().equals("DEBUG");
        e.setLogLevel("WARN");
        assert e.getLogLevel().equals("WARN");
    }

}