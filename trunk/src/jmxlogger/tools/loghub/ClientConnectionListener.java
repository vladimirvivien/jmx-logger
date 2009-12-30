/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools.loghub;

/**
 *
 * @author vvivien
 */
public interface ClientConnectionListener {
    public void onConnectionOpened();
    public void onConnectionClosed();
    public void onConnectionFailed();
}
