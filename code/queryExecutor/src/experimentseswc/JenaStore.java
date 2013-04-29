package experimentseswc;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class JenaStore implements Store {
	
	private Model graph;
	
	public JenaStore() {
		graph = ModelFactory.createDefaultModel();
	}
	
	public void loadIn(String fileName) {
		
		graph = FileManager.get().readModel(graph, fileName);
	}
	
	public Iterator<ArrayList<String>> executeSelect(String query, ArrayList<String> vs) { 

		return new SolutionsIter(query, vs); 
	}
	
	public long size() {
		
		return graph.size();
	}
	
	public void close() {
	 
	    graph.close();
	}

	private class SolutionsIter implements Iterator<ArrayList<String>> {

		ResultSet rs;
		ArrayList<String> vars;
		
		public SolutionsIter(String query, ArrayList<String> vs) {
			
			QueryExecution result = QueryExecutionFactory.create(query, graph);
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