/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import jmxlogger.tools.JmxLogEmitterMBean;
import jmxlogger.tools.ToolBox;
import jmxlogger.tools.loghub.ClientService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vladimir
 */
public class ClientServiceTest {
    static private LogAgent logAgent;
    public ClientServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        logAgent = new LogAgent(ToolBox.buildObjectName(LogAgent.LOGGER_NAME), 3000);
        logAgent.start();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        logAgent.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConnect() throws Exception {
        ClientService client = new ClientService();
        String id = client.connect("localhost:3000", "test", "test");
        client.disconnect();
        assert id != null : "Connection id is null.";
        try{
            id = client.connect("localhost:3000", "test", "tset");
            fail("Connection with bad credentials should not work.");
        }catch(Exception ex){}
    }

    @Test
    public void testDisconnect() {
        ClientService client = new ClientService();
        String id = client.connect("localhost:3000", "test", "test");
        client.disconnect();
    }

    @Test
    public void testGetLogEmitter() {
        ClientService client = new ClientService();
        String id = client.connect("localhost:3000", "test", "test");
        JmxLogEmitterMBean emitter = client.getLogEmitter(ToolBox.buildObjectName(LogAgent.LOGGER_NAME));
        assert emitter != null : "Unable to create JmxLogEmitterMBean proxy";
        client.disconnect();
    }

    @Test
    public void testAddLogEmitterListener() {
        ClientService client = new ClientService();
        String id = client.connect("localhost:3000", "test", "test");
        final AtomicInteger logCounter = new AtomicInteger();
        client.addListenerToLogEmitter(ToolBox.buildObjectName(LogAgent.LOGGER_NAME), new NotificationListener(){
            public void handleNotification(Notification notification, Object handback) {
                logCounter.getAndIncrement();
            }
        });
        try {
            // stall a bit
            System.out.println ("Stalling, to wait for log event");
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        assert logCounter.get() > 0 : "LogListener not being called during log events.";
        client.disconnect();

    }
}