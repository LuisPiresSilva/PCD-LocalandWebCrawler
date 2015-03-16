package query;

import java.util.HashSet;
import java.util.StringTokenizer;

import index.Index;

public class SequentialQuery extends AbstractQuery {

	Index index;
	String queryString;
	HashSet<String> results;
	int num_results;
	
	public SequentialQuery(Index index, String queryString) {
		super(queryString);
		this.index = index;

		StringTokenizer tokenizer = new StringTokenizer(queryString);

		if (tokenizer.countTokens() == 0) {
			results = null;
		} else {
			results = index.findSourcesForWord(tokenizer.nextToken());
		}
		if (results != null)
			while (tokenizer.hasMoreTokens() && !results.isEmpty()) {
				HashSet<String> partialResults = index.findSourcesForWord(tokenizer.nextToken());
				if (partialResults != null)
					results.retainAll(partialResults);
				else
					results.clear();
			}

		num_results = results.size();
	}
	
	public HashSet<String> getResults() {
		return results;
	}

	
	public int getNumResults() {
		return num_results;
	}
}
