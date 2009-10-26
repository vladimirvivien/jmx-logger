/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jmxlogger.integration.log4j.JmxLogAppender;
import jmxlogger.integration.log4j.DefaultLog4jFilter;
import jmxlogger.tools.ToolBox;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
        platformServer = ManagementFactory.getPlatformMBeanServer();
        objectName = ToolBox.buildObjectName("log4j.logging:type=Log4jAppender");
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
        assert l.getObjectName().equals(objectName.toString()) : "JmxLogAppender - constructor not seting object name.";
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
        l.setObjectName(objectName.toString());
        assert l.getObjectName().equals(objectName.toString()) : "JmxLogAppender - ObjectName setter fails.";
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

    @Test
    public void testLog() throws Exception{
        Logger logger = Logger.getLogger(JmxLogAppenderTest.class);
        DOMConfigurator.configure("log4j.xml");
        platformServer.addNotificationListener(objectName, lstnr, null,null);
        logger.info("Hello!");

        int count = 0;
        while(count < 10 && lstnr.getNoteCount() <= 0){
            try {
                Thread.currentThread().sleep(500);
                count++;
                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        assert lstnr.getNoteCount() > 0 : "JmxLoggingHandler ! broadcasting log event";
    }

    @Test
    public void testSetLogLevel() {
        JmxLogAppender l = new JmxLogAppender();
        l.setLogLevel("INFO");
        assert l.getLogLevel().equals("INFO");

        l.setLogLevel("WARN");
        assert l.getLogLevel().equals("WARN");

        l.setLogLevel("ERROR");
        assert l.getLogLevel().equals("ERROR");
    }

    @Test
    public void testFilterCreation() {
        Logger logger = LogManager.getLogger(JmxLogAppenderTest.class.getName());
        JmxLogAppender appender = new JmxLogAppender();
        DefaultLog4jFilter filter = new DefaultLog4jFilter();
        appender.addFilter(filter);
        filter.setLogPattern("(.)(.)*Exception(.)(.)*");

        Filter f = appender.getFilter();
        DefaultLog4jFilter lf = null;
        while (f != null){
            if(f instanceof DefaultLog4jFilter){
                lf = (DefaultLog4jFilter) f;
                break;
            }
            f = f.getNext();
        }

        assert lf.getLogPattern().equals("(.)(.)*Exception(.)(.)*");
    }
    
}