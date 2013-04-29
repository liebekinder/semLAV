/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package experimentseswc;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author luisdanielibanesgonzalez
 */
public class graphExp {

    public static void main(String[] args) throws IOException {
        final String PATH = "/home/user/code/expfiles/";
        final String N3 = "berlinData/300views/viewsN3/";
        final String CATALOGPATH = "/home/user/code/expfiles/wikiTaaableData/catalog";
        final String TRUTH = "berlinData/answers/query16";

        String QUERY_PATH = PATH + "berlinData/sparqlQueries/query16.sparql";
        Properties P = new Properties();
        FileInputStream fileinput;
        try {
            fileinput = new FileInputStream(CATALOGPATH);
            P.load(fileinput);
            fileinput.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(graphExp.class.getName()).log(Level.SEVERE, null, ex);
        }
        Catalog c = new Catalog(P,PATH + N3 , "");
		ArrayList<String> views = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("/home/user/code/expfiles/relevantViewsData/relviews300/q16-Nothing-35"));
        String l = br.readLine();
        while(l!= null){
			views.add(l);
            l = br.readLine();
        }
        Model gun = ModelFactory.createDefaultModel();

        HashSet<ArrayList<String>> solutionsGathered = new HashSet<ArrayList<String>>();
        Query query;
		Timer gunTimer = new Timer();
		Timer unionTimer = new Timer();
		unionTimer.resume();
        for(int i=1; i<=views.size();i++){
            Model g = c.getModel(views.get(i-1));
            //gun = gun.union(g);
            gun.add(g);
			unionTimer.stop();
			System.out.println("Milliseconds to union " +i+": " +unionTimer.getTotalTime());
			unionTimer.resume();
        }
		unionTimer.stop();

		gunTimer.resume();
        try {
            query = Main.readQuery(QUERY_PATH);
        QueryExecution result = QueryExecutionFactory.create(query.toString(), gun);
        //QueryExecution result = QueryExecutionFactory.create(query.toString(), g);
        for (ResultSet rs = result.execSelect(); rs.hasNext();) {
            QuerySolution binding = rs.nextSolution();
            ArrayList<String> s = new ArrayList<String>();
            for (String var : query.getVars()) {
                String val = binding.get(var).toString();
                s.add(val);
            }
            solutionsGathered.add(s);
        }
		gunTimer.stop();
		System.out.println("GUN size " + gun.size());
		System.out.println("Solution size " + solutionsGathered.size());
		System.out.println("Union time " + TimeUnit.MILLISECONDS.toSeconds(unionTimer.getTotalTime()));
		System.out.println("Querying time " + TimeUnit.MILLISECONDS.toSeconds(gunTimer.getTotalTime()));
		System.out.println("GUN time " + TimeUnit.MILLISECONDS.toSeconds(unionTimer.getTotalTime() + gunTimer.getTotalTime()));
        } catch (Exception ex) {
            Logger.getLogger(graphExp.class.getName()).log(Level.SEVERE, null, ex);
        }
	
		gun.removeAll();
		solutionsGathered.clear();


		Timer jenaTimer = new Timer();
		jenaTimer.resume();
        for(String v: views){
            Model g = c.getModel(v);

        try {
            query = Main.readQuery(QUERY_PATH);
        QueryExecution result = QueryExecutionFactory.create(query.toString(), g);
        for (ResultSet rs = result.execSelect(); rs.hasNext();) {
            QuerySolution binding = rs.nextSolution();
            ArrayList<String> s = new ArrayList<String>();
            for (String var : query.getVars()) {
                String val = binding.get(var).toString();
                s.add(val);
            }
            solutionsGathered.add(s);
        }
        } catch (Exception ex) {
            Logger.getLogger(graphExp.class.getName()).log(Level.SEVERE, null, ex);
        }
		jenaTimer.stop();
		//System.out.println("Little Graph size " + g.size());
		//System.out.println("Solutions from littlegraph size " + solutionsGathered.size());
		//System.out.println("Partial time " + TimeUnit.MILLISECONDS.toSeconds(jenaTimer.getTotalTime()));
		solutionsGathered.clear();
		g.removeAll();
		jenaTimer.resume();
		
        }
		jenaTimer.stop();
		System.out.println("Total time by pieces " + TimeUnit.MILLISECONDS.toSeconds(jenaTimer.getTotalTime()));
}
		


}
