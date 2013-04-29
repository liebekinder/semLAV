/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentseswc;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.FileManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author gonzalez-l
 */
public class Joins {

        //ToDO : tests de todos estos joins.
        // Que conmuten, que asocien


     // m JOIN n, donde la condición del join es
    // m.subject = n.subject
    // Ahora parece que no es necesario/correcto
    // Creo que usaríamos este si usasemos constantes.

     public static Model joinByUri(Model m, Model n){

        HashSet<String> urisM = new HashSet<String>();
        HashSet<String> urisN = new HashSet<String>();
        String extractURIS = "Select DISTINCT ?uri "
                + "WHERE { ?uri ?y ?z .}"
                ;
        QueryExecution qem = QueryExecutionFactory.create(extractURIS, m);

        for (ResultSet rs = qem.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             urisM.add(binding.get("?uri").toString());
        }

        QueryExecution qen = QueryExecutionFactory.create(extractURIS, n);

        for (ResultSet rs = qen.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             urisN.add(binding.get("?uri").toString());
        }

        HashSet<String> uriIntersect = new HashSet<String>(urisM);
        uriIntersect.retainAll(urisN);
        if(uriIntersect.isEmpty()){
            return ModelFactory.createDefaultModel();
        }

        Model joinResult = ModelFactory.createDefaultModel();
        //System.out.println(uriIntersect.toString());

        for(String uri: uriIntersect){
            String retrieveTriples = "CONSTRUCT { <"+ uri +"> ?y ?z }"
            //String retrieveTriples = "SELECT ?x ?y ?z "
                    + "WHERE{ <" + uri + "> ?y ?z .}";
            qem = QueryExecutionFactory.create(retrieveTriples, m);
            qen = QueryExecutionFactory.create(retrieveTriples, n);
           /* for (ResultSet rs = qem.execSelect(); rs.hasNext();) {
                QuerySolution binding = rs.nextSolution();
                System.out.println(binding.get("x")+" "+binding.get("y")+" "+binding.get("z"));
            }*/

            qem.execConstruct(joinResult);
            qen.execConstruct(joinResult);
        }

        return joinResult;
    }


    /*
     * Use a fixed ingredient for the subClassOf Comparison
     */
    public static Model joinByIngredientFix(Model m, Model n, Reasoner reasoner, String QueryIngredient){

        String extractIngs= "SELECT DISTINCT ?ing "
                + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing .}"
                ;
        HashSet<String> ingsM = new HashSet<String>();

        QueryExecution qeingm = QueryExecutionFactory.create(extractIngs, m);

        for (ResultSet rs = qeingm.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             ingsM.add(binding.get("?ing").toString());
            // System.out.println("FLAG "+binding.get("?ing").toString());
        }

        HashSet<String> ingsN = new HashSet<String>();

        QueryExecution qeingn = QueryExecutionFactory.create(extractIngs, n);

        for (ResultSet rs = qeingn.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             ingsN.add(binding.get("?ing").toString());
            // System.out.println("FLAG "+binding.get("?ing").toString());
        }

        InfModel minfer = ModelFactory.createInfModel (reasoner, m);
        InfModel ninfer = ModelFactory.createInfModel (reasoner, n);

        HashSet<String> ingsSubClassM = new HashSet<String>();

        for(String i : ingsM){
            String askSubClass =
                    "ASK { <"+i+"> <http://www.w3.org/2000/01/rdf-schema#subClassOf> "
                        + "<"+ QueryIngredient+"> .}";
            QueryExecution q = QueryExecutionFactory.create(askSubClass,minfer);
           // System.out.println(askSubClass);
            if (q.execAsk()){
               // System.out.println(i);
                ingsSubClassM.add(i);
            }
        }

        HashSet<String> ingsSubClassN = new HashSet<String>();

        for(String i : ingsN){
            String askSubClass =
                    "ASK {<"+i+"> <http://www.w3.org/2000/01/rdf-schema#subClassOf> "
                        + "<"+ QueryIngredient+"> .}";
            QueryExecution q = QueryExecutionFactory.create(askSubClass,ninfer);
           // System.out.println(askSubClass);
            if (q.execAsk()){
                //System.out.println(i);
                ingsSubClassN.add(i);
            }
        }

