/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import jmxlogger.tools.JmxEventWrapper;
import jmxlogger.tools.JmxLogFilter;
import jmxlogger.tools.JmxLogConfig;
import jmxlogger.tools.ToolBox;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vvivien
 */
public class JmxLogFilterTest {

    public JmxLogFilterTest() {
    }

    private JmxLogFilter filter;
    private JmxLogConfig config;
    private Map<String,Object> event;
    private String debugMsg = "2009-07-29 14:18:00,516 DEBUG [jmxlogger.JmxLogger] This is a debug message";
    private String infoMsg  = "2009-07-30 16:02:00,500  INFO [jmxlogger.JmxTester] This is an info message";
    @Before
    public void setUpClass() throws Exception {
        filter = new JmxLogFilter();
        config = new JmxLogConfig();
        filter.setLogFilterConfig(config);
        event = new HashMap<String,Object>();
    }

    @After
    public void tearDownClass() throws Exception {
    }

    @Test
    public void testFormattedLogPattern() {
        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, debugMsg);
        JmxEventWrapper eventwrap = new JmxEventWrapper(event);
        String pattern = "\\d{4}-\\d{2}-\\d{2}\\s\\d\\d:\\d\\d:\\d\\d,\\d{3}\\sDEBUG";
        config.setLogPattern(Pattern.compile(pattern));
        assert filter.isLogAllowed(eventwrap);
        pattern = "\\d{4}-\\d{2}-\\d{2}\\s\\d\\d:\\d\\d:\\d\\d,\\d{3}\\sINFO";
        config.setLogPattern(Pattern.compile(pattern));
        assert !filter.isLogAllowed(eventwrap);
    }

    @Test
    public void testRawLogPattern() {
        event.put(ToolBox.KEY_EVENT_RAW_MESSAGE, "Error has been encountered.");
        JmxEventWrapper eventwrap = new JmxEventWrapper(event);
        String pattern = "\\d{4}-\\d{2}-\\d{2}\\s\\d\\d:\\d\\d:\\d\\d,\\d{3}\\sDEBUG";
        config.setLogPattern(Pattern.compile(pattern));
        assert !filter.isLogAllowed(eventwrap);
        pattern = "Error*";
        config.setLogPattern(Pattern.compile(pattern));
        assert filter.isLogAllowed(eventwrap);
    }

    @Test
    public void testSourceClassAllowed() {
        event.put(ToolBox.KEY_EVENT_SOURCE_CLASS, this.getClass().getName());
        JmxEventWrapper warpper = new JmxEventWrapper(event);
        config.setSourceClass(this.getClass().getName());
        assert filter.isLogAllowed(warpper);
        config.setSourceClass("jxmlogger.tools.JmxLogFilter");
        assert !filter.isLogAllowed(warpper);
    }

    @Test
    public void testSourceMethodAllowed() {
        event.put(ToolBox.KEY_EVENT_SOURCE_METHOD, "isLogAllowed");
        JmxEventWrapper warpper = new JmxEventWrapper(event);
        config.setSourceMethod("isLogAllowed");
        assert filter.isLogAllowed(warpper);
        config.setSourceMethod("testSourceMethodAllowed");
        assert !filter.isLogAllowed(warpper);
    }

    @Test
    public void testThrownClassAllowed() {
        event.put(ToolBox.KEY_EVENT_THROWABLE, "java.lang.RuntimeException");
        JmxEventWrapper warpper = new JmxEventWrapper(event);
        config.setThrownClass("java.lang.RuntimeException");
        assert filter.isLogAllowed(warpper);
        config.setThrownClass("java.lang.String");
        assert !filter.isLogAllowed(warpper);
    }

@Test
    public void testTimestampLoBound() {
        long today = new Date().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(today);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrow = cal.getTimeInMillis();
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, tomorrow);
        JmxEventWrapper warpper = new JmxEventWrapper(event);
        config.setTimestampLo(today);
        assert filter.isLogAllowed(warpper);
        
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, today);
        config.setTimestampLo(tomorrow);
        assert !filter.isLogAllowed(warpper);
    }

    public void testTimestampHiBound() {
        long today = new Date().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(today);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrow = cal.getTimeInMillis();
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, today);
        JmxEventWrapper warpper = new JmxEventWrapper(event);
        config.setTimestampHi(tomorrow);
        assert filter.isLogAllowed(warpper);

        event.put(ToolBox.KEY_EVENT_TIME_STAMP, tomorrow);
        config.setTimestampLo(today);
        assert !filter.isLogAllowed(warpper);
    }
}