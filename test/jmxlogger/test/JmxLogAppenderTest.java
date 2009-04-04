/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jmxlogger.integration.log4j.JmxLogAppender;
import jmxlogger.tools.ToolBox;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vladimir
 */
public class JmxLogAppenderTest {
    private MBeanServer platformServer;
    private ObjectName objectName;
    private LogListener lstnr;

    public JmxLogAppenderTest() {
        platformServer = ManagementFactory.getPlatformMBeanServer();
        objectName = ToolBox.buildObjectName("test:type=ObjectName");
        lstnr = new LogListener();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructors() {
        JmxLogAppender l = new JmxLogAppender();
        assert l.getMBeanServer().equals(platformServer) : "JmxLogAppender - no default server found.";
        assert l.getObjectName() != null : "JmxLogAppender - default name not set.";
        assert l.getLayout() != null : "JmxLogAppender - default layout not set.";


        l = new JmxLogAppender(javax.management.MBeanServerFactory.createMBeanServer("test"));
        assert !l.getMBeanServer().equals(platformServer) : "JmxLogAppender - constructor not setting server";
        assert l.getMBeanServer().getDefaultDomain().equals("test");

        l = new JmxLogAppender(objectName);
        assert l.getObjectName().equals(objectName) : "JmxLogAppender - constructor not seting object name.";
        assert l.getMBeanServer().equals(platformServer) : "JmxLogAppender - no default server found.";
    }

    @Test
    public void testSetMBeanServer() {
        JmxLogAppender l = new JmxLogAppender();
        l.setMBeanServer(platformServer);
        assert l.getMBeanServer().equals(platformServer) : "JmxLogAppender - MBeanServer setter failing.";
    }

    @Test
    public void testSetObjectName(){
        JmxLogAppender l = new JmxLogAppender();
        l.setObjectName(objectName);
        assert l.getObjectName().equals(objectName) : "JmxLogAppender - ObjectName setter fails.";
    }

    @Test
    public void testSetLogPattern() {
        JmxLogAppender l = new JmxLogAppender();
        l.setLogPattern("somePattern");
        assert l.getLogPattern() != null : "JmxLogAppender - LogPattern setter fails.";
    }

    @Test
    public void testServerSelection() {
        JmxLogAppender l = new JmxLogAppender();
        l.setServerSelection("someServer");
        assert l.getServerSelection().equals("someServer") : "JmxLogAppender - ServerSelection sertter tails";
    }
}