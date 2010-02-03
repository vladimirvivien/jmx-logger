/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.io.File;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jmxlogger.integration.log4j.JmxLogAppender;
import jmxlogger.tools.ToolBox;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.xml.DOMConfigurator;
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
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        platformServer = ManagementFactory.getPlatformMBeanServer();
        objectName = ToolBox.buildObjectName("jmxlogger:type=LogEmitter");
        lstnr = new LogListener();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDefaultCtor() {
        JmxLogAppender l = new JmxLogAppender();
        l.activateOptions();
        assert l.getMBeanServerValue().equals(platformServer) : "JmxLogAppender - no default server found.";
        assert l.getObjectNameValue() != null : "JmxLogAppender - default name not set.";
        assert l.getLayout() != null : "JmxLogAppender - default layout not set.";
    }

    @Test
    public void testCtorWithMBeanServerInstance() {
        JmxLogAppender l = new JmxLogAppender(javax.management.MBeanServerFactory.createMBeanServer("test"));
        l.activateOptions();
        assert !l.getMBeanServerValue().equals(platformServer) : "JmxLogAppender - constructor not setting server";
        assert l.getMBeanServerValue().getDefaultDomain().equals("test");
    }

    @Test
    public void testCtorWithObjectNameInstance(){
        platformServer = ManagementFactory.getPlatformMBeanServer();
        JmxLogAppender l = new JmxLogAppender(objectName);
        l.activateOptions();
        assert l.getObjectNameValue().equals(objectName) : "JmxLogAppender - constructor not seting object name.";
        assert l.getMBeanServerValue().equals(platformServer) : "JmxLogAppender - no default server found.";
    }

    @Test
    public void testSetMBeanServer() {
        JmxLogAppender l = new JmxLogAppender();
        l.setMBeanServerValue(platformServer);
        l.activateOptions();
        assert l.getMBeanServerValue().equals(platformServer) : "JmxLogAppender - MBeanServer setter failing.";
        l.setMBeanServer("platform");
        assert l.getMBeanServerValue().equals(platformServer) : "Not setting platform server by name";
    }

    @Test
    public void testSetObjectName(){
        JmxLogAppender l = new JmxLogAppender();
        l.setObjectName(objectName.toString());
        l.activateOptions();
        assert l.getObjectNameValue().equals(objectName) : "JmxLogAppender - ObjectName setter fails.";
    }

    @Test
    public void testSetFilterExpression() {
        JmxLogAppender l = new JmxLogAppender();
        l.setFilterExpression("1 == 1");
        l.activateOptions();
        assert l.getFilterExpression().equals( "1 == 1" ) : "JmxLogAppender - filter expression not set.";
    }

    @Test
    public void testServerSelection() {
        JmxLogAppender l = new JmxLogAppender();
        l.setMBeanServer("platform");
        l.activateOptions();
        assert l.getMBeanServerValue().equals(platformServer) : "JmxLogAppender - ServerSelection sertter tails";
    }

    @Test
    public void testLog() throws Exception{
        Logger logger = Logger.getLogger(JmxLogAppenderTest.class);
        DOMConfigurator.configure("log4j.xml");
        platformServer.addNotificationListener(objectName, lstnr, null,null);
        logger.info("Hello World");

        int count = 0;
        while(count < 10 && lstnr.getNoteCount() <= 0){
            try {
                Thread.currentThread().sleep(500); // stall thread
                count++;
                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        System.out.println ("NoteCount: " + lstnr.getNoteCount());

        assert lstnr.getNoteCount() > 0 : "JmxLoggingAppender ! broadcasting log event";

        logger.info("This is a test for log4j.");
        count = 0;
        while(count < 10 && lstnr.getNoteCount() <= 0){
            try {
                Thread.currentThread().sleep(500); // stall thread
                count++;
                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        assert lstnr.getNoteCount() > 0 : "JmxLoggingAppender ! broadcasting log event";
    }


    @Test
    public void testFilterCreation() {
        JmxLogAppender appender = new JmxLogAppender();
        appender.setFilterExpression("expression");
        appender.activateOptions();
        assert appender.getFilterExpression().equals("expression");
    }

    @Test
    public void testFilterScript ()  {
        JmxLogAppender appender = new JmxLogAppender();
        appender.setFilterScriptFile("test-script.mvl");
        appender.activateOptions();
        System.out.println ("File: " + appender.getFilterScriptFile());
        assert appender.getFilterScriptFile().equals(new File("test-script.mvl").getAbsolutePath());
    }

    @Test
    public void testFilterExpIntegration() throws Exception{
        Logger logger = Logger.getLogger(JmxLogAppenderTest.class);
        DOMConfigurator.configure("log4j.xml");
        platformServer.addNotificationListener(objectName, lstnr, null,null);

        logger.info("Hello World!");
        logger.info("This is a test for log4j.");
        logger.info("Do not attempt to change the channel.");


        // stall thread
        int count = 0;
        long logCount = (Long)platformServer.getAttribute(objectName, "LogCount");
        while(count < 10 && logCount == 0){
            try {
                Thread.currentThread().sleep(500); // stall thread
                logCount = (Long)platformServer.getAttribute(objectName, "LogCount");
                count++;
                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(count > 9 && logCount == 0){
            throw new IllegalStateException ("Unable to get Notification count within alloted time");
        }
        assert logCount > 0;
    }
}