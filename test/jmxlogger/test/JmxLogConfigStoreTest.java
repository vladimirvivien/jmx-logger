/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.util.HashMap;
import jmxlogger.tools.JmxLogConfigStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vvivien
 */
public class JmxLogConfigStoreTest {

    public JmxLogConfigStoreTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testConstructors() {
        JmxLogConfigStore cfg = new JmxLogConfigStore();

        HashMap vals = new HashMap();
        vals.put("a", "b");
        cfg = new JmxLogConfigStore(vals);
        assert cfg.getValue("a").equals("b") : "Constructor fails to pass in values";
    }


}