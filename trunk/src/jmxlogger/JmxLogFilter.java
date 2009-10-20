/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger;

import java.util.logging.Filter;
import jmxlogger.tools.JmxEventWrapper;

/**
 *
 * @author vvivien
 */
public interface JmxLogFilter {
    public boolean isAllowed(JmxEventWrapper wrapper);
}
