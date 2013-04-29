package experimentseswc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.InputStream;
import java.io.FileInputStream;

class TestQueryParser {

    public static void main(String args[]) throws Exception {

        String fileName = args[0];
        FileInputStream fis = new FileInputStream(fileName);
        QueryParser qp = new QueryParser(fis);
        Query q = qp.ParseSparql();
        System.out.println(q);
    }
}
