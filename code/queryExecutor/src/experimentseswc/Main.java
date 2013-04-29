/*
 * @author Luis Daniel IBANEZ
 */
package experimentseswc;
import com.hp.hpl.jena.query.DatasetFactory;
import org.hamcrest.Matcher;
import java.util.Collections;
import com.hp.hpl.jena.rdf.model.InfModel;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.io.FileWriter;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;
import static ch.lambdaj.Lambda.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;
//import com.hp.hpl.jena.query.DataSource; // when using jena2.6.4
import com.hp.hpl.jena.query.Dataset; // when using jena2.7.4
import com.hp.hpl.jena.util.FileManager;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.*;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.mgt.Explain.InfoLevel;
import com.hp.hpl.jena.shared.Lock;

public class Main {

    public static Query readQuery(String queryPath) throws Exception {
        FileInputStream fis = new FileInputStream(queryPath);
        QueryParser qp = new QueryParser(fis);
        Query q = qp.ParseSparql();
        return q;
    }

    public static Catalog loadCatalog (Properties config, final String PATH, 
                                       final String N3, final String SPARQL, 
                                       boolean contactSources) 
                                                    throws java.io.IOException {
        Properties Catalog = new Properties();
        FileInputStream fileinput = new FileInputStream(
                                             config.getProperty("catalogpath"));
        Catalog.load(fileinput);
        fileinput.close();
        Catalog catalog = new Catalog(Catalog, PATH + N3, PATH + SPARQL, contactSources);
        return catalog;
    }

    public static Reasoner makeReasoner (final String fileName) 
                                                    throws java.io.IOException {
        FileInputStream fileinput = new FileInputStream(fileName);
        OntModel ontology = ModelFactory.createOntologyModel(
                              OntModelSpec.OWL_MEM_MICRO_RULE_INF, null);
        ontology.read(fileinput, null, "N3");
        fileinput.close();
        Reasoner reasoner = ReasonerRegistry.getOWLMicroReasoner();
        reasoner = reasoner.bindSchema(ontology);
        return reasoner;
    }

    public static Properties loadConfiguration (String file) throws java.io.IOException {
        Properties ps = new Properties();
        FileInputStream fileinput = new FileInputStream(file);
        ps.load(fileinput);
        fileinput.close();
        return ps;
    }
    
