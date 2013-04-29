package experimentseswc;

import java.util.*;
import java.io.*;

public class calculateRelevantViews {

    public static void main(String[] args) throws Exception {

        try {
            Properties config = Main.loadConfiguration(System.getProperty("user.dir")
                                                  +"/configC.properties");
            final String PATH = config.getProperty("path");
            final String mappings = config.getProperty("mappings");
            final String conjunctiveQuery = config.getProperty("conjunctiveQuery");
            final String REWRITING = config.getProperty("rewritings");
            calculateRVs(PATH, mappings, conjunctiveQuery, REWRITING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void calculateRVs(String path, String mappings, String conjunctiveQuery, String rewritings) throws Exception {

        ArrayList<Rewrite> solution = Main.getSolutions(path, rewritings);
        HashSet<String> relevantViews = new HashSet<String>();
        System.out.println("number of rewritings: "+solution.size());
        System.out.println("k\trelevantViews.size()\trelevantViews\tnumberRWsin500\tnumberRWs");
        for (int k = 1; k <= solution.size(); k++) {
            boolean modified = false;
            Rewrite r = solution.get(k-1);
            for(Predicate p : r.getGoals()){
                if (relevantViews.add(p.getName())) {
                    modified = true;
                }
            }
            if (modified) {
                int numberRWsin500 = getNumRWs(relevantViews, solution);
                int numberRWs = getNumRWs(relevantViews, path, mappings, conjunctiveQuery);
                System.out.println(k+"\t"+relevantViews.size()+"\t"+relevantViews+"\t"+numberRWsin500+"\t"+numberRWs);
            }
        }
    }

    public static int getNumRWs(HashSet<String> relevantViews, ArrayList<Rewrite> solution) {

        int n = 0;
        for (Rewrite r : solution) {
            if (allIn(r, relevantViews)) {
                n++;
            }
        }
        return n;
    }

    public static boolean allIn(Rewrite r, HashSet<String> relevantViews) {

        for(Predicate p : r.getGoals()){
            if(!relevantViews.contains(p.getName())) {
                return false;
            }
        }
        return true;
    }

    public static int getNumRWs(HashSet<String> relevantViews, String path, 
                                String mappings, String conjunctiveQuery) throws Exception {
        String codePath = System.getProperty("user.dir");
        codePath = codePath.substring(0, codePath.lastIndexOf("/queryExecutor/src"));
        makeMappingFile(path, mappings, "2", relevantViews);
        List<String> l = new ArrayList<String>();
        l.add("timeout");
        l.add(3+"m");
        l.add(codePath + "/ssdsat/driver.py");
        l.add("-t");
        l.add("RW");
        l.add("-v");
        l.add(path + mappings+"2");
        l.add("-q");
        l.add(conjunctiveQuery);
        ProcessBuilder pb = new ProcessBuilder(l);
        Process p = pb.start();
        InputStream is = p.getInputStream();
        InputStream es = p.getErrorStream();
        Counter rewritings = new Counter();
        Thread tinput = new CountingStream(is, rewritings);
        Thread terror = new IgnoringStream(es);
        terror.setPriority(Thread.MIN_PRIORITY);
        tinput.start();
        terror.start();
        int exitValue = p.waitFor();
        tinput.join();
        is.close();
        es.close();
        return rewritings.getValue();
    }

    public static void makeMappingFile(String path, String mappings, String end, HashSet<String> relevantViews) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(path+mappings));
            String l = br.readLine();

            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(path+mappings+end), 
                                                         "UTF-8"));
            while (l != null) {
                int p = l.indexOf("(");
                String name = l.substring(0, p);
                if (relevantViews.contains(name)) {
                    output.write(l);
                    output.newLine();
                }
                l = br.readLine();
            }
            output.flush();
            output.close();
            //Main.deleteDir(new File(path+mappings+end));
        }  catch (Exception e) {
			e.printStackTrace(System.out);
        }
    }
}
