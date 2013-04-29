package experimentseswc;
import java.util.HashMap;
import java.util.ArrayList;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public abstract class BinaryTree implements Comparable<BinaryTree> {

    protected HashMap<String, Integer> infoJoins;
    protected ArrayList<String> joinVariables;
    protected ArrayList<String> variables;
    protected int size;

    public int compareTo (BinaryTree other) {

         if (this.getSize() < other.getSize()) {
             return -1;
         } else if (this.getSize() > other.getSize()) {
             return 1;
         } else {
             if (this.getDegree() < other.getDegree()) {
                 return -1;
             } else if (this.getDegree() > other.getDegree()) {
                 return 1;
             } else {
                 return 0;
             }
         }
    }

    public int getSize() {

        return this.size;
    }

    public int getDegree() {

        int d = 0;
        for (String v : this.joinVariables) {
            if (this.infoJoins.containsKey(v)) {
                d = d + this.infoJoins.get(v);
            }
        }
        return d;
    }

    public abstract boolean equals(BinaryTree other);

    public String toString() {

        return this.aux(" ");
    }

    public abstract String[] toJenaQuery(Catalog catalog, HashMap<String, String> constants);

    public abstract ArrayList<String> getVariables();

    public abstract Model getModel(Catalog catalog, HashMap<String, String> constants/*, Reasoner reasoner*/);

    public abstract Query getQuery(Catalog catalog, HashMap<String, String> constants);

    public ArrayList<String> getJoinVariables() {

        return this.joinVariables;
    }

    public static ArrayList<String> unify (HashMap<String, Integer> info, 
                                           ArrayList<String> vars0, 
                                           ArrayList<String> vars1) {
        ArrayList<String> vars = new ArrayList<String>(vars0);
        for (String v : vars1) {
            if (vars.contains(v)) {
                info.put(v, info.get(v) - 1);
                if (info.containsKey(v) && info.get(v) == 0) {
                    info.remove(v);
                    vars.remove(v);
                }
            } else {
                vars.add(v);
            }
        }
        return vars;
    }

    protected abstract String aux (String x);
    //public abstract Map<String, Integer> infoJoins();
}
