package experimentseswc;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class evaluateQuery {
	
	public static void main(String[] args) throws Exception {
		
        String configFile = args[0];
        Properties config = Main.loadConfiguration(configFile);
        String cQueryPath = config.getProperty("cQueryPath");
        String mappingsPath = config.getProperty("mappingsPath");
        ConjunctiveQuery q = loadCQuery(cQueryPath);
        ArrayList<ConjunctiveQuery> ms = loadMappings(mappingsPath);
        Timer relViewsTimer = new Timer();
        relViewsTimer.start();
        ArrayList<String> relevantViews = obtainRelevantViews(q, ms);
        //System.out.println(" "+relevantViews.size()+" relevant views..");
        relViewsTimer.stop();
        String sparqlQuery = config.getProperty("sQueryPath");
        String path = config.getProperty("path");
        String queryResults = config.getProperty("queryResults");		
        String queryResultsPath = queryResults + "/";
        String file = path + queryResultsPath;
        Main.makeNewDir(file);		
        String groundTruthFile = config.getProperty("groundTruth");
        String groundTruthPath = path+groundTruthFile;		
        String n3Dir = config.getProperty("n3Dir");
        String tt = path + queryResultsPath + "TimeTable";
        BufferedWriter timetable = new BufferedWriter(new FileWriter(tt, true));
        execute(sparqlQuery, relevantViews, path, queryResultsPath, n3Dir, 
                timetable, groundTruthPath, relViewsTimer);
	}

    private static void execute(String sparqlQuery, ArrayList<String> relevantViews, 
                                String PATH, String QUERY_RESULTS_PATH, String n3Dir, 
                                BufferedWriter timetable, String GT_PATH, 
                                Timer relViewsTimer) throws Exception {
    
        HashSet<ArrayList<String>> solutionsGathered = new HashSet<ArrayList<String>>();
        Store graphUnion = new JenaStore();
        String dir = PATH + QUERY_RESULTS_PATH +"NOTHING";
        Main.makeNewDir(dir);
        Query q = Main.readQuery(sparqlQuery);
        BufferedWriter info = new BufferedWriter(new FileWriter(dir + "/modelSize", true));
        Timer numberTimer = new Timer();
        numberTimer.start();
        int number = 1;		
        for (String v : relevantViews) {
            numberTimer.stop();					
            System.out.println("Including view: " + v);
            info.write("Including view: " + v);
            info.newLine();
            info.flush();
            numberTimer.resume();
            graphUnion.loadIn(PATH+n3Dir+v+".n3");
            numberTimer.stop();
            info.write("gun model size: " + graphUnion.size());
            info.newLine();
            info.flush();
            Timer evaluation = new Timer();
            evaluation.start();
            Iterator<ArrayList<String>> results = graphUnion.executeSelect(q.toString(), 
                                                                           q.getVars());
            while (results.hasNext()) {
                ArrayList<String> r = results.next();
                solutionsGathered.add(r);
            }
            
            evaluation.stop();
            timetable.write("NOTHING_" + number	+ " "
                       + TimeUnit.MILLISECONDS.toSeconds(relViewsTimer.getTotalTime()) + " "
                       + TimeUnit.MILLISECONDS.toSeconds(numberTimer.getTotalTime()) + "  "
                       + TimeUnit.MILLISECONDS.toSeconds(evaluation.getTotalTime()) + "  "
                       + TimeUnit.MILLISECONDS.toSeconds(relViewsTimer.getTotalTime()
                                                         + numberTimer.getTotalTime()
                                                         + evaluation.getTotalTime()));
            timetable.newLine();
            timetable.flush();

            Main.saveResults(PATH, dir, GT_PATH, number, solutionsGathered);
            number++;
            numberTimer.resume();
        }
        graphUnion.close();
    }

    private static ArrayList<String> obtainRelevantViews(ConjunctiveQuery q,
                                                         ArrayList<ConjunctiveQuery> ms) {
    
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
                }
            }
            //System.out.println("toRemove: "+toRemove);
            for (Predicate v : toRemove) {
                buckets.remove(v);
            }
            ready = buckets.isEmpty();
        }
        //System.out.println("res: "+res);
        return res;
    }

    private static boolean allEmpty(HashMap<Predicate, ArrayList<String>> buckets) {

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
