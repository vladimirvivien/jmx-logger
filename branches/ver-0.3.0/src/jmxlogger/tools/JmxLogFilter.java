/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import java.util.Map;

/**
 *
 * @author vladimir
 */
public interface JmxLogFilter {
    boolean isLogAllowed(Map <String,Object> event);
}