        Model joinResult = ModelFactory.createDefaultModel();

        if(ingsSubClassN.isEmpty() || ingsSubClassM.isEmpty()) {
            return joinResult;
        }

        HashSet<String> ings = new HashSet<String>(ingsSubClassM);
        ings.addAll(ingsSubClassN);

        for(String i : ings){
                 String retrieveTriples = "CONSTRUCT {?x ?y ?ing ."
                        + "                             ?x ?y ?z .} "
                        + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing ."
                            + "     ?ing <http://www.w3.org/2000/01/rdf-schema#subClassOf> <"+ i+"> ."
                            + "     ?x ?y ?z .}"
                        ;
                QueryExecution qen = QueryExecutionFactory.create(retrieveTriples, ninfer);
                qen.execConstruct(joinResult);

                QueryExecution qem = QueryExecutionFactory.create(retrieveTriples, minfer);
                qem.execConstruct(joinResult);
        }



//        for(String ing: ingredients){
//
//                String askTriples = "ASK "
//                        + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing ."
//                            + "     ?ing <http://www.w3.org/2000/01/rdf-schema#subClassOf> <"+ ing+"> .}"
//                        ;
//                QueryExecution qen = QueryExecutionFactory.create(askTriples, n);
//                System.out.println(askTriples);
//               // System.out.println(qen.execAsk());
//                if(qen.execAsk()){
//
//                String retrieveTriples = "CONSTRUCT {?x ?y ?ing ."
//                        + "                             ?x ?y ?z .} "
//                        + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing ."
//                            + "     ?ing <http://www.w3.org/2000/01/rdf-schema#subClassOf> <"+ ing+"> ."
//                            + "     ?x ?y ?z .}"
//                        ;
//
//                qen = QueryExecutionFactory.create(retrieveTriples, n);
//                qen.execConstruct(joinResult);
//
//                QueryExecution qem = QueryExecutionFactory.create(retrieveTriples, m);
//                    qem.execConstruct(joinResult);
//
//                }
//
//        }

        return joinResult;
    }




