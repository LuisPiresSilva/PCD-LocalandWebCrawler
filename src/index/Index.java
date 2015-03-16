package index;

import java.io.File;
import java.util.HashSet;

public interface Index {
	public HashSet<String> findSourcesForWord(String words);

	public void addSourceForWord(String word, String source);

	public int getNumLinks();

	public void load(File file);

	public void save(File file);

	public void reset();

	public final String DEFAULT_FILE_EXTENSION = ".idx";

	public void join(File file);

}
