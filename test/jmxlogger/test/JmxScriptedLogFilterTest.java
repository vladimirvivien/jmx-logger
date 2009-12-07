/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.util.HashMap;
import java.util.Map;
import jmxlogger.tools.JmxConfigStore;
import jmxlogger.tools.JmxEventWrapper;
import jmxlogger.tools.JmxScriptedLogFilter;
import jmxlogger.tools.ToolBox;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author vvivien
 */
public class JmxScriptedLogFilterTest {

    public JmxScriptedLogFilterTest() {
    }

    private JmxScriptedLogFilter filter;
    private JmxConfigStore config;
    private Map<String,Object> event;
    private String debugMsg = "2009-07-29 14:18:00,516 DEBUG [jmxlogger.JmxLogger] This is a debug message";
    private String infoMsg  = "2009-07-30 16:02:00,500  INFO [jmxlogger.JmxTester] This is an info message";

    @Before
    public void setUpClass() throws Exception {
        filter = new JmxScriptedLogFilter();
        config = new JmxConfigStore();
        event = new HashMap<String,Object>();
    }

    @After
    public void tearDownClass() throws Exception {
    }

    @Test
    public void testIsLogAllowed() {
        event.put(ToolBox.KEY_EVENT_LEVEL, "INFO");
        JmxEventWrapper eventwrap = new JmxEventWrapper(event);
        // if test is absent, return true
         assert filter.isLogAllowed(eventwrap);
         filter.setFilterExpression("logLevel == 'INFO'");
         assert filter.isLogAllowed(eventwrap);
    }

    @Test
    public void testFormattedMessage() {
        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, debugMsg);
        JmxEventWrapper eventwrap = new JmxEventWrapper(event);
        filter.setFilterExpression("formattedMessage contains 'DEBUG'");
        assert filter.isLogAllowed(eventwrap);

        filter.setFilterExpression("formattedMessage contains 'INFO'");
        assert ! filter.isLogAllowed(eventwrap);
    }

    @Test
    public void testRawMessage() {
        event.put(ToolBox.KEY_EVENT_RAW_MESSAGE, "ERROR, STOP EVERYTHING");
        JmxEventWrapper eventwrap = new JmxEventWrapper(event);
        filter.setFilterExpression("rawMessage.length() == 22");
        assert filter.isLogAllowed(eventwrap);
    }

    @Test
    public void testSourceClass() {
        event.put(ToolBox.KEY_EVENT_SOURCE_CLASS, this.getClass().getName());
        JmxEventWrapper warpper = new JmxEventWrapper(event);
        filter.setFilterExpression(String.format("sourceClassName == '%s'", this.getClass().getName()));
        assert filter.isLogAllowed(warpper);
        filter.setFilterExpression("sourceClassName == 'jxmlogger.tools.JmxLogFilter'");
        assert !filter.isLogAllowed(warpper);
    }

//    @Test
//    public void testSourceMethodAllowed() {
//        event.put(ToolBox.KEY_EVENT_SOURCE_METHOD, "isLogAllowed");
//        JmxEventWrapper warpper = new JmxEventWrapper(event);
//        config.setSourceMethod("isLogAllowed");
//        assert filter.isLogAllowed(warpper);
//        config.setSourceMethod("testSourceMethodAllowed");
//        assert !filter.isLogAllowed(warpper);
//    }
//
//    @Test
//    public void testThrownClassAllowed() {
//        event.put(ToolBox.KEY_EVENT_THROWABLE, "java.lang.RuntimeException");
//        JmxEventWrapper warpper = new JmxEventWrapper(event);
//        config.setThrownClass("java.lang.RuntimeException");
//        assert filter.isLogAllowed(warpper);
//        config.setThrownClass("java.lang.String");
//        assert !filter.isLogAllowed(warpper);
//    }
//
//@Test
//    public void testTimestampLoBound() {
//        long today = new Date().getTime();
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(today);
//        cal.add(Calendar.DAY_OF_MONTH, 1);
//        long tomorrow = cal.getTimeInMillis();
//        event.put(ToolBox.KEY_EVENT_TIME_STAMP, tomorrow);
//        JmxEventWrapper warpper = new JmxEventWrapper(event);
//        config.setTimestampLo(today);
//        assert filter.isLogAllowed(warpper);
//
//        event.put(ToolBox.KEY_EVENT_TIME_STAMP, today);
//        config.setTimestampLo(tomorrow);
//        assert !filter.isLogAllowed(warpper);
//    }
//
//    public void testTimestampHiBound() {
//        long today = new Date().getTime();
//        Calendar cal = Calendar.getInstance();
//        cal.setTimeInMillis(today);
//        cal.add(Calendar.DAY_OF_MONTH, 1);
//        long tomorrow = cal.getTimeInMillis();
//        event.put(ToolBox.KEY_EVENT_TIME_STAMP, today);
//        JmxEventWrapper warpper = new JmxEventWrapper(event);
//        config.setTimestampHi(tomorrow);
//        assert filter.isLogAllowed(warpper);
//
//        event.put(ToolBox.KEY_EVENT_TIME_STAMP, tomorrow);
//        config.setTimestampLo(today);
//        assert !filter.isLogAllowed(warpper);
//    }
}