/* m JOIN n donde la condicion del JOIN es;
 * m.predicate = n. predicate = HasIngredient AND
 * n.object subClassOf m.object AND
 * Nótese que:
 * 1) Esto es superconjunto de la condición
 * m.predicate = n. predicate = HasIngredient AND
 * n.object = m.object
 * 2) Como subClassOf no conmuta, este join tampoco conmuta.
 * 3) Requiere que los modelos tengan un razonador activo, en el caso
 * de nuestro experimento, MicroOWL hará el trabajo.
 * Si QueryIngredient diferente de vacío, estamos fijando I
 *
 *
 */
    public static Model joinByIngredientSem(Model m, Model n){

        String extractIngs= "SELECT DISTINCT ?ing "
                + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing .}"
                ;
        HashSet<String> ingredients = new HashSet<String>();

        QueryExecution qeing = QueryExecutionFactory.create(extractIngs, m);

        for (ResultSet rs = qeing.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             ingredients.add(binding.get("?ing").toString());
            // System.out.println("FLAG "+binding.get("?ing").toString());
        }

        Model joinResult = ModelFactory.createDefaultModel();

        for(String ing: ingredients){

                String askTriples = "ASK "
                        + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing ."
                            + "     ?ing <http://www.w3.org/2000/01/rdf-schema#subClassOf> <"+ ing+"> .}"
                        ;
                QueryExecution qen = QueryExecutionFactory.create(askTriples, n);
                System.out.println(askTriples);
               // System.out.println(qen.execAsk());
                if(qen.execAsk()){

                String retrieveTriples = "CONSTRUCT {?x ?y ?ing ."
                        + "                             ?x ?y ?z .} "
                        + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing ."
                            + "     ?ing <http://www.w3.org/2000/01/rdf-schema#subClassOf> <"+ ing+"> ."
                            + "     ?x ?y ?z .}"
                        ;

                qen = QueryExecutionFactory.create(retrieveTriples, n);
                qen.execConstruct(joinResult);

                QueryExecution qem = QueryExecutionFactory.create(retrieveTriples, m);
                    qem.execConstruct(joinResult);

                }

        }

        return joinResult;
    }

    /* m JOIN n donde la condicion del JOIN es;
     * m.predicate = n. predicate = a AND
     * m.object subClassOf recipe AND
     * n.object subClassOf recipe
     * Nótese que:
     * 1) Esto es superconjunto de la condición
     * m.predicate = n. predicate = HasIngredient AND
     * m.object = n.object
     * 2) Como subClassOf no conmuta, este join tampoco conmuta.
     * 3) Requiere que los modelos tengan un razonador activo, en el caso
     * de nuestro experimento, MicroOWL hará el trabajo.
     *
    */

    public static Model joinByRecipeSem(Model m, Model n){

        String extractRecs= "SELECT DISTINCT ?rec "
                + "WHERE { ?x a ?rec ."
                + "         ?rec a <http://localhost/menuontology.owl/#Recipe>}"
                ;
        HashSet<String> recipes = new HashSet<String>();

        QueryExecution qerec = QueryExecutionFactory.create(extractRecs, m);

        for (ResultSet rs = qerec.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             recipes.add(binding.get("?rec").toString());
        }

        Model joinResult = ModelFactory.createDefaultModel();

        for(String rec: recipes){


                String retrieveTriples = "CONSTRUCT {?x ?y ?rec} "
                        + "WHERE { ?x a ?rec ."
                            + "     ?rec a "+ rec +" .}"
                        ;

                QueryExecution qen = QueryExecutionFactory.create(retrieveTriples, n);
                Model tmp = qen.execConstruct(joinResult);
                if (!joinResult.isIsomorphicWith(tmp)){
                    QueryExecution qem = QueryExecutionFactory.create(retrieveTriples, m);
                    qem.execConstruct(joinResult);
                }

        }

        return joinResult;


    }

    // m JOIN n, donde la condición del join es
    // m.predicate = n.predicate = has_ingredient AND
    // m.subject = n.subject
    // Ahora parece que no es necesario/correcto
    // Creo que usaríamos este si usasemos constantes.

    public static Model joinBySameIng(Model m, Model n){

        HashSet<String> ingsM = new HashSet<String>();
        HashSet<String> ingsN = new HashSet<String>();
        String extractIngs = "Select DISTINCT ?ing "
                + "WHERE { ?x <http://localhost/menuontology.owl/#hasIngredient> ?ing ."
                + "         }"
                ;
        QueryExecution qem = QueryExecutionFactory.create(extractIngs, m);

        for (ResultSet rs = qem.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             ingsM.add(binding.get("?uri").toString());
        }

        QueryExecution qen = QueryExecutionFactory.create(extractIngs, n);

        for (ResultSet rs = qen.execSelect() ; rs.hasNext() ; ) {
            QuerySolution binding = rs.nextSolution();
             ingsN.add(binding.get("?uri").toString());
        }

        HashSet<String> ingIntersect = new HashSet<String>(ingsM);
        ingIntersect.retainAll(ingsN);
        if(ingIntersect.isEmpty()){
            return ModelFactory.createDefaultModel();
        }

        Model joinResult = ModelFactory.createDefaultModel();
        System.out.println(ingIntersect.toString());

        for(String ing: ingIntersect){
            String retrieveTriples = "CONSTRUCT { ?x ?y ?z }"
            //String retrieveTriples = "SELECT ?x ?y ?z "
                    + "WHERE{ ?x <http://localhost/menuontology.owl/#hasIngredient>"+ ing +".}";
            qem = QueryExecutionFactory.create(retrieveTriples, m);
            qen = QueryExecutionFactory.create(retrieveTriples, n);
           /* for (ResultSet rs = qem.execSelect(); rs.hasNext();) {
                QuerySolution binding = rs.nextSolution();
                System.out.println(binding.get("x")+" "+binding.get("y")+" "+binding.get("z"));
            }*/

            qem.execConstruct(joinResult);
            qen.execConstruct(joinResult);
        }

        return joinResult;
    }


   
     public Model join(Model m, Model n, OntModel ontology, String queryIngredient){
	  return m;
     }
}
