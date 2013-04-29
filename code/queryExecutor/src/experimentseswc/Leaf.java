package experimentseswc;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import java.util.HashSet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Leaf extends BinaryTree {

    private Predicate element;

    public Leaf (Predicate e, ArrayList<String> joinVars, 
                 HashMap<String, Integer> infoJoins) {
        this.element = e;
        this.joinVariables = joinVars;
        this.infoJoins = infoJoins;
        this.size = 1;
    }

    public boolean equals(BinaryTree other) {

        return ((other instanceof Leaf) 
             && (this.getJoinVariables() == other.getJoinVariables()) 
             && (this.infoJoins == other.infoJoins) 
             && (this.getDegree() == other.getDegree())
             && (this.element == ((Leaf) other).element));
    }

    public String[] toJenaQuery(Catalog catalog, HashMap<String, String> constants) {
        Query q = this.getQuery(catalog, constants);
        String[] s = q.getStrings();
        s[4] = "{ GRAPH <http://"+this.element.getName()+">\n  {\n" + s[4] + "\n} } ";
        
        return s;
    }

    protected String aux (String x) {

        return x + this.element.toString();
    }

    public Predicate getElement() {

        return this.element;
    }

    public ArrayList<String> getVariables() {

        ArrayList<String> vs = this.element.getVars();
        return vs;
    }

    public Model getModel(Catalog catalog, HashMap<String, String> constants/*, Reasoner reasoner*/) {

        Model m = catalog.getModel(this.element, constants);
        Query q = this.getQuery(catalog, constants);
        HashSet<QuerySolution> solutions = new HashSet<QuerySolution>();

        //InfModel minfer = ModelFactory.createInfModel (reasoner, m);
        QueryExecution queryExec = QueryExecutionFactory.create(q.toString(), /*minfer*/m);

        Model result = ModelFactory.createDefaultModel();

        for (ResultSet rs = queryExec.execSelect() ; rs.hasNext() ; ) {
            QuerySolution solution = rs.nextSolution();
            String qConstruct = q.getConstruct(getMap(solution));
            QueryExecution qem = QueryExecutionFactory.create(qConstruct, /*minfer*/m);
            qem.execConstruct(result);
        }
        return result;     
    }

    public static HashMap<String, RDFNode> getMap (QuerySolution qs) {

        HashMap<String, RDFNode> map = new HashMap<String, RDFNode>();
        Iterator<String> it = qs.varNames();
        while (it.hasNext()) {
            String c = it.next();
            RDFNode v = qs.get(c);
            map.put(c, v);
        }
        return map;
    }

    public Query getQuery(Catalog catalog, HashMap<String, String> constants) {

        Query q = catalog.getQuery(this.element, constants);
        return q;
    }
}
