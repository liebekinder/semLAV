package experimentseswc;

import java.util.ArrayList;
import java.util.Iterator;

public interface Store {
	
	public void loadIn(String fileName);
	
	public Iterator<ArrayList<String>> executeSelect(String query, ArrayList<String> vs);

    //public Iterator<QuerySolution> executeSelect(String query);

    //public void executeConstruct(String query, Store source);

	public long size();
	
	public void close();
}
