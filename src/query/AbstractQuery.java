package query;

import java.util.HashSet;

public abstract class AbstractQuery {
	private String queryString;

	public AbstractQuery(String queryString) {
		this.queryString = queryString;
	}

	public abstract HashSet<String> getResults();
	
	public abstract int getNumResults();
	
	public String toString(){
		StringBuffer sb=new StringBuffer("Query: '" + queryString + "'\n");
	
		HashSet<String> results = getResults();
		if(results==null)
			sb.append("  <null>\n");
		else if (results.isEmpty())
			sb.append("  <empty>  <-- depois tirar isto\nHits:0");
		else{
			for(String s : results){
				sb.append("  " + s + "\n");
			}
			sb.append("Hits: " + results.size() + "\n");
		}
		return sb.toString();
	}
}
