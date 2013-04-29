/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentseswc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static ch.lambdaj.Lambda.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matchers.*;

/**
 *
 * @author gonzalez-l
 */
public class Rewrite {

	Predicate head;
	ArrayList<Predicate> goals;


	public Rewrite(Predicate h, ArrayList<Predicate> g){
		this.head = h;
		this.goals = g;

	}

	// Construct a Rewrite from a String of the form
	// Head(X,Y..) :- SG(X,Y..),SG(W,Z)...
	public Rewrite(String str){
		String[] split = str.split(" :- ");
		this.head = new Predicate(split[0]);
		this.goals = new ArrayList<Predicate>();
        Pattern goal = Pattern.compile("\\w+\\([\\w[,]]+\\)");
        Matcher match = goal.matcher(split[1]);
		while(match.find()){
			this.goals.add(new Predicate(match.group()));
		}
	}

	public ArrayList<Predicate> getGoals(){
		return this.goals;
	}

	// return true if all the vars are different
	// No instantiation of X = Y in thsi RW
	public boolean diffVar(){

		return head.getArguments().size() == head.getArgumentsSet().size();
	}

	// Check if two pred names, goal1 and goal2 are going to be Joined
	// by the same variable in this rewrite
	public boolean checkJoinByVar(String goal1, String goal2, String var){


		if(!exists(this.goals, having(on(Predicate.class).getName(),org.hamcrest.Matchers.equalTo(goal1)))
			|| !exists(this.goals, having(on(Predicate.class).getName(),org.hamcrest.Matchers.equalTo(goal2))))
				{return false;	}

		ArrayList<Predicate> g1 = new ArrayList<Predicate>(select(this.goals,having(on(Predicate.class).getName(), org.hamcrest.Matchers.equalTo(goal1))));
		ArrayList<Predicate> g2 = new ArrayList<Predicate>(select(this.goals,having(on(Predicate.class).getName(), org.hamcrest.Matchers.equalTo(goal2))));

		for(Predicate p : g1 ){
			for (Predicate q: g2){
				if(p.getArguments().contains(var)
						&& q.getArguments().contains(var)){
					return true; }
			}
		}

		 return false;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Rewrite){
			Rewrite r = (Rewrite) obj;
			return this.head.equals(r.head) && this.goals.equals(r.goals);
		}else {return false;}
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 19 * hash + (this.head != null ? this.head.hashCode() : 0);
		hash = 19 * hash + (this.goals != null ? this.goals.hashCode() : 0);
		return hash;
	}


	@Override
	public String toString(){
		String str = "";
		str = str.concat(this.head.toString());
		str = str.concat(" :- ");
		for(Predicate goal : goals){
			str = str.concat(goal.toString());
			str = str.concat(",");

		}
		// Return without the last extra comma
		return str.substring(0,str.length()-1);

	}

	public int getPredicateNumber(){
		return this.goals.size();
	}

	// Returns true if the Rewrite has a join for the
	public boolean hasJoinBy(String variable){
		List<Predicate> predsWithVar = select(this.goals, having(on(Predicate.class).getArguments() , org.hamcrest.Matchers.equalTo(variable)));
		if(predsWithVar.size() > 1){
			return true;
		} else {
			return false;
		}
	}

	// Map from vars in the head to
	public HashMap<String,Set<String>> varTable(){

		HashMap <String,Set<String>> table = new HashMap<String,Set<String>>();
		for(String var: this.head.getArguments()){
			List<Predicate> predlist =	select(this.goals,having(on(Predicate.class).isVar(var)));
			HashSet<String> nameset = new HashSet<String>();
			for(Predicate p: predlist){
				nameset.add(p.getName());
			}
			table.put(var,nameset);
		}

		return table;

	}

    // like varTable, but considers the position of the arguments
	public HashMap<String,Set<Pair>> varTable2(){

		HashMap <String,Set<Pair>> table = new HashMap<String,Set<Pair>>();
		for(String var: this.head.getArguments()){
            HashSet<Pair> set = new HashSet<Pair>();
            for (Predicate p : this.goals) {
                for (int i = 0; i < p.getArguments().size(); i++) {
                    if (p.getArguments().get(i).equals(var)) {
                        set.add(new Pair(i, p.getName()));
                    }
                }
            }
			table.put(var,set);
		}

		return table;

	}

	// like equivalenceByJoin, but considers the position of the arguments
	public boolean equivalentByJoin2(Rewrite R){
		if(this.equivalent(R)){
			return this.varTable2().equals(R.varTable2());
		}else { 
			return false;
		}
	}

    // like equivalenceClassesJoin, but considers the position of the arguments
	public static ArrayList<ArrayList<Rewrite>> equivalenceClassesJoin2 (List<Rewrite> original){

		ArrayList<ArrayList<Rewrite>> eqclasses = new ArrayList<ArrayList<Rewrite>>();
		ArrayList<Rewrite> cla1 = new ArrayList<Rewrite>();
		Rewrite first = original.get(0);
		cla1.add(first);
		eqclasses.add(cla1);
		for(int i =1 ; i< original.size(); i++){
			Rewrite elem = original.get(i);
			int k = 0;
			for( ; k< eqclasses.size();k++){
				ArrayList<Rewrite> clazz = eqclasses.get(k);
				Rewrite repr = clazz.get(0);
				//if(elem.equivalent(repr)){
				if(elem.equivalentByJoin2(repr)){
					clazz.add(elem);
					break;
				}
			}
			if (k == eqclasses.size()){
				ArrayList<Rewrite> cla = new ArrayList<Rewrite>();
				cla.add(elem);
				eqclasses.add(cla);
			}
		}

		return eqclasses;

	}

	// Strong equivalence version, rewrites needs to have the same var Table
	// to be equivalent

	public boolean equivalentByJoin(Rewrite R){
		if(this.equivalent(R)){
			return this.varTable().equals(R.varTable());
		}else { 
			return false;
		}
	}

	// Equivalence class by Set of Predicates used
	public boolean equivalent(Rewrite R){
		if(!this.head.equals(R.head)){
			return false;
		}else {

			HashSet<String> namesthis = new HashSet<String>();
			HashSet<String> namesthat = new HashSet<String>();
			for(Predicate p : this.goals){
				namesthis.add(p.getName());
			}
			for(Predicate p : R.goals){
				namesthat.add(p.getName());
			}
			return namesthis.equals(namesthat);

		}
	}

	public static ArrayList<ArrayList<Rewrite>> equivalenceClasses (List<Rewrite> original){

		ArrayList<ArrayList<Rewrite>> eqclasses = new ArrayList<ArrayList<Rewrite>>();
		ArrayList<Rewrite> cla1 = new ArrayList<Rewrite>();
		Rewrite first = original.get(0);
		cla1.add(first);
		eqclasses.add(cla1);
		for(int i =1 ; i< original.size(); i++){
			Rewrite elem = original.get(i);
			int k = 0;
			for( ; k< eqclasses.size();k++){
				ArrayList<Rewrite> clazz = eqclasses.get(k);
				Rewrite repr = clazz.get(0);
				if(elem.equivalent(repr)){
					clazz.add(elem);
					break;
				}
			}
			if (k == eqclasses.size()){
				ArrayList<Rewrite> cla = new ArrayList<Rewrite>();
				cla.add(elem);
				eqclasses.add(cla);
			}
		}

		return eqclasses;

	}

	public static ArrayList<ArrayList<Rewrite>> equivalenceClassesJoin (List<Rewrite> original){

		ArrayList<ArrayList<Rewrite>> eqclasses = new ArrayList<ArrayList<Rewrite>>();
		ArrayList<Rewrite> cla1 = new ArrayList<Rewrite>();
		Rewrite first = original.get(0);
		cla1.add(first);
		eqclasses.add(cla1);
		for(int i =1 ; i< original.size(); i++){
			Rewrite elem = original.get(i);
			int k = 0;
			for( ; k< eqclasses.size();k++){
				ArrayList<Rewrite> clazz = eqclasses.get(k);
				Rewrite repr = clazz.get(0);
				//if(elem.equivalent(repr)){
				if(elem.equivalentByJoin(repr)){
					clazz.add(elem);
					break;
				}
			}
			if (k == eqclasses.size()){
				ArrayList<Rewrite> cla = new ArrayList<Rewrite>();
				cla.add(elem);
				eqclasses.add(cla);
			}
		}

		return eqclasses;

	}
	// Returns the model result of the union of th execution of all subgoals
	// in this rewrite, according to the Catalog passed
	public Model union(Catalog c, Set<String> loadedViews, Timer wrapperTimer, 
			           Timer graphCreationTimer, HashMap<String, String> constants){
		Model union = ModelFactory.createDefaultModel();

		//Timer callTimer = new Timer();
		//Timer unionTimer = new Timer();
		for(Predicate g: goals){
		//	callTimer.resume();
            if(!loadedViews.contains(g.getName())){
            graphCreationTimer.stop();
            wrapperTimer.resume();
			Model temp = c.getModel(g, constants);
			wrapperTimer.stop();
			graphCreationTimer.resume();
		//	callTimer.stop();
		//	unionTimer.resume();
			union = union.union(temp);
            }
		//	unionTimer.stop();
		}
		/*
		System.out.println("Time to retrieving data " 
			+ (callTimer.getTotalTime())
			);
		System.out.println("Time to perform this union "
			+ (unionTimer.getTotalTime())
			);
		 *
		 */

		return union;

	}

    public String getSparqlQuery(Catalog c, HashMap<String, String> constants) {

        String[] result = new String[6];
        result[0] = "";
        result[1] = "";
        result[2] = "";
        result[3] = "";
        result[4] = "";
        result[5] = "";
		for(Predicate g: goals){
            Query q = c.getQuery(g, constants);
            String[] ss = q.getStrings();
            result[0] = result[0] + ss[0];
            result[1] = ss[1];
            result[2] = result[2] + ss[2];
            result[3] = ss[3];
            result[4] = result[4] + ss[4];
            result[5] = ss[5];
        }     
        String s = "";
        for (String e : result) {
            s = s + e;
        }
        return s;
    }
}




