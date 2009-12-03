/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

/**
 *
 * @author vladimir
 */
public interface JmxLogFilter {
    boolean isLogAllowed(JmxEventWrapper eventWrapper);
}
