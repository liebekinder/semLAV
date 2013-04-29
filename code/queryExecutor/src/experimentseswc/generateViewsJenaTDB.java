package experimentseswc;
import com.hp.hpl.jena.rdf.model.InfModel;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.io.File;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import java.io.OutputStreamWriter;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.util.HashSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.Iterator;

class generateViewsJenaTDB {

    public static void main(String args[]) throws Exception {

        Properties config = Main.loadConfiguration("configData.properties");
        String dataSetFile = config.getProperty("dataSetFile");
        String indexesFolder = config.getProperty("indexesFolder");
        String queriesFolder = config.getProperty("queriesFolder");
        int factor = Integer.parseInt(config.getProperty("factor"));
        String sparqlViewsFolder = config.getProperty("sparqlViewsFolder");
        String n3ViewsFolder = config.getProperty("n3ViewsFolder");
        String n3OntologyFile = config.getProperty("n3OntologyFile");
        String ontologyFile = config.getProperty("ontologyFile");

        if (sparqlViewsFolder != null && n3ViewsFolder != null) {
            Main.makeNewDir(sparqlViewsFolder);
            Main.makeNewDir(n3ViewsFolder);
            System.out.println("ready to load model");
            JenaTDBStore res = new JenaTDBStore("/home/gabriela/database1");
            //res.loadIn(dataSetFile);
            System.out.println("model loaded");
            Reasoner reasoner = null;

            File f = new File(indexesFolder);
            File[] content = f.listFiles();
            if (content != null) {
                for (File g : content) {
                    if (g.isFile()) {
                        extractIndexData(res, g, n3ViewsFolder, reasoner,
                                         sparqlViewsFolder, factor);
                    }
                }
            }
            res.close();
        }
    }

    public static void createOntologyFile(Model m, String n3FileName, 
                                          String fileName) throws Exception {

        Property p = m.createProperty("http://www.w3.org/2000/01/rdf-schema#","subClassOf");
        Resource s = null; 
        RDFNode o = null; 
        Selector ss = new SimpleSelector(s, p, o);       
        Model sc = m.query(ss);
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                      new FileOutputStream(fileName), "UTF-8"));
        StmtIterator it = sc.listStatements();
        while (it.hasNext()) {
            Statement st = it.nextStatement();
            String r = Query.convert(st.getSubject());
            String n = Query.convert(st.getObject());
            String c = "type(X, "+Triple.transform(r)+") <= type(X, "
                        +Triple.transform(n)+")";
            output.write(c);
            output.newLine();
        }
        output.flush();
        output.close();
        p = m.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#","type");
        ss = new SimpleSelector(s, p, o);       
        Model t = m.query(ss);
        Model r = t.union(sc);
        OutputStream out = new FileOutputStream(n3FileName);
        r.write(out, "N-TRIPLE");
    }

    public static void extractIndexData(JenaTDBStore res, File g, String viewsDataset, 
                                        Reasoner reasoner, String viewsFolder, int f) {

        try {
            String fileName = g.getAbsolutePath();
            String name = g.getName();
            int i = name.lastIndexOf(".");
            name = name.substring(0, i);
            //System.out.println(fileName);
            FileInputStream fis = new FileInputStream(fileName);
            QueryParser qp = new QueryParser(fis);
            Query q = qp.ParseSparql();
            //System.out.println(q.toString());
            HashSet<QuerySolution> solutions = new HashSet<QuerySolution>();

            Iterator<QuerySolution> results = res.executeSelect(q.toString());
            //System.out.println("the inference model is ready");

            //System.out.println("the query execution is ready");
            JenaTDBStore result[] = new JenaTDBStore[f];

            for (i = 0; i < f; i++) {
                result[i] = new JenaTDBStore();
            }
            //System.out.println("the empty models are ready");

            //System.out.println("the select is ready");
            i = 0;
            ArrayList<String> vs = q.getVars();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                //System.out.println(solution);
                String qConstruct = q.getConstruct(Leaf.getMap(solution));
                result[i].executeConstruct(qConstruct, res);
                i = (i+1)%f;
            }
            for (i = 0; i < f; i++) {
                if (result[i].size()> 0) {
                    OutputStream out = new FileOutputStream(viewsDataset+"/"+name+"_"+i+".n3");
                    result[i].write(out, "N-TRIPLE");
                    copy(fileName, viewsFolder, i);
                }
                result[i].close();
            }
        }  catch (Exception e) {
			e.printStackTrace(System.out);
        }
    }

    public static void copy(String name, String viewsFolder, int k) throws Exception {
        int i = name.lastIndexOf(".");
        int j = name.lastIndexOf("/");
        String oldName = name.substring(j+1, i);
        int p = oldName.indexOf("w");
        String end = "_"+oldName.substring(p+1)+"_"+k;
        String newName = viewsFolder+"/"+oldName+"_"+k+name.substring(i);
        BufferedReader br = new BufferedReader(new FileReader(name));
        String l = br.readLine();
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(newName), 
                                                         "UTF-8"));
        while (l != null) {
            String m = processLine(l, end);
            output.write(m);
            output.newLine();
            l = br.readLine();
        }
        output.flush();
        output.close(); 
        br.close();
    }

    public static String processLine(String l, String end) {

        StringTokenizer st = new StringTokenizer(l, " \t\n\r\f:{}.", true);
        String ns = "";

        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            char c = t.charAt(0);
            if ((c == '?') || (c == '$')) {
                t = t + end;
            }
            ns = ns + t;
        }
        return ns;
    }
}
