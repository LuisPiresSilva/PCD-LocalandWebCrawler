package query;

import index.Index;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelQuery extends AbstractQuery {

	HashSet<String> results = null;
	Index index;
	public int num_results;

	ExecutorService threadPool = Executors.newFixedThreadPool(10);
	LinkedList<QueryWordTask> queryWordTasks = new LinkedList<QueryWordTask>();

	public int getNumResults(){
		return num_results;
	}
	
	public ParallelQuery(Index index, String queryString) {
		super(queryString);
		this.index = index;

		StringTokenizer tokenizer = new StringTokenizer(queryString);

		if (tokenizer.countTokens() == 0) {
			results = null;
		} else {
			while (tokenizer.hasMoreTokens()) {
				QueryWordTask qt = new QueryWordTask(tokenizer.nextToken());
				threadPool.execute(qt);
				queryWordTasks.add(qt);

			}
		}
	}

	class QueryWordTask implements Runnable {

		boolean terminatedResults = false;
		String word;
		HashSet<String> taskResults = null;

		public QueryWordTask(String word) {
			this.word = word;
		}

		public synchronized HashSet<String> waitForResults() {
			try {
				while (!terminatedResults) {
					wait();
				}
			} catch (InterruptedException e) {
			}
			return taskResults;
		}

		public void run() {

			synchronized (this) {
				try {
					taskResults = index.findSourcesForWord(word);
				} finally {
					terminatedResults = true;
					notify();
				}
			}

		}

	}

	public HashSet<String> getResults() {
		if (queryWordTasks.size() == 0)
			return null;


		Iterator<QueryWordTask> i = queryWordTasks.iterator();

		results = i.next().waitForResults();

		if (results == null) {
			return null;
		}

		while (i.hasNext() && !results.isEmpty()) {
			HashSet<String> newResults = i.next().waitForResults();
			if (newResults != null) {
				results.retainAll(newResults);
			} else {
				results.clear();
			}
		}
		num_results = results.size();
		return results;
	}

}
