/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 *
 * @author vvivien
 */
public interface JmxLogConfigurer {
    public void setLogLevel(String level);
    public String getLogLevel();
    public void setObjectName(ObjectName objName);
    public ObjectName getObjectName();
    public void setMBeanServer(MBeanServer svr);
    public MBeanServer getMBeanServer();
    public void setFilterExpression(String exp);
    public String getFilterExpression();
    public void setFilterScript(String fileName);
    public String getFilterScript();
}
