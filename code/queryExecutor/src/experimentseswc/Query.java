package experimentseswc;
import java.util.HashMap;
import java.util.ArrayList;
import com.hp.hpl.jena.query.QuerySolution;
import java.util.Iterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Literal;

public class Query  {

    private HashMap<String, String> prefixes;
    private ArrayList<String> vars;
    private ArrayList<BasicGraphPattern> bgps;
    private static int i = 0;
    private boolean distinct;

    public Query (ArrayList<String> vs, ArrayList<BasicGraphPattern> bgps, HashMap<String, String> ps, boolean d) {
        this.vars = vs;
        this.bgps = bgps;
        this.prefixes = ps;
        this.distinct = d;
        if (this.vars.size() == 0) {
            this.vars = this.getVars();
        }
    }

    public void makeBT() {

    }

    public void addBGP(BasicGraphPattern bgp) {

        this.bgps.add(bgp);
    }
    
    public void extendBGPs(ArrayList<BasicGraphPattern> bgps2) {
    	this.bgps.addAll(bgps2);
    }
    
    public void extendPs(HashMap<String, String> ps) {
    	this.prefixes.putAll(ps);
    }

    public void setDistinct(boolean d) {

        this.distinct = d;
    }
    
    public boolean getDistinct() {

        return this.distinct;
    }

    public Query join (Query other) {

        ArrayList<String> newVars = new ArrayList<String>(this.getVars());
        for (String v : other.getVars()) { 
            if (!newVars.contains(v)) {
                newVars.add(v);
            }
        }
        ArrayList<BasicGraphPattern> newBGPs = new ArrayList<BasicGraphPattern>(this.bgps);
        newBGPs.addAll(other.bgps);
        HashMap<String, String> newPrefixes = new HashMap<String, String>(this.prefixes);
        newPrefixes.putAll(other.prefixes);
        return new Query(newVars, newBGPs, newPrefixes, this.distinct || other.distinct);
    }

    public ArrayList<String> getVars() {
        if (this.vars.size() > 0) {
            return this.vars;
        }
        ArrayList<String> vs = new ArrayList<String>();
        for (BasicGraphPattern bgp : this.bgps) {
            vs.addAll(bgp.getVars(vs));
        }
        return vs;        
    }
    
    public void setVariables(ArrayList<String> vs) {
    	
    	this.vars = vs;
    }

    public ArrayList<String> getExistentialVars() {
        return this.getExistentialVars(this.getVars());
    }

    public ArrayList<String> getExistentialVars(ArrayList<String> vs) {

        ArrayList<String> evs = new ArrayList<String>();

        for (BasicGraphPattern bgp : this.bgps) {
            evs.addAll(bgp.getExistentialVars(vs));
        }
        return evs;
    }

    public void replaceAll(ArrayList<String> vs) {
        for (String v : vs) {
            this.replace(v);
        }
    }

    public void replace(String v) {

        String nv = "?_new_existential_var_"+this.i;
        this.i++;
        for (BasicGraphPattern bgp : this.bgps) {
            bgp.replace(v, nv);
        }
    }

    public void replace(String var, String newVar) {
        int j = this.vars.indexOf(var);
        if (j>=0) {
            this.vars.remove(j);
            this.vars.add(j, newVar);
        }
        for (BasicGraphPattern bgp : this.bgps) {
            bgp.replace(var, newVar);
        }
    }

    public String toMapping(String name) {

        String s = name+"(";
        for (String v : this.getVars()) {
            s = s + v.substring(1) + ", ";
        }
        if (this.getVars().size() > 0) {
            s = s.substring(0, s.length()-2);
        }
        s = s + ") :- ";
        for (BasicGraphPattern bgp : this.bgps) {
            s = s + bgp.toPredicate() + ", ";
        }
        if (this.bgps.size() > 0) {
            s = s.substring(0, s.length()-2);
        }
        return s;
    }

    public HashMap<String, String> getConstantsMapping() {

        HashMap<String, String> hm = new HashMap<String, String>();
        for (BasicGraphPattern bgp : this.bgps) {
            hm.putAll(bgp.getConstantsMapping());
        }
        return hm;
    }

