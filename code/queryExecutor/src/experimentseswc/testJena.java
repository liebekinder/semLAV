package experimentseswc;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;

class testJena {

    public static void main(String[] args) {

        String queryName = args[0];
        String queryString = read(queryName);
        Query q = QueryFactory.create(queryString);
    }

    public static String read(String queryName) {
        return "";
    }
}
