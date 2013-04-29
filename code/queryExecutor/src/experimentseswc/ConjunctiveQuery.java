package experimentseswc;

import java.util.ArrayList;

public class ConjunctiveQuery {
	
	private Predicate head;
	private ArrayList<Predicate> body;
	
	public ConjunctiveQuery (Predicate h, ArrayList<Predicate> b) {
		
		this.setHead(h);
		this.setBody(b);
	}

	public Predicate getHead() {
		return head;
	}
	
	public void replace(String prevArg, String newArg) {
		
		this.head.replace(prevArg, newArg);
		for (Predicate p : body) {			
			p.replace(prevArg, newArg);
		}
	}
	
	public boolean isDistinguished (String var) {
		
		ArrayList<String> args = this.head.getArguments();
		for (String a : args) {
			
			if (a.equals(var)) {
				return true;
			}
		}
		return false;
	}

	public void setHead(Predicate head) {
		this.head = head;
	}

	public ArrayList<Predicate> getBody() {
		return body;
	}

	public void setBody(ArrayList<Predicate> body) {
		this.body = body;
	}
	
	public String toString() {
		
		String s = "";
		if (this.head != null) {
			s = s + this.head.toString();
		}
		s = s + ":-";
		for (Predicate p : this.body) {
			s = s + p.toString() + ", ";
		}
		s = s.substring(0, s.length()-2);
		return s;
	}
}