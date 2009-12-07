/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author vvivien
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmxlogger.test.JmxConfigStoreTest.class,
    jmxlogger.test.JmxLogServiceTest.class,
    jmxlogger.test.JmxScriptedLogFilterTest.class,
    jmxlogger.test.JmxLogHandlerTest.class,
    jmxlogger.test.JmxLogAppenderTest.class,
    jmxlogger.test.JmxLogEmitterTest.class
})

public class JmxLoggerTestSuite {
    //
}