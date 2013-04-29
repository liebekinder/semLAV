package experimentseswc;

import java.util.*;
import java.io.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.util.concurrent.TimeUnit;

public class testGC {

    public static void main (String[] args) throws Exception {
        Properties config = Main.loadConfiguration(System.getProperty("user.dir")
                                                  +"/configC.properties");
        final String PATH = config.getProperty("path");
        final String N3 = config.getProperty("mappingsn3");
        final String SPARQL = config.getProperty("mappingssparql");
        boolean contactSources = Boolean.parseBoolean(config.getProperty("contactsources"));
        Catalog catalog = Main.loadCatalog(config, PATH, N3, SPARQL, contactSources);
        String fileName = args[0];
        HashMap<String, String> constants = Main.loadConstants(config.getProperty("constants"));
        ArrayList<String> views = loadFile(fileName);
        Timer wrapperTimer = new Timer();
        Model graphUnion = ModelFactory.createDefaultModel();
        for (String v : views) {
            Predicate p = new Predicate(v);
            wrapperTimer.resume();
            Model m = catalog.getModel(p, constants);
            wrapperTimer.stop();
            graphUnion.add(m);
        }
        System.out.println("Wrapper time: "+TimeUnit.MILLISECONDS.toSeconds(wrapperTimer.getTotalTime()));
    }

    public static ArrayList<String> loadFile(String fileName) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String l = br.readLine();
        ArrayList<String> views = new ArrayList<String>();

        while (l != null) {
            views.add(l);
            l = br.readLine();
        }
        br.close();

        return views;
    }
}
