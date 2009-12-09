/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmxlogger.tools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

/**
 *
 * @author vvivien
 */
public class JmxScriptedLogFilter implements JmxLogFilter {

    private Serializable expression;
    private ParserContext context = new ParserContext();
    private File scriptFile;

    public JmxScriptedLogFilter() {
        context.addPackageImport("java.util");
    }

    public void setFilterExpression(String exp) {
        expression = MVEL.compileExpression(exp, context);
    }

    public void setScriptFile(File f){
        if(!ToolBox.isFileValid(f)){
            throw new IllegalStateException(String.format("File [%s] is not a valid file.", f ));
        }
        scriptFile = f;
    }

    public boolean isLogAllowed(JmxEventWrapper eventWrapper) {

        Map<String, Object> event = eventWrapper.unwrap();
        Object result = false;
        if(scriptFile != null){
            try {
                result = MVEL.evalFile(scriptFile, event);
            } catch (IOException ex) {
                throw new IllegalStateException("JmxScriptedLogFilter - unable to access script file: " + ex.getMessage());
            }
        }else{
            if(expression == null){
                result = true;
            }else{
                result = MVEL.executeExpression(expression, event);
            }
        }
        if (!(result instanceof Boolean)) {
            throw new IllegalStateException("Filter expession must evaluate to a Boolean result.");
        }
        return (Boolean) result;
    }

    @Override
    public String toString() {
        return (String)expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JmxScriptedLogFilter)) {
            return false;
        }
        final JmxScriptedLogFilter other = (JmxScriptedLogFilter) obj;
        if (!this.expression.equals(other.expression)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.expression != null ? this.expression.hashCode() : 0);
        return hash;
    }
}
