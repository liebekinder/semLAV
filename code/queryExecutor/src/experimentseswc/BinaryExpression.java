package experimentseswc;
import java.util.ArrayList;
import java.util.HashMap;
class BinaryExpression extends Expression {
    Expression left;
    Expression right;
    String op;

    public BinaryExpression (String op, Expression e0, Expression e1) {
        this.op = op;
        this.left = e0;
        this.right = e1;
    }

    public Expression copy() {
        return new BinaryExpression(this.op, this.left.copy(), this.right.copy());
    }

    public void replace (String var, String value) {

        if (this.left != null) {
            this.left.replace(var, value);
        }
        if (this.right != null) {
            this.right.replace(var, value);
        }
    }

    public void replaceAll (String var, String value) {

        if (this.left != null) {
            this.left.replaceAll(var, value);
        }
        if (this.right != null) {
            this.right.replaceAll(var, value);
        }
    }

    public ArrayList<String> getExistentialVars(ArrayList<String> vs) {

        ArrayList<String> evs = new ArrayList<String>();
        if (this.left != null) {
            evs.addAll(this.left.getExistentialVars(vs));
        }
        if (this.right != null) {
            evs.addAll(this.right.getExistentialVars(vs));
        }
        return evs;
    }

    public ArrayList<String> getVars(ArrayList<String> vs) {

        ArrayList<String> evs = new ArrayList<String>();
        if (this.left != null) {
            ArrayList<String> evsl = this.left.getVars(vs);
            for (String e : evsl) {
                if (!evs.contains(e)) {
                    evs.add(e);
                }
            }
        }
        if (this.right != null) {
            ArrayList<String> evsr = this.right.getVars(vs);
            for (String e : evsr) {
                if (!evs.contains(e)) {
                    evs.add(e);
                }
            }
        }
        return evs;
    }

    public HashMap<String, String> getConstantsMapping() {

        HashMap<String, String> hm = new HashMap<String, String>();
        if (this.left != null) {
            hm.putAll(this.left.getConstantsMapping());
        }
        if (this.right != null) {
            hm.putAll(this.right.getConstantsMapping());
        }
        return hm;
    }

    public String toString() {
        return this.left.toString() + " " + this.op + " " + this.right.toString();
    }
}
