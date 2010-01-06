/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
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
    private Map<String,Object> event;
    private String debugMsg = "2009-07-29 14:18:00,516 DEBUG [jmxlogger.JmxLogger] This is a debug message";
    private String infoMsg  = "2009-07-30 16:02:00,500  INFO [jmxlogger.JmxTester] This is an info message";

    @Before
    public void setUpClass() throws Exception {
        filter = new JmxScriptedLogFilter();
        event = new HashMap<String,Object>();
    }

    @After
    public void tearDownClass() throws Exception {
    }

    @Test
    public void testIsLogAllowedWithExpression() {
        event.put(ToolBox.KEY_EVENT_LEVEL, "INFO");
        // if test is absent, return true
         assert filter.isLogAllowed(event);
         filter.setFilterExpression("logLevel == 'INFO'");
         assert filter.isLogAllowed(event);
    }

    @Test
    public void testIsLogAllowedWithBlankExpression() {
        event.put(ToolBox.KEY_EVENT_LEVEL, "INFO");
        // if test is absent, return true
         assert filter.isLogAllowed(event);
         filter.setFilterExpression("");
         assert filter.isLogAllowed(event);
    }

    @Test
    public void testIsLogAllowedWithScriptFile() {
        event.put(ToolBox.KEY_EVENT_LEVEL, "INFO");
        try{
            filter.setScriptFile(new File("bad-file.mvl"));
            throw new IllegalStateException("Test should have filed upon missing file.");
        }catch(Exception ex){}

        filter.setScriptFile(new File ("test-script.mvl"));
        assert filter.isLogAllowed(event);
    }

    @Test
    public void testFormattedMessage() {
        event.put(ToolBox.KEY_EVENT_FORMATTED_MESSAGE, debugMsg);
        filter.setFilterExpression("formattedMessage contains 'DEBUG'");
        assert filter.isLogAllowed(event);

        filter.setFilterExpression("formattedMessage contains 'INFO'");
        assert ! filter.isLogAllowed(event);
    }

    @Test
    public void testRawMessage() {
        event.put(ToolBox.KEY_EVENT_RAW_MESSAGE, "ERROR, STOP EVERYTHING");

        filter.setFilterExpression("rawMessage.length() == 22");
        assert filter.isLogAllowed(event);
    }

    @Test
    public void testSourceClass() {
        event.put(ToolBox.KEY_EVENT_SOURCE_CLASS, this.getClass().getName());

        filter.setFilterExpression(String.format("sourceClassName == '%s'", this.getClass().getName()));
        assert filter.isLogAllowed(event);
        filter.setFilterExpression("sourceClassName == 'jxmlogger.tools.JmxLogFilter'");
        assert !filter.isLogAllowed(event);
    }

    @Test
    public void testSourceMethod() {
        event.put(ToolBox.KEY_EVENT_SOURCE_METHOD, "isLogAllowed");

        filter.setFilterExpression("sourceMethodName == 'isLogAllowed'");
        assert filter.isLogAllowed(event);
        filter.setFilterExpression("sourceMethodName == 'testSourceMethodName'");
        assert !filter.isLogAllowed(event);
    }

    @Test
    public void testThrownClassAllowed() {
        event.put(ToolBox.KEY_EVENT_THROWABLE, "java.lang.RuntimeException");
        JmxEventWrapper warpper = new JmxEventWrapper(event);
        filter.setFilterExpression("exceptionName == 'java.lang.RuntimeException'");
        assert filter.isLogAllowed(event);

        filter.setFilterExpression("exceptionName.equals('java.lang.Exception')");
        assert !filter.isLogAllowed(event);
    }


    @Test
    public void testTimeStamp() {
        long today = new Date().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(today);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long tomorrow = cal.getTimeInMillis();
        event.put(ToolBox.KEY_EVENT_TIME_STAMP, tomorrow);

        filter.setFilterExpression("timestamp > new Date().getTime()");
        assert filter.isLogAllowed(event);
        filter.setFilterExpression("timestamp < new Date().getTime()");
        assert !filter.isLogAllowed(event);
    }

    @Test
    public void testStatistics() {
        Map<String, Long> stats = new HashMap<String,Long>();
        stats.put(ToolBox.KEY_EVENT_START_TIME, new Long(new Date().getTime()));
        stats.put(ToolBox.KEY_EVENT_LOG_COUNTED, new Long(12345678));
        stats.put(ToolBox.KEY_EVENT_LOG_COUNT_ATTEMPTED, new Long(87654321));
        stats.put("INFO", new Long(12345));
        stats.put("DEBUG", new Long(1234567));
        event.put(ToolBox.KEY_EVENT_LOG_STAT, stats);

        filter.setFilterExpression("logStats['startTime'] < (new Date().getTime() + 10000)");
        assert filter.isLogAllowed(event) : "Unable to evaluate stats from event map";

        filter.setFilterExpression("logStats.totalLogCounted == new Long(12345678)");
        assert filter.isLogAllowed(event) : "Unable to evaluate stats from event map";

        filter.setFilterExpression("logStats.totalLogCounted < logStats.totalLogAttempted");
        assert filter.isLogAllowed(event) : "Unable to evaluate stats from event map";

        filter.setFilterExpression("logStats.INFO == new Long(12345)");
        assert filter.isLogAllowed(event) : "Unable to evaluate stats from event map";

        filter.setFilterExpression("logStats.DEBUG > logStats.INFO");
        assert filter.isLogAllowed(event) : "Unable to evaluate stats from event map";
    }
}