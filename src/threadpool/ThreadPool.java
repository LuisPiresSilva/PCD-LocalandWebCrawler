package threadpool;

import index.Index;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import threadpool.disk.DiskCrawler;
import threadpool.disk.Crawler;
import threadpool.web.NetCrawler;



public class ThreadPool {

	private String path;
	private int num_crawlers, crawl_depth;
	private Index index;

	/**
	 * 
	 * @param path caminho
	 * @param num_crawlers número de crawlers pretendido
	 * @param crawl_depth depth profundidade
	 * @param index é um objecto do tipo Index
	 */
	public ThreadPool(String path, int num_crawlers, int crawl_depth,
			Index index) {
		this.path = path;
		this.num_crawlers = num_crawlers;
		this.crawl_depth = crawl_depth;
		this.index = index;
	}

	public static ExecutorService crawlersPool;
	LinkedList<Crawler> crawlers;
	LinkedList<NetCrawler> Netcrawlers;
	HashMap<String,String> evita_repeticoes;
	
	/**
	 * Cria um ThreadPoolFixa com tamanho igual ao num_crawlers.
	 * Se o caminho do objecto comecar por "http://" entao é um endereco e este cria uma LinkedList Netcrawlers e evita_repeticoes
	 * e cria um novo NetCrawler.
	 * Cria um iterador do tipo Netcrawlers e vai percorrer a LinkedList NetCrawlers ate ao fim enquanto percorre esta Linkedlist espera 
	 * que o actual thread na LinkedList termine. Isto para garantir que todos terminam.
	 * Se o caminho não comecar por "http://" este cria uma LinkedList crawlers e cria um novo DiskCrawler.
	 * Cria um iterador do tipo crawlers e vai percorrer a LinkedList crawlers ate ao fim enquanto percorre esta Linkedlist espera 
	 * que o actual thread na LinkedList termine. Isto para garantir que todos terminam.
	 */
	public void execute() {
		
		crawlersPool = Executors.newFixedThreadPool(num_crawlers);
		
		if (path.startsWith("http://")){
			Netcrawlers = new LinkedList<NetCrawler>();
			evita_repeticoes = new HashMap<String,String>();
			new NetCrawler(path, crawl_depth, index,Netcrawlers,evita_repeticoes).run();
			
			Iterator<NetCrawler> i = Netcrawlers.iterator();

			if (Netcrawlers.size() > 0) {
				while (i.hasNext()) {
					NetCrawler c = i.next();
					c.waitFinishing();
				}
			}
			
		}else{
			crawlers = new LinkedList<Crawler>();
			new DiskCrawler(path, crawl_depth, index,crawlers).crawl(path, crawl_depth);
			
			Iterator<Crawler> i = crawlers.iterator();

			if (crawlers.size() > 0) {
				while (i.hasNext()) {
					Crawler c = i.next();
					c.waitFinishing();
				}
			}
		}
	}
}
