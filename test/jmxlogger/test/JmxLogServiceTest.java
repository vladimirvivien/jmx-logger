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
import jmxlogger.tools.JmxConfigStore;
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
    private JmxConfigStore configStore;

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
        configStore = new JmxConfigStore();
        configStore.putValue(ToolBox.KEY_CONFIG_JMX_SERVER, server);
        configStore.putValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME, objName);
    }

    @After
    public void tearDown() {
    }

    //@Test
    public void testCreateInstance() {
        JmxLogService l1 = JmxLogService.createInstance();
        assert l1 != null : "JmxLogService.createInstance() is returning null";
        assert l1 != JmxLogService.createInstance() : "JmxLogService.createInstance() is not initializing new instance.";
    }

    //@Test
    public void testDefaultConfigurationStore() {

        JmxLogService l = JmxLogService.createInstance();
        assert l.getDefaultConfigurationStore().getValue(ToolBox.KEY_CONFIG_JMX_SERVER) == null : "JmxLogService - default mbean server value is bogus";
        assert l.getDefaultConfigurationStore().getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME) == null : "JmxLogService - default obj name is bogus";

        l = JmxLogService.createInstance(configStore);
        assert l.getDefaultConfigurationStore().equals(configStore) : "JmxLogService - Factory not setting configStore";
        assert l.getDefaultConfigurationStore().getValue(ToolBox.KEY_CONFIG_JMX_SERVER)
                .equals(configStore.getValue(ToolBox.KEY_CONFIG_JMX_SERVER)) : "JmxLogService - default mbean server value is bogus";
        assert l.getDefaultConfigurationStore().getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME)
                .equals(configStore.getValue(ToolBox.KEY_CONFIG_JMX_OBJECTNAME)): "JmxLogService - default obj name is bogus";
    }



    //@Test
    public void testStart() throws Exception{
        JmxLogService l = JmxLogService.createInstance(configStore);
        l.start();
        assert l.isStarted() : "JmxLogService not starting";
        assert java.lang.management.ManagementFactory.getPlatformMBeanServer().isRegistered(objName)
                : "JmxLogService start() is not registering internal MBean object";
    }

    //@Test
    public void testStop() throws Exception{
        JmxLogService l = JmxLogService.createInstance(configStore);
        l.start();
        assert l.isStarted() : "JmxLogService not starting";
        l.stop();
        assert !java.lang.management.ManagementFactory.getPlatformMBeanServer().isRegistered(objName)
                : "JmxLogService stop() is not unregistering internal MBean object";
    }

    //@Test
    public void testLog() throws Exception{
        JmxLogService l = JmxLogService.createInstance(configStore);
        LogListener lstnr = new LogListener();
        l.start();
        server.addNotificationListener(objName, lstnr, null, null);
        Map<String,Object> event = new HashMap<String,Object>();
        event.put(ToolBox.KEY_EVENT_LEVEL, "DEBUG");
        event.put(ToolBox.KEY_EVENT_SOURCE, l.getClass().getName());
        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, "Hello, this is a logged message.");

        l.log(event);

        // lets stall to give thread time to settle
        int count = 0;
        while(count < 10 && lstnr.getNoteCount() <= 0){
            try {
                Thread.currentThread().sleep(500);
                count++;
                System.out.println ("Waiting for notification ... " + count * 500 + " millis.");
            } catch (InterruptedException ex) {
                Logger.getLogger(JmxLogServiceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        assert lstnr.getNoteCount() > 0;
    }

    @Test
    public void testLogStatistics() throws Exception{
        JmxLogService l = JmxLogService.createInstance(configStore);
        l.start();
        // log debug
        Map<String,Object> debug = new HashMap<String,Object>();
        debug.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(2));
        debug.put(ToolBox.KEY_EVENT_LEVEL, "DEBUG");
        debug.put(ToolBox.KEY_EVENT_SOURCE, l.getClass().getName());
        debug.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, "Debug Message.");
        l.log(debug);

        Map<String,Object> info = new HashMap<String,Object>();
        info.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(0));
        info.put(ToolBox.KEY_EVENT_LEVEL, "INFO");
        info.put(ToolBox.KEY_EVENT_SOURCE, l.getClass().getName());
        info.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, "Info message.");        
        l.log(info);
        
        info = new HashMap<String,Object>();
        info.put(ToolBox.KEY_EVENT_TIME_STAMP, new Long(1));
        info.put(ToolBox.KEY_EVENT_LEVEL, "INFO");
        info.put(ToolBox.KEY_EVENT_SOURCE, l.getClass().getName());
        info.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, "Another info message.");        
        l.log(info);

        // stall for time to ensure thread settled.
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(JmxLogServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        Map<String,Long> stat = ((Map<String,Long>)info.get(ToolBox.KEY_EVENT_LOG_STAT));

        long count = stat.get("DEBUG");
        assert count == 1 : "Statistics object not tracing logs";
        count = stat.get("INFO");
        assert count == 2 : "Statistic object not tracing logs";

        count = stat.get(ToolBox.KEY_EVENT_LOG_COUNT_ATTEMPTED);
        assert count == 3;
        count = stat.get(ToolBox.KEY_EVENT_LOG_COUNTED);
        System.out.println ("log counted " + count);
        assert count == 3;
        assert stat.get(ToolBox.KEY_EVENT_START_TIME) != null : "Start Time statistics not registering";

    }
}