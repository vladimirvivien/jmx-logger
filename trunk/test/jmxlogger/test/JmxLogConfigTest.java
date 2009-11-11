/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.util.HashMap;
import jmxlogger.tools.JmxLogConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vvivien
 */
public class JmxLogConfigTest {

    public JmxLogConfigTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testConstructors() {
        JmxLogConfig cfg = new JmxLogConfig();
        assert cfg.getValues() == null : "Default constructor failed.";

        HashMap vals = new HashMap();
        vals.put("a", "b");
        cfg = new JmxLogConfig(vals);
        assert cfg.getValue("a").equals("b") : "Constructor fails to pass in values";
        assert cfg.getValues().size() == 1 : "Constructor fails to pass in values";
    }


}