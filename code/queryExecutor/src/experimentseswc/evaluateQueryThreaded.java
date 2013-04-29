package experimentseswc;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class evaluateQueryThreaded {
	
    public static void main(String[] args) throws Exception {
		
        String configFile = args[0];
        Properties config = Main.loadConfiguration(configFile);
        String cQueryPath = config.getProperty("cQueryPath");
        String mappingsPath = config.getProperty("mappingsPath");
        ConjunctiveQuery q = loadCQuery(cQueryPath);
        ArrayList<ConjunctiveQuery> ms = loadMappings(mappingsPath);
        //Timer relViewsTimer = new Timer();
        //relViewsTimer.start();
        
        //relViewsTimer.stop();
        String sparqlQuery = config.getProperty("sQueryPath");
        String path = config.getProperty("path");
        String queryResults = config.getProperty("queryResults");		
        String queryResultsPath = queryResults + "/";
        String file = path + queryResultsPath;
        Main.makeNewDir(file);		
        String groundTruthFile = config.getProperty("groundTruth");
        String groundTruthPath = path+groundTruthFile;		
        String n3Dir = config.getProperty("n3Dir");
        String sparqlDir = config.getProperty("sparqlDir");
        boolean contactSources = Boolean.parseBoolean(config.getProperty("contactsources"));
        //String tt = path + queryResultsPath + "TimeTable";
        HashMap<String, String> constants
                               = Main.loadConstants(config.getProperty("constants"));
        //BufferedWriter timetable = new BufferedWriter(new FileWriter(tt, true));
        Catalog catalog = Main.loadCatalog(config, path, n3Dir, sparqlDir, contactSources);
        execute(sparqlQuery, path, queryResultsPath, n3Dir, 
                /*timetable, */groundTruthPath, /*, relViewsTimer*/q, ms, constants, 
                catalog);
	}

    private static void execute(String sparqlQuery,
                                String PATH, String QUERY_RESULTS_PATH, String n3Dir, 
                                /*BufferedWriter timetable,*/ String GT_PATH,/*, 
                                Timer relViewsTimer*/ConjunctiveQuery cq, 
                                ArrayList<ConjunctiveQuery> ms, HashMap<String, String> 
                                constants, Catalog catalog) throws Exception {
    
        HashSet<ArrayList<String>> solutionsGathered = new HashSet<ArrayList<String>>();
        Model graphUnion = ModelFactory.createDefaultModel();
        String dir = PATH + QUERY_RESULTS_PATH +"NOTHING";
        Main.makeNewDir(dir);
        Query q = Main.readQuery(sparqlQuery);
        int n = q.getTriples().size();
        BufferedWriter info = new BufferedWriter(new FileWriter(dir + "/throughput", true));
        info.write("# File Id\tNumber of views considered\tWrapper Time (milliseconds)\tGraph Creation Time (milliseconds)\tExecution Time (milliseconds)\tTotal Time (milliseconds)\tGraph Size (statements)");
        info.newLine();
        info.flush();
        BufferedWriter info2 = new BufferedWriter(new FileWriter(dir + "/newRVi", true));
        info2.write("# File Id\tNumber of views considered\tWrapper Time (milliseconds)\tGraph Creation Time (milliseconds)\tExecution Time (milliseconds)\tTotal Time (milliseconds)\tGraph Size (statements)");
        info2.newLine();
        info2.flush();
        Timer numberTimer = new Timer();
        Timer wrapperTimer = new Timer();
        Timer graphCreationTimer = new Timer();
        Timer executionTimer = new Timer();
        Counter ids = new Counter();
        numberTimer.start();

        final PipedOutputStream[] outArray = new PipedOutputStream[n];
        final PipedInputStream[] inArray = new PipedInputStream[n];
        for (int i = 0; i < n; i++) {

            outArray[i] = new PipedOutputStream();
            inArray[i] = new PipedInputStream(outArray[i]);
        }
        Thread tRelViews = new RelevantViewsSelector2(outArray, cq, ms, constants);
        tRelViews.start();
        Counter includedViews = new Counter();
        //Thread tinput = new IncludingStreamV(in, graphUnion, includedViews, PATH+n3Dir, ".n3");
        Thread tinput = new IncludingStreamV2(inArray, graphUnion, includedViews, catalog, constants, wrapperTimer, graphCreationTimer, executionTimer, numberTimer, info2, ids);
        tinput.start();

        Thread tquery = new QueryingStream(graphUnion, null, q, 
                            solutionsGathered, executionTimer, numberTimer, includedViews, info, dir, wrapperTimer, graphCreationTimer, ids);
        tquery.start();

        tRelViews.join();
        tinput.join();
        //System.out.println(includedViews.getValue()+" views have been included");
        tquery.interrupt();
        tquery.join();
        for (int i = 0; i < n; i++) {
            inArray[i].close();
        }
        info.flush();
        info.close();
        info2.flush();
        info2.close();
        numberTimer.stop();
    }

    public static void replace (ArrayList<String> list, String prevArg, String newArg) {
    
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(prevArg)) {
                list.set(i, newArg);
            }
        }
    }
    
    public static ArrayList<String> getMapping (ConjunctiveQuery v, Predicate g, 
                                                HashMap<String, String> constants) { 
    	//System.out.println("looking mapping for subgoal "+g+". view: "+v);
    	ArrayList<Predicate> body = v.getBody();
        for (Predicate p : body) {
        	//System.out.println("in view, consider subgoal: "+p);
            if (p.getName().equals(g.getName())
                   && (p.getArguments().size() == g.getArguments().size())) {
                boolean okay = true;
                ArrayList<String> mapping = (ArrayList<String>) v.getHead().getArguments().clone();
                for (int i = 0; i < p.getArguments().size(); i++) {
                    String argV = p.getArguments().get(i);
                    String argQ = g.getArguments().get(i);
                	//System.out.println("argV: "+argV+". argQ: "+argQ);
                    // This a restriction for GUN, if it is existential, it can not be
                    // included in the UNion Graph
                    if (!v.isDistinguished(argV) && !constants.containsKey(argV)) {
                        okay = false;
                    } else if (constants.containsKey(argQ)) {
                        // Only replacing variable by constant has sense in GUN
                        replace(mapping, argV, argQ);
                    }
                }
                if (okay) {
                	//System.out.println("okay! including mapping "+mapping+" for view "+v+" with subgoal of view "+p);
                    return mapping;
                }
            }
        }
        return null;
    }

    // This version of RelevantViewsSelector intends to consider arguments in
    // the covering of views
    private static class RelevantViewsSelector2 extends Thread {

        OutputStream[] oss;
        ConjunctiveQuery q;
        ArrayList<ConjunctiveQuery> ms;
        HashMap<String, String> cs;

        public RelevantViewsSelector2(OutputStream[] oss, ConjunctiveQuery q, 
                                     ArrayList<ConjunctiveQuery> ms, HashMap<String, String> cs) {
            this.oss = oss;
            this.q = q;
            this.ms = ms;
            this.cs = cs;
        }

        private static boolean ready(int[] array, int n) {

            boolean r = true;
            for (int i = 0; i < array.length && r; i++) {
                r = array[i] >= n;
            }
            return r;
        }

        public void run () {
          try {     
            int n = oss.length;     
            OutputStreamWriter[] osws = new OutputStreamWriter[n];//= new OutputStreamWriter(os);
            BufferedWriter[] bws = new BufferedWriter[n];//= new BufferedWriter(osw);
            for (int i = 0; i < n; i++) {
                osws[i] = new OutputStreamWriter(oss[i]);
                bws[i] = new BufferedWriter(osws[i]);
            }
            int[] currentMapping = new int[n];
            for (int i = 0; i < n; i++) {
                currentMapping[i] = 0;
            }
            HashMap<Predicate,ArrayList<Predicate>> buckets = new HashMap<Predicate, ArrayList<Predicate>>();
            for (Predicate p : q.getBody()) {
                ArrayList<Predicate> b = new ArrayList<Predicate>();
                buckets.put(p, b);
            }
            int selectedViews = 0;
            while (!ready(currentMapping, ms.size())) {
                ArrayList<Predicate> body = q.getBody();
                for (int i = 0; i < body.size(); i++) { //Predicate p : q.getBody()) {
                    Predicate p = body.get(i);
                    ArrayList<Predicate> b = buckets.get(p);
                    int ncm = currentMapping[i];
                    for (int j = currentMapping[i]; j < ms.size(); j++) {
                        ConjunctiveQuery v = ms.get(j);
                        ncm = j + 1;
                        ArrayList<String> mapping = getMapping(v, p, cs);
                        if (mapping != null) { // is it possible to cover this subgoal using this view?
                            Predicate vi = v.getHead().replace(mapping);
                            Predicate toInclude = include(b, vi, cs);
                            if ( toInclude != null ) {
                                selectedViews++;
                                bws[i].write(vi+"\n");
                                bws[i].flush();
                                break;
                            }
                        }
                    }
                    currentMapping[i] = ncm;
                }
                //System.out.println(selectedViews+" views had been selected!");
            }
            for (int i = 0; i < n; i++) {
                Predicate vi = new Predicate("end()");
                bws[i].write(vi+"\n");
                bws[i].flush();
                bws[i].close();
                osws[i].close();
                oss[i].close();
            }
            //System.out.println("buckets: " + buckets);
/*
            ArrayList<ArrayList<Predicate>> res = new ArrayList<ArrayList<Predicate>>();
            for (int i = 0; i < n; i++) {
                res.add(new ArrayList<Predicate>());
            }
            boolean ready = allEmpty(buckets);
            while (!ready) {
       
                HashSet<Predicate> toRemove = new HashSet<Predicate>();
                //System.out.println("buckets: " + buckets);
                for (Predicate g : buckets.keySet()) {
                    int k = q.getBody().indexOf(g);
                    ArrayList<Predicate> views = buckets.get(g);
                    if (views.size() == 1) {
                        toRemove.add(g);
                    }
                    Predicate v = views.remove(0);

                    include(res.get(k), v, cs);
                }
                //System.out.println("toRemove: "+toRemove);
                for (Predicate v : toRemove) {
                    buckets.remove(v);
                }


                ready = buckets.isEmpty();
            }
            //for (int i = 0; i < n; i++) {
            //    System.out.println("res "+i+": "+res.get(i));
            //}
            for (int i = 0; i < n; i++) {
                ArrayList<Predicate> r = res.get(i);
                //System.out.println("Going to include "+r.size()+" views");
                int j = 0;
                for (Predicate v : r) {
                    //System.out.println("Including..l:224");
                    bws[i].write(v+"\n");
                    //System.out.println("Including..l:226");
                    bws[i].flush();
                    //System.out.println("Having included "+(++j)+" views");
                }
                //System.out.println("Including..l:229");
                bws[i].close();
                //System.out.println("Including..l:231");
                osws[i].close();
                //System.out.println("Including..l:233");
                oss[i].close();
                //System.out.println("finish view selection. "+res.get(i).size()+" views for subgoal "+i);
            }*/
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
    }

    private static boolean weaker(String argA, String argB, HashMap<String, String> cs) {
    
    	return cs.containsKey(argA) && (!cs.containsKey(argB) || !argA.equals(argB));
    }
    
    private static Predicate include(ArrayList<Predicate> res, Predicate v, HashMap<String, String> cs) {
    
    	int vsize = v.getArguments().size();
    	ArrayList<String> argsV = v.getArguments();
    	boolean included = false;
        for (int i = 0; i < res.size(); i++) {
    		Predicate iv = res.get(i);
        	ArrayList<String> argsIV = iv.getArguments();
    		if (iv.getName().equals(v.getName()) && (iv.getArguments().size() == vsize)) {
    		    boolean coversIVV = true; // the included view covers view?
    		    boolean coversVIV = true; // the view covers included view?
    		    for (int j = 0; j < vsize; j++) {
    		    	
    		    	if (weaker(argsV.get(j), argsIV.get(j), cs)) {
    		    		coversVIV = false;
    		    	}
    		    	if (weaker(argsIV.get(j), argsV.get(j), cs)) {
    		    		coversIVV = false;
    		    	}
    		    }
    		    if (coversIVV) {
    		    	return null;
    		    } else if (coversVIV && !included) {
    		    	res.set(i, v);
    		    	included = true;
    		    } else if (coversVIV && included) {
    		    	res.remove(i);
    		    	i--;
    		    }
    	    }
    	}
        if (!included) {
        	res.add(v);
        }
        return v;
    }

    private static class RelevantViewsSelector extends Thread {

        OutputStream os;
        ConjunctiveQuery q;
        ArrayList<ConjunctiveQuery> ms;

        public RelevantViewsSelector(OutputStream os, ConjunctiveQuery q, 
                                     ArrayList<ConjunctiveQuery> ms) {
            this.os = os;
            this.q = q;
            this.ms = ms;
        }

        public void run () {
          try {          
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            HashMap<Predicate,ArrayList<String>> buckets = new HashMap<Predicate, ArrayList<String>>();
            for (Predicate p : q.getBody()) {
                ArrayList<String> b = new ArrayList<String>();
                for (ConjunctiveQuery v : ms) {
                    if (covers(v, p)) {
                        b.add(v.getHead().getName());
                    }
                }
                if (!b.isEmpty()) {
                    buckets.put(p, b);
                }
            }
            //System.out.println("buckets: " + buckets);
            ArrayList<String> res = new ArrayList<String>();
            boolean ready = allEmpty(buckets);
            while (!ready) {
                HashSet<Predicate> toRemove = new HashSet<Predicate>();
                //System.out.println("buckets: " + buckets);
                for (Predicate g : buckets.keySet()) {
                    ArrayList<String> views = buckets.get(g);
                    if (views.size() == 1) {
                        toRemove.add(g);
                    }
                    String v = views.remove(0);

                    if (!res.contains(v)) {
                        res.add(v);
                        bw.write(v+"\n");
                        bw.flush();
                    }
                }
                //System.out.println("toRemove: "+toRemove);
                for (Predicate v : toRemove) {
                    buckets.remove(v);
                }
                ready = buckets.isEmpty();
            }
            //System.out.println("res: "+res);
            bw.close();
            osw.close();
            os.close();
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
    }

    private static <T> boolean allEmpty(HashMap<Predicate, ArrayList<T>> buckets) {

        boolean areEmpty = true;
        for (Predicate g : buckets.keySet()) {
            areEmpty = areEmpty && buckets.get(g).isEmpty();
        }
        return areEmpty;
    }

    private static boolean covers(ConjunctiveQuery v, Predicate g) {
        
        for (Predicate p : v.getBody()) {
            if (p.getName().equals(g.getName()) 
                   && (p.getArguments().size() == g.getArguments().size())) {
                return true;
            }
        }
        return false;
    }

    private static ArrayList<ConjunctiveQuery> loadMappings(String mappingsPath) 
                                     throws FileNotFoundException, ParseException {

        FileInputStream fis = new FileInputStream(mappingsPath);
        ConjunctiveQueryParser qp = new ConjunctiveQueryParser(fis);
        ArrayList<ConjunctiveQuery> ms = qp.ParseMappings();
        return ms;
    }

    private static ConjunctiveQuery loadCQuery(String queryPath) throws Exception {
        
        FileInputStream fis = new FileInputStream(queryPath);
        ConjunctiveQueryParser qp = new ConjunctiveQueryParser(fis);
        ConjunctiveQuery q = qp.ParseConjunctiveQuery();
        return q;
    }
}
