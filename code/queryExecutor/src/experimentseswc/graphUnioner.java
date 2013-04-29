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

/**
 *
 * @author luisdanielibanesgonzalez
 */
public class graphUnioner {

    public static void main(String[] args) throws IOException {
    
        final String PATH = "/Users/luisdanielibanesgonzalez/Documents/Travaille/ArticlesEcrit/gun2012/code/expfiles/";
        final String N3 = "berlinDataSet2/dataset10_5/viewsN3/";
        final String CATALOGPATH = "/Users/luisdanielibanesgonzalez/Documents/Travaille/ArticlesEcrit/gun2012/code/expfiles/catalog";
        final String TRUTH = "berlinDataSet2/dataset10_10/answers/query14";

        String QUERY_PATH = PATH + "berlinDataset2/queries/query14.sparql";
        
        Properties P = new Properties();
        FileInputStream fileinput;
        try {
            fileinput = new FileInputStream(CATALOGPATH);
            P.load(fileinput);
            fileinput.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(graphUnioner.class.getName()).log(Level.SEVERE, null, ex);
        }
        Catalog c75 = new Catalog(P,PATH + N3,"");
        Catalog c300 = new Catalog(P,PATH + "berlinDataSet2/dataset10_20/viewsN3/", "");

        BufferedReader br = new BufferedReader(new FileReader("/Users/luisdanielibanesgonzalez/Documents/Travaille/ArticlesEcrit/gun2012/code/queryExecutor/src/relviews75/q14-Nothing-35"));
        String l = br.readLine();
        Model gun = ModelFactory.createDefaultModel();
        while(l!= null){
            Model g = c75.getModel(l);
            gun.union(g);
            l = br.readLine();
        }

        
        HashSet<ArrayList<String>> solutionsGathered = new HashSet<ArrayList<String>>();
        Query query;
        try {
            query = Main.readQuery(QUERY_PATH);
        QueryExecution result = QueryExecutionFactory.create(query.toString(), /*inf*/gun);
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
            Logger.getLogger(graphUnioner.class.getName()).log(Level.SEVERE, null, ex);
        }

        double recall = Main.saveResults(PATH, "goldenViewsExps", TRUTH, 35, solutionsGathered);
        System.out.println(recall);

        Model g = c300.getModel("view3_2");
        gun.union(g);
        recall = Main.saveResults(PATH, "goldenViewsExps", TRUTH, 36, solutionsGathered);
        System.out.println(recall);
        


    }

        
    
    
}