    public static void main(String[] args) {

        try {
            Properties config = loadConfiguration(System.getProperty("user.dir")
                                                  +"/configC.properties");
            final String PATH = config.getProperty("path");
            final String N3 = config.getProperty("mappingsn3");
            final String SPARQL = config.getProperty("mappingssparql");
            final String ONTO = config.getProperty("ontology");
            final String JOIN_TYPE = config.getProperty("jointype");
            final String SELECTK = config.getProperty("selectK");
            final String GT = config.getProperty("groundTruth");
            final String GT_PATH = PATH + GT;
            final String QUERY_RESULTS = config.getProperty("queryresults");
            final String QUERY_RESULTS_PATH = QUERY_RESULTS + JOIN_TYPE + "/";
            String file = PATH + QUERY_RESULTS_PATH;
            makeNewDir(file);
            final String QUERY_PATH = config.getProperty("querypath");
            Query query = readQuery(QUERY_PATH);
            String[] sortTypes = config.getProperty("sorttypes").split(",");
            String[] strategies = config.getProperty("experiments").split(",");
            int start = Integer.parseInt(config.getProperty("rew.startnumber"));
            int end = Integer.parseInt(config.getProperty("rew.endnumber"));
            int hop = Integer.parseInt(config.getProperty("rew.hop"));
            int numViews = Integer.parseInt(config.getProperty("numberviews"));
            int maxNumRws = Integer.parseInt(config.getProperty("maxnumberrewritings"));
            boolean contactSources = Boolean.parseBoolean(config.getProperty("contactsources"));
            double desiredRecall = Double.parseDouble(config.getProperty("desiredrecall"));
            final String REWRITING = config.getProperty("rewritings");
            final String conjunctiveQuery = config.getProperty("conjunctiveQuery");
            final String mappings = config.getProperty("mappings");
            final HashMap<String, String> constants 
                               = loadConstants(config.getProperty("constants"));
            
            Catalog catalog = loadCatalog(config, PATH, N3, SPARQL, contactSources);
            Reasoner reasoner = null;
            if (ONTO != null) {
                reasoner = makeReasoner(PATH + ONTO);
            }

            if (strategies[0].equals("FULL")) {
                int t = Integer.parseInt(config.getProperty("timeout"));
                executeGUNWholeAnswer(PATH, ONTO, QUERY_RESULTS_PATH, query, catalog, 
                                      reasoner, GT_PATH, constants, 
                                      conjunctiveQuery, mappings, t, numViews, maxNumRws);
            } else {
                String tt = PATH + QUERY_RESULTS_PATH + "TimeTable" + JOIN_TYPE;
                BufferedWriter timetable = new BufferedWriter(new FileWriter(tt,true));
                String tt2 = PATH + QUERY_RESULTS_PATH + "TimeFirstAnswer" + JOIN_TYPE;
                BufferedWriter timetable2 = new BufferedWriter(new FileWriter(tt2,true));
                executeJoin(config, sortTypes, strategies, start, end, hop, PATH, 
                        ONTO, JOIN_TYPE, QUERY_RESULTS_PATH, SELECTK,
                        query, catalog, reasoner, timetable, timetable2, GT_PATH, REWRITING, 
                        desiredRecall, constants);
                timetable.close();
                timetable2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static HashMap<String, String> loadConstants(String file) throws Exception {

        HashMap<String, String> hm = new HashMap<String, String>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String l = br.readLine();
        while (l!=null) {
            java.util.StringTokenizer st = new java.util.StringTokenizer(l);
            hm.put(st.nextToken(), st.nextToken());
            l = br.readLine();
        }
        br.close();
        return hm;
    }

    public static ArrayList<Rewrite> getSolutions(String PATH, 
                                                  String REWRITING) throws java.io.IOException { 

        ArrayList<Rewrite> solution = new ArrayList<Rewrite>();
        String sol = "";
        BufferedReader input = new BufferedReader(new FileReader(PATH 
                                                  + REWRITING));
        while ((sol = input.readLine()) != null) {
            Rewrite r = new Rewrite(sol);
            solution.add(r);
        }
        input.close();
        return solution;
    }

    // TODO improve this method.. it is needed to generalize it..
    public static void semanticFilter(ArrayList<Rewrite> solution) {
        List<Rewrite> l = select(solution, org.hamcrest.Matchers.not(
                                 having(on(Rewrite.class).checkJoinByVar(
                                        "choco", "mainchicken", "X"))));
        l = sort(l, on(Rewrite.class).getPredicateNumber());
        solution = new ArrayList<Rewrite>(l);
        l = select(solution, having(on(Rewrite.class).checkJoinByVar("choco", "mainchicken", "Y")));
        solution.removeAll(l);
        l = select(solution, having(on(Rewrite.class).checkJoinByVar("choco", "mainchicken", "I")));
        solution.removeAll(l);
        l = select(solution, having(on(Rewrite.class).checkJoinByVar("dessertfruit", "mainchicken", "X")));
        solution.removeAll(l);
        l = select(solution, having(on(Rewrite.class).checkJoinByVar("dessertfruit", "mainchicken", "Y")));
        solution.removeAll(l);
        l = select(solution, having(on(Rewrite.class).checkJoinByVar("dessertfruit", "mainchicken", "I")));
        solution.removeAll(l);
        // Deep Ontology semantic filters
        //l = select(representatives, having(on(Rewrite.class).checkJoinByVar("citricmain", "rec_raisindark", "X")));
        //solution.removeAll(l);
    }

    public static ArrayList<Rewrite> selectRewritings(String SELECTK, int start, 
                                                      int end, ArrayList<Rewrite> solution) {
        ArrayList<Rewrite> toAnalyze = new ArrayList<Rewrite>();
        if(SELECTK.equals("Random")){
            Random ran = new Random();
            int i = 0;
            while(i < end && i < solution.size()){
                int j = ran.nextInt(end);
                if(!toAnalyze.contains(solution.get(j))){
                    toAnalyze.add(solution.get(j));
                    i++;
                }
            }
        }else{
            if (solution.size() > end) {
                toAnalyze = new ArrayList<Rewrite>(solution.subList(0, end));
            } else {
                toAnalyze = new ArrayList<Rewrite>(solution);
            }
        }
        return toAnalyze;
    }
    
    public static ArrayList<Rewrite> applyStrategy(ArrayList<Rewrite> solution, 
                                                   String sorttype, String strategy, 
                                                   Timer strategyTimer, String JOIN_TYPE) {
        
        if (solution.size() == 0) {
            return solution;
        }
        // No Action: No filtering
        if (sorttype.equals("MAX") 
               && 
              (strategy.equals("NOTHING") || strategy.equals("EQUIVALENCECLASS"))) {
            return solution;
        }
        if (strategy.equals("FILTERDUPS")) {
            strategyTimer.start();
            ArrayList<Rewrite> nondup = new ArrayList<Rewrite>(selectDistinct(solution));
            solution.retainAll(nondup);
            strategyTimer.stop();
        }
        if (strategy.equals("SEMANTICFILTER") || strategy.equals("EQUIVALENCECLASSSORT")
              || strategy.equals("EQUIVALENCECLASS")) {
            strategyTimer.start();
            if(strategy.equals("SEMANTICFILTER")){
                semanticFilter(solution);
                strategyTimer.stop();
                System.out.println("Number of Solutions after SemFilter: " + solution.size());
                strategyTimer.resume();
            }

            ArrayList<ArrayList<Rewrite>> eq = new ArrayList<ArrayList<Rewrite>>();
            ArrayList<ArrayList<Rewrite>> eq2 = new ArrayList<ArrayList<Rewrite>>();
            if(JOIN_TYPE.equals("GUN")) {
                eq = Rewrite.equivalenceClasses(solution);
            } else {
                eq = Rewrite.equivalenceClassesJoin2(solution);
            }
            strategyTimer.stop();
            System.out.println("Number of EQCLASSES: " + eq.size());
            strategyTimer.resume();
            ArrayList<Rewrite> representatives = new ArrayList<Rewrite>();

            // Choice of Representative:
            for (ArrayList<Rewrite> cla : eq) {
              /*
                System.out.println("CLASS [");
                for(Rewrite r: cla){
                    System.out.println(r);
                }
                System.out.println("]");
               */
                Rewrite rep = new Rewrite(new Predicate(""),new ArrayList<Predicate>());
                if(sorttype.equals("MIN")) {
                    rep = selectMin(cla, on(Rewrite.class).getPredicateNumber());
                } else {
                    rep = selectMax(cla, on(Rewrite.class).getPredicateNumber());
                }
                //System.out.println("Representative:" + rep);
                representatives.add(rep);
            }

            if (strategy.equals("EQUIVALENCECLASS")) {
                solution = representatives;
            } else if (strategy.equals("EQUIVALENCECLASSSORT") 
                          || strategy.equals("SEMANTICFILTER")) {
                List<Rewrite> l = sort(representatives, 
                                       on(Rewrite.class).getPredicateNumber());
                solution = new ArrayList<Rewrite>(l);
                if (sorttype.equals("MAX")) {
                    Collections.reverse(solution);
                }
            }
            strategyTimer.stop();
        }
        return solution;
    }
 
    public static void executeGUNWholeAnswer(final String PATH, final String ONTO,  
                                             final String QUERY_RESULTS_PATH, Query query, 
                                             Catalog catalog, Reasoner reasoner, 
                                             final String GT_PATH, 
                                             HashMap<String, String> constants, 
                                             String conjunctiveQuery, String mappings, int t, 
                                             int numViews, int maxNumRws) throws Exception {
        String line;
        String codePath = System.getProperty("user.dir");
        codePath = codePath.substring(0, codePath.lastIndexOf("/queryExecutor/src"));
        int number = 0;
        HashSet<ArrayList<String>> solutionsGathered = new HashSet<ArrayList<String>>();
        String dir = PATH + QUERY_RESULTS_PATH + "FULL";
        makeNewDir(dir);
        BufferedWriter info = new BufferedWriter(new FileWriter(dir+"/throughput", true));
        info.write("# Time (milliseconds)\t Number of answers \t Number of rewritings considered");
        info.newLine();
        info.flush();
        HashSet<String> loadedViews = new HashSet<String>();
        Model graphUnion = ModelFactory.createDefaultModel();
        Timer numberTimer = new Timer();
        Timer executionTimer = new Timer();
        Timer wrapperTimer = new Timer();
        Timer graphCreationTimer = new Timer();
        Counter ids = new Counter();
        numberTimer.start();
        List<String> l = new ArrayList<String>();
        l.add("timeout");
        l.add(t+"m");
        l.add(codePath + "/ssdsat/driver.py");
        l.add("-t");
        l.add("RW");
        l.add("-v");
        l.add(PATH + mappings);
        l.add("-q");
        l.add(conjunctiveQuery);
        ProcessBuilder pb = new ProcessBuilder(l);
        Process p = pb.start();
        InputStream is = p.getInputStream();
        InputStream es = p.getErrorStream();
        Counter executedRewritings = new Counter();
        Thread tinput = new IncludingStreamRW(is, catalog, graphUnion, loadedViews, 
                                            executedRewritings, numViews, constants);
        Thread terror = new IgnoringStream(es);
        terror.setPriority(Thread.MIN_PRIORITY);
        tinput.setPriority(Thread.MIN_PRIORITY);
        tinput.start();
        terror.start();
        Thread tquery = new QueryingStream(graphUnion, reasoner, query, 
                            solutionsGathered, executionTimer, numberTimer, executedRewritings, info, dir, wrapperTimer, graphCreationTimer, ids);
        tquery.setPriority(Thread.MAX_PRIORITY);
        tquery.start();
        //System.out.println("priority of tquery: "+tquery.getPriority());
        //System.out.println("priority of terror: "+terror.getPriority());
        //System.out.println("priority of tinput: "+tinput.getPriority());
        //System.out.println("max priority: "+Thread.MAX_PRIORITY);
        //System.out.println("min priority: "+Thread.MIN_PRIORITY);
        int exitValue = p.waitFor();
        //System.out.println("exitValue: "+exitValue);
        tinput.join();
        tquery.interrupt();
        tquery.join();
        is.close();
        es.close();
        if (   ((exitValue == 0) && (executedRewritings.getValue() < maxNumRws))
            || (loadedViews.size() == numViews)) {
            info.write("# The answer is complete.");
        } else {
            info.write("# The answer may be incomplete.");
        }
        info.newLine();
        if (exitValue != 0) {
            info.write("# Error generating the rewritings. Code error: "+exitValue);
            info.newLine();
        }
        info.flush();
        info.close();
    }
 
    public static void executeJoin (Properties config, String[] sortTypes, 
                                    String[] strategies, int start, int end, 
                                    int hop, final String PATH, final String ONTO, 
                                    final String JOIN_TYPE, 
                                    final String QUERY_RESULTS_PATH, final String SELECTK,
                                    Query query, Catalog catalog, Reasoner reasoner, 
                                    BufferedWriter timetable, BufferedWriter timefirstAnswer, final String GT_PATH, 
                                    final String REWRITING, double desiredRecall, 
                                    HashMap<String, String> constants) throws Exception {

        for (String sorttype : sortTypes) {
            for (String strategy : strategies) {
                ArrayList<Rewrite> solution = getSolutions(PATH, REWRITING);
                Timer strategyTimer = new Timer();
                solution = applyStrategy(solution, sorttype, strategy, 
                                         strategyTimer, JOIN_TYPE);

                ArrayList<Rewrite> toAnalyze = selectRewritings(SELECTK, start, 
                                                                end, solution);

                Timer evaluationTimer = new Timer();
                Timer wrapperTimer = new Timer();
                Timer graphCreationTimer = new Timer();
                
                HashSet<ArrayList<String>> solutionsGathered = new HashSet<ArrayList<String>>();
                Model graphUnion = ModelFactory.createDefaultModel();
                int number = start;
                String dir = PATH + QUERY_RESULTS_PATH + sorttype + "_" + strategy;
                makeNewDir(dir);
                BufferedWriter info = new BufferedWriter(new FileWriter(dir+"/modelSize", true));
                HashSet<String> loadedViews = new HashSet<String>();
                QueryExecution result = null;
                boolean answered = false;
                graphCreationTimer.start();
                for (Rewrite rew : toAnalyze) {
                    graphCreationTimer.stop();
                    System.out.println("Executing Rewriting: " + rew.toString());
                    info.write("Executing Rewriting: " + rew.toString());
                    info.newLine();
                    info.flush();
                    graphCreationTimer.resume();
                    if (JOIN_TYPE.equals("GUN")){
                        for(Predicate p : rew.getGoals()){
                            if(loadedViews.add(p.getName())){
                            	graphCreationTimer.stop();
                            	wrapperTimer.resume();
                            	Model m = catalog.getModel(p, constants);
                                wrapperTimer.stop();
                            	//long t = wrapperTimer.stop();
                                //System.out.println("(GUN) Loading time of "+p+" is "+TimeUnit.MILLISECONDS.toSeconds(t));
                            	graphCreationTimer.resume();
                                graphUnion.add(m);
                            }
                        }
                        graphCreationTimer.stop();	
                        info.write("gun model size: " + graphUnion.size());
                        //info.write("gun model #Views: "+loadedViews.size());
                        info.newLine();
                        info.flush();
                        graphCreationTimer.resume();
                        Model m = graphUnion;
                        if (reasoner != null) {
                            m = ModelFactory.createInfModel (reasoner, m);
                        }
                        graphCreationTimer.stop();
                        // in GUN evaluation each evaluation is independent of the previous ones.
                        evaluationTimer.start();
                        result = QueryExecutionFactory.create(query.toString(), m);
                    } else if (JOIN_TYPE.equals("LLP")) { // Bushy Tree (in views) with named graphs
                        ArrayList<Predicate> goals = rew.getGoals();
                        BinaryTree bt = BushyTree.getBushyTree(goals);
                        String [] res = bt.toJenaQuery(catalog, constants);
                        String s = "";
                        for (String e : res) {
                            s = s + e;
                        }
                        Dataset d = getDataset(goals, catalog, info, graphCreationTimer, wrapperTimer, reasoner, constants);
                        graphCreationTimer.stop();
                        evaluationTimer.resume();
                        result = QueryExecutionFactory.create(s, d);
                        
                    } else if (JOIN_TYPE.equals("BT")) { // Bushy Tree (in predicates) without named graph IS UNFINISHED
                        Model join = rew.union(catalog, new HashSet<String>(), wrapperTimer, graphCreationTimer, constants);
                        if (reasoner != null) {
                            join = ModelFactory.createInfModel (reasoner, join);
                        }
                        graphCreationTimer.stop();
                        info.write("bushy tree model size: "+join.size());
                        info.newLine();
                        info.flush();
                        graphCreationTimer.resume();
                        String s = catalog.getSparqlBTQuery(rew, query.getDistinct(), constants, query.getVars());
                        //String s = rew.getSparqlQuery(catalog, constants);
                        graphCreationTimer.stop();
                        evaluationTimer.resume();
                        result = QueryExecutionFactory.create(s, join);
                    } else { // LLP in Jena using named graphs
                        ArrayList<Predicate> goals = rew.getGoals();
                        String[] qrw = {"","","","","",""};
                        for (Predicate p : goals) {
                            String[] s = toJenaQuery(p, catalog, constants);
                            qrw[0] = qrw[0] + s[0];
                            qrw[1] = s[1];
                            qrw[3] = s[3];
                            qrw[4] = qrw[4] + s[4];
                            qrw[5] = s[5];
                        }
                        for (String v : query.getVars()) {
                            qrw[2] = qrw[2] + " " + v;
                        }
                        String s = "";
                        for (String e : qrw) {
                            s = s + e;
                        }
                        Dataset d = getDataset(goals, catalog, info, graphCreationTimer, wrapperTimer, reasoner, constants);
                        graphCreationTimer.stop();
                        evaluationTimer.resume();
                        result = QueryExecutionFactory.create(s, d);
                    }
                    for (ResultSet rs = result.execSelect(); rs.hasNext();) {
                        QuerySolution binding = rs.nextSolution();
                        ArrayList<String> s = new ArrayList<String>();
                        for (String var : query.getVars()) {
                            String val = binding.get(var).toString();
                            s.add(val);
                        }
                        evaluationTimer.stop();
                        if (solutionsGathered.isEmpty()) {
                            writeTime(timefirstAnswer, sorttype + strategy, strategyTimer, wrapperTimer, graphCreationTimer, evaluationTimer);
                        }
                        evaluationTimer.resume();
                        solutionsGathered.add(s);
                    }
                    evaluationTimer.stop();
                    writeTime(timetable, sorttype + strategy + "_" + number, 
                    		  strategyTimer, wrapperTimer, graphCreationTimer, 
                    		  evaluationTimer);

                    double r = saveResults(PATH, dir, GT_PATH, number, solutionsGathered);
                    number++;
                    graphCreationTimer.resume();
                } // end FOR rewritings
                graphCreationTimer.stop();
            }// end FOR strategies
        } // end FOR sorttypes
    }

    public static void writeTime(BufferedWriter output, String id, 
    		                     Timer strategyTimer, Timer wrapperTimer, 
    		                     Timer graphCreationTimer, Timer evaluationTimer) throws Exception {
    
        output.write(id + " "
                + TimeUnit.MILLISECONDS.toSeconds(strategyTimer.getTotalTime())
                + "  "
                + TimeUnit.MILLISECONDS.toSeconds(wrapperTimer.getTotalTime())
                + "  "
                + TimeUnit.MILLISECONDS.toSeconds(graphCreationTimer.getTotalTime())
                + "  "                                
                + TimeUnit.MILLISECONDS.toSeconds(evaluationTimer.getTotalTime())
	            + "  "
                + TimeUnit.MILLISECONDS.toSeconds(strategyTimer.getTotalTime()
                                                + wrapperTimer.getTotalTime()
                                                + graphCreationTimer.getTotalTime()
                                                + evaluationTimer.getTotalTime()));
        output.newLine();
        output.flush();
    }
    
    public static String[] toJenaQuery(Predicate p, Catalog catalog, HashMap<String, String> constants) {
        Query q = catalog.getQuery(p, constants);
        String[] res = q.getStrings();
        res[4] = "{ GRAPH <http://"+p.getName()+">\n  {\n" + res[4] + "\n} } ";

        return res;
    }

    public static Dataset getDataset(ArrayList<Predicate> goals, Catalog catalog, 
                                     BufferedWriter info, Timer graphCreationTimer, 
                                     Timer wrapperTimer, Reasoner reasoner, 
                                     HashMap<String, String> constants) 
                                                                     throws Exception{

        Dataset d = DatasetFactory.createMem();
        for (Predicate p : goals) {
            String n = p.getName();
            graphCreationTimer.stop();
            wrapperTimer.resume();
            Model m = catalog.getModel(p, constants);
            wrapperTimer.stop();
            //long t = wrapperTimer.stop();
            //System.out.println("(Jena) Loading time of "+p+" is "+TimeUnit.MILLISECONDS.toSeconds(t));
            graphCreationTimer.resume();
            if (reasoner != null) {
                m = ModelFactory.createInfModel (reasoner, m);
            }
            graphCreationTimer.stop();
            info.write("Named graph - predicate: " + p+" model size: "+m.size());
            info.newLine();
            info.flush();
            graphCreationTimer.resume();
            d.addNamedModel("http://"+n, m);
        }
        return d;
    }

    public static double saveResults(String PATH, String dir, String groundTruth, 
                                     int number, HashSet<ArrayList<String>> results) 
                                                        throws java.io.IOException {

        String file = dir + "/solutions" + number;
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                                new FileOutputStream(file), "UTF-8")); 
        for (ArrayList<String> r : results) {
            output.write(r.toString());
            output.newLine();
        }
        output.flush();
        output.close();
        
        output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(dir+"/answerSize", true), "UTF-8"));
        output.write(number+"\t"+results.size());
        output.newLine();
        output.flush();
        output.close();
        double recall = processFile(PATH, dir, groundTruth, file);
        deleteDir(new File(file));
        return recall;
    }

    public static void makeNewDir(String file) {
        
        File f = new File(file);
        deleteDir(f);                
        f.mkdir();
    }

    public static void deleteDir(File f) {
        
        File[] content = f.listFiles();
        if (content != null) {
            for (File g : content) {
                deleteDir(g);
            }
        }
        f.delete();
    }

    public static double processFile(String PATH, String dir, String groundTruth, 
                                     String file) {
   
        ProcessBuilder pb = new ProcessBuilder(PATH + "scripts/calculePrecisionRecallFile.sh", 
                                               dir, groundTruth, file);
        pb.directory(new File (PATH+"scripts/"));
        try {
            Process p = pb.start();
            p.waitFor();
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            double r = Double.parseDouble(br.readLine());
            return r;
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
            return 0;
        } catch (InterruptedException iex){
            iex.printStackTrace(System.out);
            return 0;
        }
    }
}
