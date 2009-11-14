/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jmxlogger.integration.log4j.JmxLogAppender;
import jmxlogger.integration.logutil.JmxLogHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import jmxlogger.tools.JmxLogService;
import jmxlogger.tools.ToolBox;

/**
 *
 * @author VVivien
 */
public class JmxLogServiceTest {
    private ObjectName objName;
    private MBeanServer server;

    public JmxLogServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception{
        objName = new ObjectName("test:type=ObjectName");
        server = ManagementFactory.getPlatformMBeanServer();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateInstance() {
        JmxLogService l1 = JmxLogService.createInstance();
        assert l1 != null : "JmxLogService.createInstance() is returning null";
        assert l1 != JmxLogService.createInstance() : "JmxLogService.createInstance() is not initializing new instance.";
    }

//    @Test
//    public void testSetMBeanServer() {
//        JmxLogService l = JmxLogService.createInstance();
//        l.setMBeanServer(javax.management.MBeanServerFactory.createMBeanServer());
//        assert l.getMBeanServer() != null : "JmxLogService not setting isntance MBeanServer";
//        assert ! l.getMBeanServer().equals(java.lang.management.ManagementFactory.getPlatformMBeanServer())
//                : "JmxLogService setting MBeanServer instance to platform MBeanServer";
//    }
//
//
//    @Test
//    public void testSetObjectName() throws Exception{
//        JmxLogService l = JmxLogService.createInstance();
//        l.setObjectName(objName);
//        assert objName.equals(l.getObjectName()) : "JmxLogService not setting ObjectName properly.";
//    }
//
//    @Test
//    public void testStart() throws Exception{
//        JmxLogService l = JmxLogService.createInstance();
//        l.setObjectName(objName);
//        l.setMBeanServer(server);
//        l.start();
//        assert l.isStarted() : "JmxLogService not starting";
//        assert java.lang.management.ManagementFactory.getPlatformMBeanServer().isRegistered(objName)
//                : "JmxLogService start() is not registering internal MBean object";
//    }
//
//    @Test
//    public void testStop() throws Exception{
//        JmxLogService l = JmxLogService.createInstance();
//        l.setMBeanServer(server);
//        l.setObjectName(objName);
//        l.start();
//        assert l.isStarted() : "JmxLogService not starting";
//        l.stop();
//        assert !java.lang.management.ManagementFactory.getPlatformMBeanServer().isRegistered(objName)
//                : "JmxLogService stop() is not unregistering internal MBean object";
//    }
//
//    @Test
//    public void testLog() throws Exception{
//        JmxLogService l = JmxLogService.createInstance();
//        LogListener lstnr = new LogListener();
//        l.setMBeanServer(server);
//        l.setObjectName(objName);
//        l.start();
//        l.getMBeanServer().addNotificationListener(objName, lstnr, null, null);
//        Map<String,Object> event = new HashMap<String,Object>();
//        event.put(ToolBox.KEY_EVENT_SOURCE, l.getClass().getName());
//        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, "Hello, this is a logged message.");
//
//        l.log(event);
//
//        // lets stall to give thread time to settle
//        int count = 0;
//        while(count < 10 && lstnr.getNoteCount() <= 0){
//            try {
//                Thread.currentThread().sleep(500);
//                count++;
//                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JmxLogServiceTest.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//
//        assert lstnr.getNoteCount() > 0;
//    }
//
//    @Test
//    public void testSetLogger() {
//        JmxLogService svc = JmxLogService.createInstance();
//        svc.setLogger(new JmxLogAppender());
//    }
//
//    @Test
//    public void testSetLo4JLoggerLevel(){
//        JmxLogAppender log4j = new JmxLogAppender();
//        JmxLogService svc = JmxLogService.createInstance();
//        svc.setLogger(log4j);
//        svc.setLoggerLevel("DEBUG");
//        assert log4j.getLogLevel().equals("DEBUG");
//        assert !log4j.getLogLevel().equals("ERROR");
//
//        svc.setLoggerLevel("INFO");
//        assert log4j.getLogLevel().equals("INFO");
//
//        svc.setLoggerLevel("WARN");
//        assert log4j.getLogLevel().equals("WARN");
//
//        svc.setLoggerLevel("ERROR");
//        assert log4j.getLogLevel().equals("ERROR");
//
//        svc.setLoggerLevel("FATAL");
//        assert log4j.getLogLevel().equals("FATAL");
//
//    }
//
//    @Test
//    public void testGetLo4JLoggerLevel(){
//        JmxLogAppender log4j = new JmxLogAppender();
//        JmxLogService svc = JmxLogService.createInstance();
//        svc.setLogger(log4j);
//        svc.setLoggerLevel("DEBUG");
//        assert svc.getLoggerLevel().equals("DEBUG");
//        assert !svc.getLoggerLevel().equals("ERROR");
//
//        svc.setLoggerLevel("INFO");
//        assert svc.getLoggerLevel().equals("INFO");
//
//        svc.setLoggerLevel("WARN");
//        assert svc.getLoggerLevel().equals("WARN");
//
//        svc.setLoggerLevel("ERROR");
//        assert svc.getLoggerLevel().equals("ERROR");
//
//        svc.setLoggerLevel("FATAL");
//        assert svc.getLoggerLevel().equals("FATAL");
//
//        svc.setLoggerLevel("OFF");
//        assert svc.getLoggerLevel().equals("OFF");
//    }
//
//    @Test
//    public void testGetJavaLoggerLevel(){
//        JmxLogHandler log4j = new JmxLogHandler();
//        JmxLogService svc = JmxLogService.createInstance();
//        svc.setLogger(log4j);
//        svc.setLoggerLevel("FINE");
//        assert svc.getLoggerLevel().equals("FINE");
//        assert !svc.getLoggerLevel().equals("FINEST");
//
//        svc.setLoggerLevel("INFO");
//        assert svc.getLoggerLevel().equals("INFO");
//
//        svc.setLoggerLevel("WARNING");
//        assert svc.getLoggerLevel().equals("WARNING");
//
//        svc.setLoggerLevel("CONFIG");
//        assert svc.getLoggerLevel().equals("CONFIG");
//
//        svc.setLoggerLevel("SEVERE");
//        assert svc.getLoggerLevel().equals("SEVERE");
//
//        svc.setLoggerLevel("OFF");
//        assert svc.getLoggerLevel().equals("OFF");
//    }
}