package threadpool.disk;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;

import threadpool.ThreadPool;
import threadpool.disk.Crawler;

import index.Index;

public class DiskCrawler {

	private String path;
	private int crawl_depth;
	
	private Index index;
	private LinkedList<Crawler> crawlers;

	/**
	 * 
	 * @param path caminho
	 * @param crawl_depth profundidade
	 * @param index é um objecto do tipo Index
	 * @param crawlers número de crawlers pretendido
	 */
	public DiskCrawler(String path, int crawl_depth,Index index, LinkedList<Crawler> crawlers) {
		this.path = path;
		this.crawl_depth = crawl_depth;
		this.index = index;
		this.crawlers = crawlers;
	}

	/**
	 * Este Metodo é recursivo. Primeiramente cria uma lista com todas as directorias e ficheiros, com extensão txt, do caminho recebido.
	 * Depois se esta lista existir, ou seja o caminho era válido, este entra num ciclo recursivo.
	 * Se encontar uma directoria entra nela, se encontrar um ficheiro cria um novo Crawler com esse ficheiro e adiciona esse mesmo Crawler
	 * à LinkedList crawlers.
	 * Vai fazendo estes dois procedimentos recursivamente até completar a lista.
	 * @param path caminho
	 * @param crawl_depth profundidade
	 */
	public void crawl(String path, int crawl_depth) {
		File directory = new File(path);
		File[] lista = directory.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory()
						|| (file.isFile() && file.getName().endsWith(".txt"));
			}
		});

		if (lista != null) {
			for (int i = 0; i < lista.length; i++) {
				if (lista[i].isDirectory()) {
					if (crawl_depth > 0) {
//						System.out.println("                     Foi encontrada uma directoria : "+ lista[i]);
						crawl(lista[i].getAbsolutePath(), crawl_depth - 1);
					}
				} else {
					Crawler c = new Crawler(lista[i], index);
					ThreadPool.crawlersPool.submit(c);
					crawlers.add(c);
				}
			}
		}

	}
}