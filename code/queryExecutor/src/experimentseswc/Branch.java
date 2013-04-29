package experimentseswc;
import java.util.ArrayList;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import java.util.HashSet;
import java.util.HashMap;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Branch extends BinaryTree {

    private BinaryTree left, right;

    public Branch (BinaryTree l, BinaryTree r) {

        this.left = l;
        this.right = r;
        this.joinVariables = unify(this.left.infoJoins,
                                   this.left.joinVariables, 
                                   this.right.joinVariables);
        this.infoJoins = this.left.infoJoins = this.right.infoJoins;
        this.size = this.left.getSize() + this.right.getSize();
    }

    public boolean equals(BinaryTree other) {

        return ((other instanceof Branch) 
             && (this.getJoinVariables() == other.getJoinVariables()) 
             && (this.infoJoins == other.infoJoins) 
             && (this.getDegree() == other.getDegree())
             && (this.getSize() == other.getSize()) 
             && (this.left == ((Branch) other).left)
             && (this.right == ((Branch) other).right));
    }

    protected String aux (String x) {

        String s = "";
        if (this.left != null) {
            s = s + x + "{\n" + this.left.aux(x+"  ") + "\n" + x + "}\n" + x + "  . \n";
        }
        if (this.right != null) {
            s = s + x + "{\n" + this.right.aux(x+"  ") + "\n"+ x+"}";
        }
        return s;
    }
    public String[] toJenaQuery(Catalog catalog, HashMap<String, String> constants) {

        String[] l = this.left.toJenaQuery(catalog, constants);
        String[] r = this.right.toJenaQuery(catalog, constants);
        l[0] = l[0] + r[0];
        l[2] = l[2] + r[2];
        l[4] = "{\n" + l[4] + "\n} . {\n" + r[4] + "}\n";

        return l;
    }

    public Model getModel(Catalog catalog, HashMap<String, String> constants/*, Reasoner reasoner*/) {

        Model result = ModelFactory.createDefaultModel();
        Model mLeft = this.left.getModel(catalog, constants/*, reasoner*/);
        if (mLeft.size()==0) {
            return result;
        }
        Model mRight = this.right.getModel(catalog, constants/*, reasoner*/);
        Query qLeft = this.left.getQuery(catalog, constants);
        Query qRight = this.right.getQuery(catalog, constants);
        ArrayList<String> jVarsLeft = this.left.getJoinVariables();
        ArrayList<String> jVarsRight = this.right.getJoinVariables();
        ArrayList<String> joinVars = inBoth(jVarsLeft, jVarsRight);
   
        //InfModel minferLeft = ModelFactory.createInfModel (reasoner, mLeft);
        HashSet<ArrayList<RDFNode>> leftSolutions = getSolutions(qLeft, /*minferLeft,*/mLeft, joinVars);
        //InfModel minferRight = ModelFactory.createInfModel (reasoner, mRight);
        HashSet<ArrayList<RDFNode>> rightSolutions = getSolutions(qRight, /*minferRight,*/mRight, joinVars);

        HashSet<ArrayList<RDFNode>> solutions = new HashSet<ArrayList<RDFNode>>(leftSolutions);
        solutions.retainAll(rightSolutions);

        for (ArrayList<RDFNode> s : solutions) {
            HashMap<String, RDFNode> map = getMap(joinVars, s);
            String qConstruct = qLeft.getConstruct(map);
            //System.out.println(qConstruct);
            QueryExecution qem = QueryExecutionFactory.create(qConstruct, /*minferLeft*/mLeft);
            qem.execConstruct(result);
            qConstruct = qRight.getConstruct(map);
            qem = QueryExecutionFactory.create(qConstruct, /*minferRight*/mRight);
            qem.execConstruct(result);
        }
        return result;     
    }

    public static ArrayList<String> inBoth(ArrayList<String> vars0, ArrayList<String> vars1) {

        ArrayList<String> vars = new ArrayList<String>(vars0);
        for (String v : vars0) {
            if (!vars1.contains(v)) {
                vars.remove(v);
            }
        }
        return vars;
    }

    public static HashMap<String, RDFNode> getMap(ArrayList<String> vars, ArrayList<RDFNode> s) {

        HashMap<String, RDFNode> map = new HashMap<String, RDFNode>();
        for (int i=0; i < vars.size(); i++) {
            String c = vars.get(i);
            RDFNode v = s.get(i);
            map.put(c, v);
        }
        return map;
    }

    public static HashSet<ArrayList<RDFNode>> getSolutions(Query q, /*InfModel*/Model m, ArrayList<String> joinVars) {

        QueryExecution queryExec = QueryExecutionFactory.create(q.toString(), m);
        HashSet<ArrayList<RDFNode>> solutions = new HashSet<ArrayList<RDFNode>>();
        for (ResultSet rs = queryExec.execSelect() ; rs.hasNext() ; ) {
            QuerySolution solution = rs.nextSolution();
            ArrayList<RDFNode> values = new ArrayList<RDFNode>();
            for (String v : joinVars) {
                values.add(solution.get(v));
            }
            solutions.add(values);
        }
        return solutions;
    }

    public Query getQuery(Catalog catalog, HashMap<String, String> constants) {

        Query qLeft = this.left.getQuery(catalog, constants);
        Query qRight = this.right.getQuery(catalog, constants);
        // change the existential variables in qLeft that appear as no existential in qRight
        ArrayList<String> existentialVarsLeft = qLeft.getExistentialVars();
        existentialVarsLeft.retainAll(qRight.getVars());
        qLeft.replaceAll(existentialVarsLeft);
        // change the existential variables in qRight that appear as no existential in qLeft
        ArrayList<String> existentialVarsRight = qRight.getExistentialVars();
        existentialVarsRight.retainAll(qLeft.getVars());
        qRight.replaceAll(existentialVarsRight);
        return qLeft.join(qRight);
    }

    public ArrayList<String> getVariables() {
        ArrayList<String> vars = new ArrayList<String>();
        if (this.left != null) {
            vars.addAll(this.left.getVariables());
        }
        if (this.right != null) {
            vars.addAll(this.right.getVariables());
        }
        return vars;
    }

    public BinaryTree getLeft() {
        return this.left;
    }

    public BinaryTree getRight() {
        return this.right;
    }
}
