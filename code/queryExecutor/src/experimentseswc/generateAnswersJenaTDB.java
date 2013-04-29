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
import java.util.Iterator;
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

class generateAnswersJenaTDB {

    public static void main(String args[]) throws Exception {

        Properties config = Main.loadConfiguration("configData.properties");
        String queriesFolder = config.getProperty("queriesFolder");
        String sparqlViewsFolder = config.getProperty("sparqlViewsFolder");
        String n3ViewsFolder = config.getProperty("n3ViewsFolder");
        String answersFolder = config.getProperty("answersFolder");

        if (answersFolder != null) {
            /*Main.makeNewDir(answersFolder);*/
            JenaTDBStore res = new JenaTDBStore("/home/gabriela/database2");
            //Model m = ModelFactory.createDefaultModel();
            /*File f = new File(n3ViewsFolder);
            File[] content = f.listFiles();
            if (content != null) {
                for (File g : content) {
                    if (g.isFile()) {
                        //m = loadViewData(g, m);
                        String filePath = g.getAbsolutePath();
                        res.loadIn(filePath);
                    }
                }
            }*/
            System.out.println("data loaded");
            File f = new File(queriesFolder);
            File[] content = f.listFiles();
            if (content != null) {
                for (File g : content) {
                    if (g.isFile()) {
                        saveQueryAnswer(res, g, answersFolder);
                    }
                }
            }
            res.close();
        }
    }

    public static Model loadViewData(File g, Model m) {
        String fileName = g.getAbsolutePath();
        m = FileManager.get().readModel(m, fileName);
        return m;
    }

    public static void saveQueryAnswer(JenaTDBStore res, /*Model res,*/ File g, String queryAnswers) {

        try {
            String fileName = g.getAbsolutePath();
            String name = g.getName();
            int i = name.lastIndexOf(".");
            name = name.substring(0, i);
            FileInputStream fis = new FileInputStream(fileName);
            QueryParser qp = new QueryParser(fis);
            Query q = qp.ParseSparql();

            Iterator<QuerySolution> results = res.executeSelect(q.toString());

            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(queryAnswers+"/"+name), 
                                                         "UTF-8"));

            while (results.hasNext()) {
                QuerySolution binding = results.next();
                ArrayList<String> s = new ArrayList<String>();
                for (String var : q.getVars()) {
                    String val = binding.get(var).toString();
                    s.add(val);
                }
                output.write(s.toString());
                output.newLine();                
            }
            output.flush();
            output.close();         
        }  catch (Exception e) {
			e.printStackTrace(System.out);
        }
    }
}
