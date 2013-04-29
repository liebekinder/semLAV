package experimentseswc;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class JenaTDBStore implements Store {
	
	private Dataset dataset;
	private String dir="/home/gabriela/database";
    private static int i = 2;
	
	public JenaTDBStore() {
        dir = dir+i;
		File d = new File(dir);
		System.out.println("mkdir: "+d.mkdir());
		dataset = TDBFactory.createDataset(dir) ;
        i++;
		//TDB.getContext().set(TDB.symUnionDefaultGraph, true);
	}

    public JenaTDBStore(String d) {

        dir = d;
        dataset = TDBFactory.createDataset(dir);
    }
	
	public void loadIn(String fileName) {
		//String name = fileName.substring(fileName.lastIndexOf('/'), fileName.lastIndexOf('.'));
		//Model m = dataset.getNamedModel("http://datasets/"+name) ;
		Model m = dataset.getDefaultModel(); 
		FileManager.get().readModel(m, fileName);
		//m.read(fileName);
	}
	
	public /*Iterator<ArrayList<String>>*/Iterator<QuerySolution> executeSelect(String query) { 

		return new SolutionsIter(query);
	}

	public Iterator<ArrayList<String>> executeSelect(String query, ArrayList<String> vs) { 

		return new SolutionsIter2(query, vs);
	}

    public void executeConstruct(String query, JenaTDBStore source) {
        Model mSource = source.dataset.getDefaultModel();
        QueryExecution qe = QueryExecutionFactory.create(query, mSource);
        Model m = dataset.getDefaultModel();
        qe.execConstruct(m);
    }
	
    public void write (OutputStream os, String format) {

        Model m = dataset.getDefaultModel();
        m.write(os, format);
    }

	public long size() {
		long s = dataset.getDefaultModel().size();
		Iterator<String> it = dataset.listNames();
		while (it.hasNext()) {
			Model m = dataset.getNamedModel(it.next());
			s = s + m.size();
		}
		return s;
	}

	public void close() {
		dataset.close();
		File d = new File(dir);
        Main.deleteDir(d);
	}
	
	private class SolutionsIter implements Iterator<QuerySolution/*ArrayList<String>*/> {

		ResultSet rs;
		
		public SolutionsIter(String query) {
			
			QueryExecution result = QueryExecutionFactory.create(query, dataset);
			rs = result.execSelect(); 	
		}

		public boolean hasNext() {
			
			return rs.hasNext();
		}

		public /*ArrayList<String>*/QuerySolution next() {
			
			QuerySolution binding = rs.nextSolution();

            return binding;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	private class SolutionsIter2 implements Iterator<ArrayList<String>> {

		ResultSet rs;
		ArrayList<String> vars;
		
		public SolutionsIter2(String query, ArrayList<String> vs) {
			
			QueryExecution result = QueryExecutionFactory.create(query, dataset);
			vars = vs;
			rs = result.execSelect(); 	
		}

		public boolean hasNext() {
			
			return rs.hasNext();
		}

		public ArrayList<String> next() {
			
			QuerySolution binding = rs.nextSolution();
			ArrayList<String> s = new ArrayList<String>();
			for (String var : vars) {
				String val = binding.get(var).toString();
				s.add(val);
			}
			return s;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}		
}
