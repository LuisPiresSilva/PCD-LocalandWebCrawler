package threadpool.disk;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import index.Index;

public class Crawler extends Thread {

	private Index index;
	private File file;
	private boolean terminatedTask = false;
	
	/**
	 * Contrutor usado para criar este objecto.
	 * Neste caso um Thread
	 * @param file � um ficheiro
	 * @param index � um objecto do tipo Index
	 */
	public Crawler(File file, Index index) {
		this.index = index;
		this.file = file;
	}

	/**
	 * Abre o ficheiro file em modo de leitura. Percorre-o palavra a palavra.
	 * Se a palavra for aceitavel mete-a no index
	 * E finalmente depois de percorrer o ficheiro todo mete o boolean terminatedTask a verdadeiro
	 * e notifica quem estiver a espera que acabou as suas fun��es.
	 * 
	 */
	public void run() {
		try {
			Scanner leitura = new Scanner(file);
			while (leitura.hasNext()) {
				String temp = leitura.next().toLowerCase();
				temp = altera_palavra(temp);
				if (palavra_aceitavel(temp))
					index.addSourceForWord(temp, file.getAbsolutePath());
			}
			leitura.close();
//			System.out.println(Thread.currentThread().getName()+ "      ***********CRAWLER TERMINA***********");
		} catch (FileNotFoundException e) {
		} finally {
			synchronized(this){
			terminatedTask = true;
			notify();
			}
		}

	}

	/**
	 * Verifica se existe um ponto, virgula, ponto exclama��o ou ponto de interroga��o no final da palavra
	 * caso exista retira-o da palavra
	 * @param word String com a palavra recolhida
	 * @return word retorna a palavra sem os pontos
	 */
	public String altera_palavra(String word){
		if (word.endsWith(".") || word.endsWith(",") || word.endsWith("?") || word.endsWith("!"))
			word = word.substring(0, word.length() - 1);
		return word;
	}
	
	/**
	 * Verifica se a palavra � aceitavel, ou seja, caso a palavra tenha entre 3 e 15 caracteres
	 * e apenas contenha letras de a - z � aceitavel.
	 * @param word String com a palavra recolhida
	 * @return verdadeiro se a palavra for aceitavel, falso caso contr�rio
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

	/**
	 * Metodo sincronizado que tem como funcao garantir que o programa n�o avance antes que todos
	 * os Thread's acabem as suas fun��es
	 */
	public synchronized void waitFinishing() {
		while (!terminatedTask) {
			try {
//				System.out.println("espera" + currentThread());
				wait();
			} catch (InterruptedException e) {
			}
		}
	}
}
