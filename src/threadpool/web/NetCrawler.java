package threadpool.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import threadpool.ThreadPool;

import index.Index;

public class NetCrawler implements Runnable {

	private String site;
	private int crawl_depth;
	private Index index;
	private URL url = null;
	private boolean inside_body = false;
	private boolean terminatedTask = false;
	private HashMap<String, String> evita_repeticoes;
	private LinkedList<NetCrawler> Netcrawlers;

	/**
	 * 
	 * @param site endereco de internet
	 * @param crawl_depth profundidade
	 * @param index é um objecto do tipo Index
	 * @param Netcrawlers LinkedList Netcrawlers
	 * @param evita_repeticoes LinkedList evita_repeticoes
	 */
	public NetCrawler(String site, int crawl_depth, Index index,
			LinkedList<NetCrawler> Netcrawlers,
			HashMap<String, String> evita_repeticoes) {
		this.site = site;
		this.crawl_depth = crawl_depth;
		this.index = index;
		this.evita_repeticoes = evita_repeticoes;
		this.Netcrawlers = Netcrawlers;
	}

	/**
	 * Cria uma conexao com o endereco site.
	 * Verifica ao conexao do endereco se for 200 ou 301 entao é aceite como endereco valido.
	 * Vai percorrer a pagina web linha a linha se estiver dentro do corpo da pagina vai percorrer palavra a palavra.
	 * Vai verificando se a palavra presente e um endereco ou não. Caso não seja verifica se a palavra é aceitavel.
	 * Caso seja aceitavel adiciona ao index.
	 * Por fim mete o terminatedTask a verdadeiro e notifica quem esta à espera.
	 */
	public void run() {
		try {
			url = new URL(site);
		} catch (MalformedURLException e) {
		}
		try {
			int code = ((HttpURLConnection) url.openConnection())
					.getResponseCode();
//			String type = ((HttpURLConnection) url.openConnection())
//					.getContentType();
			// System.out.println(code);
			// System.out.println(type);

			// 200 - ok
			// 301 - permanent redirect to another webpage
			// 400 - bad request
			// 404 - not found
			if (code == 200 || code == 301) {
				URLConnection u = url.openConnection();
				BufferedReader in;
				try {
					in = new BufferedReader(new InputStreamReader(u
							.getInputStream()));
					String inputLine;
					StringTokenizer tokenizer;
					String link = "";
					// System.out.println(site);
					while ((inputLine = in.readLine()) != null) {
						if (inputLine.startsWith("<body"))
							inside_body = true;
						if (inputLine.startsWith("</body"))
							inside_body = false;
						if (inside_body) {
							tokenizer = new StringTokenizer(inputLine);
							while (tokenizer.hasMoreTokens()) {
								String token = tokenizer.nextToken();
								if (token.startsWith("href=")) {
									if (token.charAt(5) == '"')
										link = getSite(token, "\"");
									if (token.charAt(5) == '\'')
										link = getSite(token, "'");
									if (crawl_depth > 0) {
										if (link.startsWith("http://")
												&& !link.endsWith(".jsp"))
											if (ScannedLinks(link))
												NewLink(link);
										if (!link.startsWith("http://")
												&& link.endsWith(".jsp"))
											if (ScannedLinks(link))
												NewLink(site + link);
									}
								} else {
									token = altera_palavra(token);
									if (palavra_aceitavel(token)) {
										// System.out.println(token);
										index.addSourceForWord(token, url
												.getProtocol()
												+ "://"
												+ url.getHost()
												+ url.getPath());
									}
								}
							}
						}
					}
					in.close();
				} catch (IOException e) {
				}
				// } else
				// System.out.println(url+
				// "ERRO Site nao existe --------------------------------------");
			}
		} catch (IOException e) {
		} finally {
//			System.out.println(url+ "      ********* NetCrawler TERMINA ***********");
			synchronized (this) {
				terminatedTask = true;
				notify();
			}
		}
	}

	/**
	 * Metodo que recebe um endereco e adiciona-o a hashmap evita_repeticoes.
	 * Por fim cria um novo Netcrawler, com o endereco e menus um de crawl_depth, e adiciona-o na
	 * ThreadPool crawlersPool e na LinkedList Netcrawlers 
	 * @param link endereco
	 */
	public void NewLink(String link) {
//		System.out.println(link);
		synchronized (evita_repeticoes) {
			evita_repeticoes.put(link, link);
		}
		NetCrawler c = new NetCrawler(link, crawl_depth - 1, index,
				Netcrawlers, evita_repeticoes);
		ThreadPool.crawlersPool.submit(c);
		Netcrawlers.add(c);
	}

	/**
	 * Metodo para retirar as aspas ou plicas do endereco recebido
	 * @param token String com um endereco entre aspas ou plicas
	 * @param separador String com uma aspa ou uma plica conformo o que estiver em token
	 * @return um endereco
	 */
	public String getSite(String token, String separador) {
		int inicio = token.indexOf(separador);
		int fim = token.lastIndexOf(separador);
		return token.substring(inicio + 1, fim);
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

	/**
	 * Verifica se existe um ponto, virgula, ponto exclamação ou ponto de interrogação no final da palavra
	 * caso exista retira-o da palavra
	 * @param word String com a palavra recolhida
	 * @return word retorna a palavra sem os pontos
	 */
	public String altera_palavra(String word) {
		if (word.endsWith(".") || word.endsWith(",") || word.endsWith("?")
				|| word.endsWith("!"))
			word = word.substring(0, word.length() - 1);
		return word;
	}

	/**
	 * Metodo sincronizado que tem como funcao garantir que o programa não avance antes que todos
	 * os Thread's acabem as suas funções
	 */
	public synchronized void waitFinishing() {
		while (!terminatedTask) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Metodo sincronizado que recebe um endereco e verifica se este existe na HashMap evita_repeticoes
	 * caso exista retorna false, caso contrario verdadeiro
	 * @param link endereco de internet
	 * @return falso se encontrar link em evita_repeticoes, caso contrario verdadeiro
	 */
	public synchronized boolean ScannedLinks(String link) {
		// System.out.println(evita_repeticoes.toString());
		if (evita_repeticoes.containsKey(link)) {
//			System.out.println(link+ "--------------------------------------- Site repetido --------------------------------------");
			return false;
		}
		return true;
	}
}