package experimentseswc;
import java.util.ArrayList;
import java.util.HashMap;
public class Filter extends BasicGraphPattern {

    Expression expr;

    public Filter (Expression e) {
        this.expr = e;
    }

    public BasicGraphPattern copy() {
        return new Filter(this.expr.copy());
    }

    public void replace (String var, String value) {

        if (!isVar(var)) {
            return;
        }
        this.expr.replace(var, value);
    }

    public void replaceAll (String var, String value) {

        this.expr.replaceAll(var, value);
    }

    public String toPredicate() {

        return this.expr.toString();
    }

    public HashMap<String, String> getConstantsMapping() {
        return this.expr.getConstantsMapping();
    }

    public ArrayList<String> getExistentialVars(ArrayList<String> vs) {

        return this.expr.getExistentialVars(vs);
    }

    public ArrayList<String> getVars(ArrayList<String> vs) {

        return this.expr.getVars(vs);
    }

    public String toString() {

        return "FILTER (" + this.expr +")";
    }

}
