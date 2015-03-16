package index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TrieTreeIndex implements Index,Serializable {	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TrieTreeIndex(){
		
	}
	Node rootNode = new Node();
	
	private AtomicInteger numLinks = new AtomicInteger(0);

	public class Node implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		HashSet<String> sources = new HashSet<String>();
		Node[] filhos = new Node[26];
		transient ReentrantLock acessoFilhos = new ReentrantLock();
		transient ReentrantLock acessoSources = new ReentrantLock();
		
	
		void addSourcesForWord(String word, String source) {
			if (word.length() == 0) {
				// inserir source
				acessoSources.lock();
				if(!sources.contains(source)){
				sources.add(source);
				numLinks.incrementAndGet();//só se foi de facto introduzido
				}
				acessoSources.unlock();
			} else {
				acessoFilhos.lock();
				if (filhos[(int) word.charAt(0) - 'a'] != null) {
					acessoFilhos.unlock();
					// quer dizer que tem filho com essa letra, continua para lá
					filhos[(int) word.charAt(0) - 'a'].addSourcesForWord(word
							.substring(1), source);
				} else {
					// não tem filho com essa letra, cria esse nó nessa pos da
					// matriz e continua para lá
					filhos[(int) word.charAt(0) - 'a'] = new Node();
					acessoFilhos.unlock();
					filhos[(int) word.charAt(0) - 'a'].addSourcesForWord(word
							.substring(1), source);
				}

			}

		}

		HashSet<String> findSourcesForWord(String word) {
			if (word.length() == 0) {
				// palavra encontrada
				return new HashSet<String>(sources);
			} else {
				// ainda n foi encontrada
				if (filhos[(int) word.charAt(0) - 'a'] != null) {
					// tem filho com a letra seguinte, continua para lá
					return filhos[(int) word.charAt(0) - 'a']
							.findSourcesForWord(word.substring(1));
				} else {
					// n tem filho com a letra seguinte, return null termina
					// pesquisa
					return null;
				}
			}
			
			
		}

		private void join(String word, Node root){
			for(int i=0; i<root.filhos.length; i++){
				if(root.filhos[i]!=null){
					join(word.concat(((char)('a'+i))+""), root.filhos[i]);
				}
			}
			if(root.sources.size()>0){
				for(String s : root.sources){
					rootNode.addSourcesForWord(word, s);
				}
			}
		}
		
		public String toString() {
			String buffer = new String("");
			for (int i = 0; i < filhos.length; i++) {
				if (filhos[i] != null)
					buffer += " " + ((char) ('a' + i)) + "("
							+ filhos[i].toString() + " )";
			}

			return buffer;

		}

	}


	public HashSet<String> findSourcesForWord(String word) {
		return rootNode.findSourcesForWord(word.toLowerCase());
	}

	public void addSourceForWord(String word, String source) {
		rootNode.addSourcesForWord(word.toLowerCase(), source);
	}

	public int getNumLinks() {
		return numLinks.get();
	}

	public void load(File file) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			try {
				rootNode = (Node) in.readObject();
				in.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		rootNode = new Node();
		numLinks.set(0);
	}

	public void save(File file) {
		try {
			ObjectOutputStream out;
			if(file.getName().endsWith(Index.DEFAULT_FILE_EXTENSION)){
				out = new ObjectOutputStream(
						new FileOutputStream(new File(file.getAbsolutePath())));
			}else{
			out = new ObjectOutputStream(
					new FileOutputStream(new File(file.getAbsolutePath()+ Index.DEFAULT_FILE_EXTENSION)));
			}
			out.writeObject(rootNode);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void join(File file){
		Node root;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
			try {
				root = (Node) in.readObject();
				in.close();
				rootNode.join("", root);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public String toString() {
		return rootNode.toString();
	}




}