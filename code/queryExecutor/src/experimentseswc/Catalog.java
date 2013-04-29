/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentseswc;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author gonzalez-l
 */
public class Catalog {

	//HashMap<String,String> catalog;
	Properties catalog;
	String execDir;
    String sparqlDir;
    boolean contactSource=false;

	public Catalog(){

		this.catalog = new Properties();
		this.execDir = "";
        this.sparqlDir = "";
	}

	public Catalog(Properties cat){

		this.catalog = cat;
	}

    public Catalog(Properties cat, String p, String q){

		this.catalog = cat;
		this.execDir = p;
        this.sparqlDir = q;
	}
	
	public Catalog(Properties cat, String p, String q, boolean c){

		this.catalog = cat;
		this.execDir = p;
        this.sparqlDir = q;
        this.contactSource = c;
	}

	// Be aware that this method doesn't take into account the view arguments.
	// If you want that, use the next one
	public Model getModel(String str){

	    if (this.contactSource) {
	    //System.out.println(this.execDir);
	    //System.out.println(str);
	    //System.out.println(this.catalog.getProperty(str));
	    
	    ArrayList<String> parametres = new ArrayList<String>();
	    parametres.add(this.execDir + "downloadresult.sh");
	    String prop = this.catalog.getProperty(str);
	    for(String subprop : prop.split(",")){
	      parametres.add(subprop);
	      }
	    
            ProcessBuilder pb = new ProcessBuilder(parametres);
	
	//log append
//	File log = new File("/home/seb/Documents/TER/V3/gun2012/code/expfiles/berlinData/DATASET/n3dir/logFile.log");
//	pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
	
	
            pb.directory(new File (this.execDir));
            
            try {
                Process p = pb.start();
                if(p.waitFor() != 0){
		    System.out.println("erreur wtf");
                    return null;
                }
            } catch (IOException ex) {
	      System.out.println("erreur io");
                ex.printStackTrace(System.out);
                return null;
            } catch (InterruptedException iex){
		System.out.println("erreur interruption");
                iex.printStackTrace(System.out);
                return null;
            }
        }
        Model res = FileManager.get().loadModel(this.execDir+str+".n3");
        if (res == null) System.out.println(this.execDir+str+".n3 is null!");
        //System.out.println("le model est nul :"+ this.execDir+str+".n3");
        return res;
    }
	
	public Model getModel(Predicate p, HashMap<String, String> constants){
		
		String end = "";
		boolean none = true;
        for (String a : p.getArguments()) {
            if (constants.containsKey(a)) {
                end = end + "_" + a;
                none = false;
                //System.out.println("a: "+a);
            } else {
            	end = end + "_V";
            }
        }
        String name = p.getName();
        //System.out.println("name: "+name);
        //System.out.println("none: "+none);
        if (!none) {
            name = name+end;
        }
        System.out.println("view name: "+name);
        return getModel(name);
	}

    public Query getQuery(Predicate p, HashMap<String, String> constants) {
        /*
           leer el query asociado con el nombre del predicado p
           parsear el query
           hacer reemplazo de variables del query por los argumentos de p
         */
		try {
            String fileName = this.sparqlDir+p.getName()+".sparql";
            FileInputStream fis = new FileInputStream(fileName);
            QueryParser qp = new QueryParser(fis);
            Query q = qp.ParseSparql();
            ArrayList<String> existentialVars = q.getExistentialVars();
            ArrayList<String> arguments = new ArrayList<String>();
            for (String a : p.getArguments()) {
                arguments.add("?"+a);
            }
            existentialVars.retainAll(arguments);
            q.replaceAll(existentialVars);
            ArrayList<String> projectedVars = q.getVars();
            for (int k = 0; k < projectedVars.size(); k++) {
                String a = arguments.get(k);
                String v = projectedVars.get(k);
                q.replace(v, a);
            }
            for (String a : p.getArguments()) {
                if (constants.containsKey(a)) {
                    q.addBGP(new Filter(new BinaryExpression("=", new UnaryExpression("?"+a), new UnaryExpression(constants.get(a)))));
                }
            }
            return q;
        }  catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
        }
    }

    // TODO consider distinct
    public String getSparqlBTQuery(Rewrite r, boolean distinct, HashMap<String, String> constants, ArrayList<String> vars) {
    	
    	ArrayList<Predicate> sgs = r.getGoals();
    	//ArrayList<BasicGraphPattern> bgps = new ArrayList();
    	//HashMap<String, String> ps = new HashMap<String, String>();
    	Query rwq = getQuery(sgs.get(0), constants);
		for (int i = 1; i < sgs.size(); i++) {
            Predicate p = sgs.get(i);
			Query q = getQuery(p, constants);
			rwq.join(q);
		}
		rwq.setVariables(vars);
		rwq.makeBT();
		return rwq.toString();
    }
}
