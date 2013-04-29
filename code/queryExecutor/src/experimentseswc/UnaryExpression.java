package experimentseswc;
import java.util.ArrayList;
import java.util.HashMap;
public class UnaryExpression extends Expression {

    String value;

    public UnaryExpression (String v) {
        this.value = v;
    }

    public void replace (String var, String value) {

        if (this.value.equals(var)) {
            this.value = value;
        }        
    }

    public void replaceAll (String var, String value) {

        if (BasicGraphPattern.isVar(this.value) && this.value.substring(1).equals(var)) {
            this.value = value;
        }
    }

    public Expression copy() {

        return new UnaryExpression(this.value);
    }

    public HashMap<String, String> getConstantsMapping() {

        HashMap<String, String> hm = new HashMap<String, String>();
        if (!BasicGraphPattern.isVar(this.value)) {
            hm.put(Triple.transform(this.value), this.value);
        }
        return hm;
    }

    public ArrayList<String> getExistentialVars(ArrayList<String> vs) {

        ArrayList<String> evs = new ArrayList<String>();
        if (BasicGraphPattern.isExistentialVar(this.value, vs) && !vs.contains(this.value)) {
            evs.add(this.value);
        }
        return evs;
    }

    public ArrayList<String> getVars(ArrayList<String> vs) {

        ArrayList<String> evs = new ArrayList<String>();
        if (BasicGraphPattern.isVar(this.value) && !vs.contains(this.value)) {
            evs.add(this.value);
        }
        return evs;
    }

    public String toString() {
        return this.value;
    }
}
