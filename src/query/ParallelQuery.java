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
				String temp = tokenizer.nextToken();
				if(palavra_aceitavel(temp)){
					QueryWordTask qt = new QueryWordTask(temp);
					threadPool.execute(qt);
					queryWordTasks.add(qt);
				}

			}
		}
	}
	
	/**
	 * Verifica se a palavra é aceitavel, ou seja, caso a palavra tenha entre 3 e 15 caracteres
	 * e apenas contenha letras de a - z é aceitavel.
	 * @param word String com a palavra recolhida
	 * @return verdadeiro se a palavra for aceitavel, falso caso contrário
	 */
	public boolean palavra_aceitavel(String word) {
		if (word != null && word.length() > 3 && word.length() < 15) {
			for (int i = 0; i < word.length(); i++)
				if (!(word.charAt(i) >= 'a' && word.charAt(i) <= 'z'))
					return false;
			return true;
		}
		return false;
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
