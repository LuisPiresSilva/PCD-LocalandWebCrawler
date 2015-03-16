package index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Hashtable;

public class HashTableIndex implements Index {
	Hashtable<String, HashSet<String>> hashTable = new Hashtable<String, HashSet<String>>();
	int numLinks = 0;

	public int getNumLinks() {
		return numLinks;
	}


	public synchronized HashSet<String> findSourcesForWord(String word) {
		return (HashSet<String>) hashTable.get(word.toLowerCase());
	}

	public synchronized void addSourceForWord(String word, String source) {
		HashSet<String> currentSources = findSourcesForWord(word);
		if (currentSources == null)
			currentSources = new HashSet<String>();

		currentSources.add(source);
		hashTable.put(word.toLowerCase(), currentSources);
		numLinks++;

	}

	public void reset() {
		this.hashTable = new Hashtable<String, HashSet<String>>();
		numLinks=0;
	}

	public void load(File file) {

		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					file));
			try {
				hashTable = (Hashtable<String, HashSet<String>>) in
						.readObject();
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

	public void save(File file) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(new File(file.getAbsolutePath()
							+ Index.DEFAULT_FILE_EXTENSION)));
			out.writeObject(hashTable);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void join(File file) {
		//não implementado porque não é a interface usada
		
	}

}
