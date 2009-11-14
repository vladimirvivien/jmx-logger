/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmxlogger.tools;

import java.io.Serializable;
import java.util.Map;
import org.mvel2.MVEL;
/**
 *
 * @author vvivien
 */
public class JmxScriptedLogFilter implements JmxLogFilter {
    private Serializable expression;
    public void setFilterExpression(String exp){
        expression = MVEL.compileExpression(exp);
    }

    public boolean isLogAllowed(JmxEventWrapper eventWrapper){
       if(expression == null) return true;
       Map<String,Object> event = eventWrapper.unwrap();
       Object result = MVEL.executeExpression(expression,event);
        if(!(result instanceof Boolean)){
            throw new IllegalStateException("Filter expession must evaluate to a Boolean result.");
        }
        return (Boolean)result;
    }
}
