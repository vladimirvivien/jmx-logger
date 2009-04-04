/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import javax.management.ObjectName;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import jmxlogger.tools.JmxEventLogger;
import jmxlogger.tools.LogEvent;
import static org.junit.Assert.*;

/**
 *
 * @author VVivien
 */
public class JmxEventLoggerTest {
    private ObjectName objName;
    public JmxEventLoggerTest() {
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateInstance() {
        JmxEventLogger l1 = JmxEventLogger.createInstance();
        assert l1 != null : "JmxEventLogger.createInstance() is returning null";
        assert l1 != JmxEventLogger.createInstance() : "JmxEventLogger.createInstance() is not initializing new instance.";
    }

    @Test
    public void testSetMBeanServer() {
        JmxEventLogger l = JmxEventLogger.createInstance();
        assert l.getMBeanServer().equals(java.lang.management.ManagementFactory.getPlatformMBeanServer())
                : "JmxEventLogger not initializing default MBeanServer";
        l.setMBeanServer(javax.management.MBeanServerFactory.createMBeanServer());
        assert l.getMBeanServer() != null : "JmxEventLogger not setting isntance MBeanServer";
        assert ! l.getMBeanServer().equals(java.lang.management.ManagementFactory.getPlatformMBeanServer())
                : "JmxEventLogger setting MBeanServer instance to platform MBeanServer";
    }


    @Test
    public void testSetObjectName() throws Exception{
        JmxEventLogger l = JmxEventLogger.createInstance();
        assert l.getObjectName() != null : "JmxEventLogger not setting default ObjectName";
        assert l.getObjectName().toString().contains("jmx.logger:type=logging") : "JmxEventLogger not setting default ObjectName";
        String objName = "test:type=ObjectName";
        l.setObjectName(new ObjectName(objName));
        assert objName.equals(l.getObjectName().toString()) : "JmxEventLogger not setting ObjectName properly.";
    }

    @Test
    public void testStart() throws Exception{
        JmxEventLogger l = JmxEventLogger.createInstance();
        l.setObjectName(objName);
        l.start();
        assert l.isStarted() : "JmxEventLogger not starting";
        assert java.lang.management.ManagementFactory.getPlatformMBeanServer().isRegistered(objName)
                : "JmxEventLogger start() is not registering internal MBean object";
    }

    @Test
    public void testStop() throws Exception{
        JmxEventLogger l = JmxEventLogger.createInstance();
        l.setObjectName(objName);
        l.start();
        assert l.isStarted() : "JmxEventLogger not starting";
        l.stop();
        assert !java.lang.management.ManagementFactory.getPlatformMBeanServer().isRegistered(objName)
                : "JmxEventLogger stop() is not unregistering internal MBean object";
    }

    @Test
    public void testLog() throws Exception{
        JmxEventLogger l = JmxEventLogger.createInstance();
        LogListener lstnr = new LogListener();
        l.setObjectName(objName);
        l.start();
        l.getMBeanServer().addNotificationListener(objName, lstnr, null, null);
        l.log(new LogEvent());
        assert lstnr.getNoteCount() > 0;

    }
}