    public String[] getStrings() {

        String s1 = "";
        String s2 = "";
        String s3 = "";
        String s4 = "";
        String s5 = "";
        String s6 = "";
        for (String k : this.prefixes.keySet()) {
            s1 = s1 + "PREFIX " + k + ": " + this.prefixes.get(k) + "\n";
        }
        s2 = s2 + "SELECT" + (this.distinct ? " DISTINCT" : "");
        for (String v : this.vars) {
            s3 = s3 + " " + v;
        }
        if (this.vars.size() == 0) {
            s3 = s3 + " *";
        }
        s4 = s4 + "\nWHERE {\n";
        for (BasicGraphPattern bgp : this.bgps) {
            s5 = s5 + "\t" + bgp.toString() + " .\n";
        }
        s6 = s6 + "}";
        String[] result = new String[6];
        result[0] = s1;
        result[1] = s2;
        result[2] = s3;
        result[3] = s4;
        result[4] = s5;
        result[5] = s6;
        return result;
    }

    public String toString() {

        String r[] = this.getStrings();
        String s = "";
        for (String e : r) {
            s = s + e;
        }
        return s;
    }
/*
    public String getConstruct(ArrayList<String> vars, ArrayList<String> values) {

        //System.out.println("solution: "+qs);
        String s = "";
        for (String k : this.prefixes.keySet()) {
            s = s + "PREFIX " + k + ": " + this.prefixes.get(k) + "\n";
        }
        s = s + "CONSTRUCT {\n";
        ArrayList<Triple> triples = this.getTriples();
        
        for (Triple t : triples) {
            Triple nt =  new Triple(t);
            for (String vn : qs.keySet() ) {
                //String vn = it.next();
                String val = convert(qs.get(vn));
		//System.out.println("replace "+vn+" with "+val);
                nt.replaceAll(vn, val);
            }
            s = s + "\t" + nt.toString() + " .\n";
        }
        s = s + "}\n";
        s = s + "WHERE {\n";
        for (BasicGraphPattern bgp : bgps) {
            BasicGraphPattern nbgp =  bgp.copy();
            //Iterator<String> it = qs.varNames();
            for (String vn : qs.keySet() ) {
                //String vn = it.next();
                String val = convert(qs.get(vn));
                nbgp.replaceAll(vn, val);
            }
            s = s + "\t" + nbgp.toString() + " .\n";
        }
        s = s + "}\n";
        //System.out.println("query: "+this.toString());
        //System.out.println("construct: "+s);
        return s;
    }*/

    public String getConstruct(HashMap<String,RDFNode> qs) {

        //System.out.println("solution: "+qs);
        String s = "";
        for (String k : this.prefixes.keySet()) {
            s = s + "PREFIX " + k + ": " + this.prefixes.get(k) + "\n";
        }
        s = s + "CONSTRUCT {\n";
        ArrayList<Triple> triples = this.getTriples();
        
        for (Triple t : triples) {
            Triple nt =  new Triple(t);
            for (String vn : qs.keySet() ) {
                //String vn = it.next();
                String val = convert(qs.get(vn));
		//System.out.println("replace "+vn+" with "+val);
                nt.replaceAll(vn, val);
            }
            s = s + "\t" + nt.toString() + " .\n";
        }
        s = s + "}\n";
        s = s + "WHERE {\n";
        for (BasicGraphPattern bgp : bgps) {
            BasicGraphPattern nbgp =  bgp.copy();
            //Iterator<String> it = qs.varNames();
            for (String vn : qs.keySet() ) {
                //String vn = it.next();
                String val = convert(qs.get(vn));
                nbgp.replaceAll(vn, val);
            }
            s = s + "\t" + nbgp.toString() + " .\n";
        }
        s = s + "}\n";
        //System.out.println("query: "+this.toString());
        //System.out.println("construct: "+s);
        return s;
    }

    public static String convert (RDFNode n) {

        String s = "";
        if (n.isURIResource()) {
            s = "<"+n.toString()+">";
        } else if (n.isLiteral()) {
            Literal l = n.asLiteral();
            s = "\""+l.getLexicalForm()+"\"";
            String lg = l.getLanguage();
            String dt = l.getDatatypeURI();
            if (dt!=null) {
                s = s+"^^<"+dt+">";
            } else if (lg!="") {
                s = s+"@"+lg;
            }
        }
        return s;
    }

    public ArrayList<Triple> getTriples() {

        ArrayList<Triple> ts = new ArrayList<Triple>();
        for (BasicGraphPattern bgp : this.bgps) {
            if (bgp instanceof Triple) {
                ts.add((Triple) bgp);
            }
        }
        return ts;
    }
}

