/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmxlogger.tools.JmxConfigStore;
import jmxlogger.tools.JmxConfigStore.ConfigEvent;
import jmxlogger.tools.JmxConfigStore.ConfigEventListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vvivien
 */
public class JmxConfigStoreTest {

    public JmxConfigStoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testConstructors() {
        JmxConfigStore cfg = new JmxConfigStore();
        assert cfg.getValues().size() == 0;

        HashMap vals = new HashMap();
        vals.put("a", "b");
        cfg = new JmxConfigStore(vals);
        assert cfg.getValue("a").equals("b") : "Constructor fails to pass in values";
    }

    @Test
    public void testAddEventListener() {
        JmxConfigStore cfg = new JmxConfigStore();
        cfg.addListener(new ConfigEventListener() {
            public void onValueChanged(ConfigEvent event) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        assert cfg.getListeners().size() == 1: "Not adding event listeners";

        cfg.addListener(new ConfigEventListener() {
            public void onValueChanged(ConfigEvent event) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        assert cfg.getListeners().size() == 2: "Not increasing event listeners";
    }

    @Test public void testRemoveListener() {
        JmxConfigStore cfg = new JmxConfigStore();
        ConfigEventListener l = new ConfigEventListener() {
            public void onValueChanged(ConfigEvent event) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        cfg.addListener(l);
        assert cfg.getListeners().size() > 0: "Not adding event listeners";
        cfg.removeListener(l);
        assert cfg.getListeners().size() == 0 : "Not removing event listener";
    }

    @Test public void testConfigValues() {
        JmxConfigStore cfg = new JmxConfigStore();
        cfg.putValue("a", "b");
        assert cfg.getValues().size() == 1 : "not setting config value";
        assert cfg.getValue("a").equals("b") : "Unable to retrieve set value";
    }

    @Test public void testConfigEventClass () {
        ConfigEvent e = new ConfigEvent(this, "a", "b");
        assert e.getSource() == this : "ConfigEvent not setting source";
        assert e.getKey().equals("a") : "ConfigEvent not setting key";
        assert e.getValue().equals("b") : "ConfigEvent not setting value";

        try{
            e = new ConfigEvent(null, "a", "b");
            fail("Null source should cause ConfigEvent to fail");
        }catch(Exception ex){}
    }

    @Test public void testPostConfigEvent() {
        JmxConfigStore cfg = new JmxConfigStore();
        final AtomicInteger counter = new AtomicInteger(0);
        cfg.addListener(new ConfigEventListener() {
            public void onValueChanged(ConfigEvent event) {
                counter.getAndIncrement();
            }
        });

        ConfigEvent e = new ConfigEvent(this, "a", "b");
        cfg.postEvent(e);

        // lets stall to give thread time to settle
        int delay = 0;
        while(delay < 10 && counter.get() <= 0){
            try {
                Thread.currentThread().sleep(500);
                System.out.println ("Waiting for notification ... " + delay * 500 + " millis.");
                delay++;
            } catch (InterruptedException ex) {
                Logger.getLogger(JmxConfigStoreTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        assert counter.get() == 1 : "Listeners are not firing on post.";
    }
}