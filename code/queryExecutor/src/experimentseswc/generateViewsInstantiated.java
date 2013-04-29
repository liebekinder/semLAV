package experimentseswc;

import java.util.*;
import java.io.*;

import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

public class generateViewsInstantiated {

    public static void main (String[] args) {

        try {
            Properties config = Main.loadConfiguration("configData.properties");
            String sparqlViewsFolder = config.getProperty("sparqlViewsFolder");
            String n3ViewsFolder = config.getProperty("n3ViewsFolder");
            final HashMap<String, String> constants 
                               = Main.loadConstants(config.getProperty("constantsFile"));
            String viewsFile = args[0];
            BufferedReader br = new BufferedReader(new FileReader(viewsFile));
            String l = br.readLine();
            while (l != null) {
                processLine(l, sparqlViewsFolder, n3ViewsFolder, constants);
                l = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void processLine(String l, String sparqlViewsFolder, 
                                   String n3ViewsFolder, HashMap<String, String> constants) throws Exception {

        StringTokenizer st = new StringTokenizer(l, " \t\n\r\f(,)", false);
        HashMap<Integer, String> cttes = new HashMap<Integer, String>();
        if (st.hasMoreTokens()) {
            String vn = st.nextToken();
            int p = 0;
            while (st.hasMoreTokens()) {
                String a = st.nextToken();
                char c = a.charAt(0);
                if ((Character.isLetter(c) && Character.isLowerCase(c))|| Character.isDigit(c)) {
                    cttes.put(p, a);
                }
                p++;
            }
            Model res = FileManager.get().loadModel(n3ViewsFolder+"/"+vn+".n3");
            String fileName = sparqlViewsFolder + "/" + vn + ".sparql";
            FileInputStream fis = new FileInputStream(fileName);
            QueryParser qp = new QueryParser(fis);
            Query q = qp.ParseSparql();
            includeInstantiations(q, cttes, constants);
            String outputName = getOutputName(l);
            HashSet<QuerySolution> solutions = new HashSet<QuerySolution>();

            QueryExecution queryExec = QueryExecutionFactory.create(q.toString(),
                                                                    res);

            Model result = ModelFactory.createDefaultModel();
            ResultSet rs = queryExec.execSelect();
            while (rs.hasNext()) {
                QuerySolution solution = rs.nextSolution();
                String qConstruct = q.getConstruct(Leaf.getMap(solution));
                QueryExecution qem = QueryExecutionFactory.create(qConstruct, res);
                qem.execConstruct(result);
            }
            //if (result.size()> 0) {
                OutputStream out = new FileOutputStream(n3ViewsFolder+"/"+outputName+".n3");
                result.write(out, "N-TRIPLE");
                out.close();
                create(sparqlViewsFolder, outputName, q);
            //}
        }
    }

    public static void create(String sparqlViewsFolder, String outputName, Query q) throws Exception {
        String newName = sparqlViewsFolder+"/"+outputName+".sparql";
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(newName),
                                                         "UTF-8"));
        output.write(q.toString());
        output.flush();
        output.close();
    }

    public static String getOutputName(String v) {

        Predicate p = new Predicate(v);
        ArrayList<String> args = p.getArguments();
        String name = p.getName();
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            char c = a.charAt(0);
            if ((Character.isLetter(c) && Character.isLowerCase(c))|| Character.isDigit(c)) {
                name = name + "_" + a;
            } else {
                name = name + "_V";
            }
        }
        
        return name;
    }

    public static void includeInstantiations(Query q, HashMap<Integer, String> cttes, 
                                             HashMap<String, String> constants) {

        ArrayList<String> vars = q.getVars();
        ArrayList<String> newVars = new ArrayList<String>();
        for (int i = 0; i < vars.size(); i++) {
            if (cttes.containsKey(i)) {
                String constantC = cttes.get(i);
                String constantRDF = constants.get(constantC);
                q.addBGP(new Filter(new BinaryExpression("=", new UnaryExpression(constantRDF), new UnaryExpression(vars.get(i)))));
            } else {
                newVars.add(vars.get(i));
            }
        }
        q.setVariables(newVars);
    }
}
