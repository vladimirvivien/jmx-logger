/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmxlogger.tools;

import java.io.Serializable;
import java.util.Map;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

/**
 *
 * @author vvivien
 */
public class JmxScriptedLogFilter implements JmxLogFilter {

    private Serializable expression;
    private ParserContext context = new ParserContext();

    public JmxScriptedLogFilter() {
        context.addPackageImport("java.util");
    }

    public void setFilterExpression(String exp) {
        expression = MVEL.compileExpression(exp, context);
    }

    public String getFilterExpression(){
        return (String)expression;
    }

    public boolean isLogAllowed(JmxEventWrapper eventWrapper) {
        if (expression == null) {
            return true;
        }
        Map<String, Object> event = eventWrapper.unwrap();
        Object result = MVEL.executeExpression(expression, event);
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
