package gui;

import index.Index;
import index.TrieTreeIndex;
import query.ParallelQuery;
import threadpool.ThreadPool;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PCDGoogle {
	private long tempo;
	private long tempo_final;
	private JFrame frame = new JFrame("PCDGoogle");

	public static final int PORTA = 8080;
	// menu inicio
	// -----------------------------------------------------------------------------------
	private JMenuBar menu_bar = new JMenuBar();
	private JMenu file_menu = new JMenu("File");
	private JMenu index_menu = new JMenu("Index");
	private JMenuItem quit_menu_item = new JMenuItem("Quit");
	private JMenuItem open_menu_item = new JMenuItem("Open...");
	private JMenuItem save_menu_item = new JMenuItem("Save...");
	private JMenuItem reset_index_menu_item = new JMenuItem("Reset Index");
	private JMenuItem join_index_menu_item = new JMenuItem("Join Index");

	private menuButtonListener sentinela = new menuButtonListener();
	// menu fim
	// ---------------------------------------------------------------------------------------

	// painel topo inicio
	// ---------------------------------------------------------------------------
	private JPanel painelT = new JPanel();
	private JTextField query_text = new JTextField(26);
	private JTextField url_text = new JTextField(26);
	private JTextField crawlers_text = new JTextField(2);
	private JTextField crawl_depth_text = new JTextField(2);
	private JButton crawl_button = new JButton("Crawl!");
	// painel topo fim
	// -------------------------------------------------------------------------------

	// painel central inicio
	// -----------------------------------------------------------------------
	private JEditorPane visualizador = new JEditorPane();
	private DefaultListModel modelo_lista = new DefaultListModel();
	private JList lista = new JList(modelo_lista);
	// painel central fim

	// painel de baixo inicio
	// -----------------------------------------------------------------------
	public JTextArea consola = new JTextArea("", 5, 0);
	// painel de baixo fim
	// ---------------------------------------------------------------------------

	Index index;
	ParallelQuery query = null;

	public PCDGoogle() {
		frame.setSize(1000, 700);
		frame.setLocationByPlatform(false);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//implementação com Trie-Tree
		index = new TrieTreeIndex();

		// menu inicio
		// -------------------------------------------------------------------------------
		file_menu.add(quit_menu_item);
		index_menu.add(open_menu_item);
		index_menu.add(save_menu_item);
		index_menu.add(join_index_menu_item);
		index_menu.add(new JSeparator());
		index_menu.add(reset_index_menu_item);

		quit_menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] option = { "Sim", "Não" };

				int resposta = JOptionPane.showOptionDialog(frame,
						"Tem a certeza que quer sair?", "Quit",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, option, option[0]);

				if (resposta == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});

		open_menu_item.addActionListener(sentinela);
		save_menu_item.addActionListener(sentinela);
		join_index_menu_item.addActionListener(sentinela);

		reset_index_menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] option = { "Sim", "Não" };

				int resposta = JOptionPane.showOptionDialog(frame,
						"Tem a certeza que quer fazer reset ao Index?",
						"Reset", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, option, option[0]);

				if (resposta == JOptionPane.YES_OPTION) {
					index.reset();
					visualizador.setText("");
					modelo_lista.clear();
				}

			}
		});

		menu_bar.add(file_menu);
		menu_bar.add(index_menu);
		frame.setJMenuBar(menu_bar);
		// menu fim
		// -----------------------------------------------------------------------------------

		// painel topo inicio
		// -----------------------------------------------------------------------
		painelT.setLayout(new FlowLayout());
		painelT.add(new JLabel("Query text: "));
		painelT.add(query_text);
		painelT.add(new JLabel("URL: "));
		painelT.add(url_text);
		painelT.add(new JLabel("# Crawlers: "));
		painelT.add(crawlers_text);
		painelT.add(new JLabel("Crawl depth: "));
		painelT.add(crawl_depth_text);
		painelT.add(crawl_button);
		frame.getContentPane().add(painelT, BorderLayout.NORTH);

		query_text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					visualizador.setText("");
					modelo_lista.clear();
					if (!query_text.getText().isEmpty()) {
						if (palavraAceitavel(query_text.getText().trim())) {
							query = new ParallelQuery(index, query_text
									.getText().trim());

							if (query.getResults() != null) {
								for (String s : query.getResults())
									modelo_lista.addElement(s);
								consola.append("Hits: " + query.num_results
										+ "\n");
							}
							if (query_text.getText().isEmpty())
								visualizador.setText("Não escreveu nada");
							else if (modelo_lista.isEmpty()) {
								visualizador
										.setText("Não foram encontrados resultados");
								consola.append("Hits: 0\n");
							}
						} else {
							consola
									.append("A pesquisa contém caracteres inválidos: Só caracteres de A a Z são aceitavéis\n");
						}
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
		// painel topo fim
		// ------------------------------------------------------------------------------

		// painel central inicio
		// ----------------------------------------------------------------------
		frame.add(new JScrollPane(visualizador), BorderLayout.CENTER);
		visualizador.setEditable(false);
		frame.add(new JScrollPane(lista), BorderLayout.EAST);

		lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lista.setSelectedIndex(-1);
		lista.setLayoutOrientation(JList.VERTICAL);

		lista.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (lista.getValueIsAdjusting()) {
					if (!modelo_lista.isEmpty() && !lista.isSelectionEmpty()) {
						visualizador.setText("");
						StringBuffer buffer = new StringBuffer("");
						String path = (String) modelo_lista.getElementAt(lista
								.getSelectedIndex());
						if (path.startsWith("http://")) {
							try {
								visualizador.setPage(new URL("" + path));
							} catch (IOException e) {
							}
						} else {
							try {
								Scanner f = new Scanner(new File("" + path));
								while (f.hasNextLine()) {
									buffer.append(f.nextLine() + "\n");
								}
								visualizador.setText(buffer.toString());
								f.close();
							} catch (FileNotFoundException e) {
								consola.append("File not found");
							}
						}
						visualizador.setCaretPosition(0);
					}
				}
			}
		});
		// painel central fim
		// --------------------------------------------------------------------------

		// painel de baixo inicio
		// ----------------------------------------------------------------------
		frame.getContentPane().add(new JScrollPane(consola), BorderLayout.SOUTH);
		// painel de baixo fim
		// --------------------------------------------------------------------------

		crawl_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String path = new String(url_text.getText().trim());
				int num_crawlers = 0;
				int crawl_depth = 0;
				try {
					num_crawlers = Integer.parseInt(crawlers_text.getText()
							.trim());
					crawl_depth = Integer.parseInt(crawl_depth_text.getText()
							.trim());
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Caracteres invalidos");
				}
				if (num_crawlers > 0) {
					crawlService(path, num_crawlers, crawl_depth, index);
				} else {
					JOptionPane.showMessageDialog(null, "Minimo Crawlers: 1");
				}
			}
		});

	}

	boolean someoneIndexing = false;

	public void crawlService(String path, int num_crawlers, int crawl_depth,
			Index index) {

		while (someoneIndexing) {
			try {
				wait();
			} catch (InterruptedException e) {
			} finally {
				someoneIndexing = true;
			}
		}

		try {
			tempo = System.currentTimeMillis();

			new ThreadPool(path, num_crawlers, crawl_depth, index).execute();

//			System.out.println("URL: " + path + "\n\n");

			tempo_final = ((System.currentTimeMillis() - tempo) / 1000);

//			System.out.println("Ended crawling. Links: " + index.getNumLinks());
			consola.append("Ended crawling. Links: " + index.getNumLinks()
					+ "\n");

//			System.out.println("Tempo demorado: " + tempo_final);
			consola.append("Tempo demorado: " + tempo_final + "s\n");

		} finally {
			synchronized (this) {
				someoneIndexing = false;
				notify();
			}
		}

	}

	public boolean palavraAceitavel(String queryString) {
		for (int i = 0; i < queryString.length(); ++i) {
			if ((queryString.charAt(i) < 'a' || queryString.charAt(i) > 'z')
					&& queryString.charAt(i) != ' ')
				return false;
		}
		return true;
	}

	public void init() throws IOException {
		frame.setVisible(true);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(PORTA);

			while (true) {
				Socket socket = serverSocket.accept();
				consola.append("Recebida conexão de cliente: "
						+ socket.getRemoteSocketAddress() + "\n");

				ObjectInputStream clienteSocketIn = new ObjectInputStream(
						socket.getInputStream());

				ObjectOutputStream clienteSocketOut = new ObjectOutputStream(
						socket.getOutputStream());
				new TrataConexao(socket.getRemoteSocketAddress(),
						clienteSocketIn, clienteSocketOut, index, this).start();
			}
		} catch (BindException b) {
			consola.append("Já se encontra um servidor em execução.");
		} finally {
			if (serverSocket != null)
				serverSocket.close();
		}
	}

	public static void main(String[] args) {
		try {
			new PCDGoogle().init();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private class menuButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser jfc = new JFileChooser(".");

			FileNameExtensionFilter filtro = new FileNameExtensionFilter(
					"Index(*.idx)", "idx");
			jfc.setFileFilter(filtro);
			jfc.setAcceptAllFileFilterUsed(false);

			File directoria_saves = new File(jfc.getCurrentDirectory()
					+ "/save");
			jfc.setCurrentDirectory(directoria_saves);

			if (e.getSource() == open_menu_item) {
				Object[] option = { "Sim", "Não" };

				int resposta = JOptionPane
						.showOptionDialog(
								frame,
								"Tem a certeza que quer fazer load de um Index?\n "
										+ "Se estiver a utilizar outro e não o tiver guardado perderá as indexações feitas."
										+ "\n Quer continuar?", "Load",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, option,
								option[0]);
				if (resposta == JOptionPane.YES_OPTION) {
					jfc.setDialogTitle("Open");
					jfc.setDialogType(JFileChooser.OPEN_DIALOG);

					int returnVal = jfc.showOpenDialog(open_menu_item);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						visualizador.setText("");
						modelo_lista.clear();
						index.load(jfc.getSelectedFile());
					}
				}
			}
			if (e.getSource() == save_menu_item) {
				jfc.setDialogTitle("Save");
				jfc.setDialogType(JFileChooser.SAVE_DIALOG);

				int returnVal = jfc.showSaveDialog(save_menu_item);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					index.save(jfc.getSelectedFile());
				}

			}
			if (e.getSource() == join_index_menu_item) {
				jfc.setDialogTitle("Join");
				jfc.setDialogType(JFileChooser.OPEN_DIALOG);

				int returnVal = jfc.showOpenDialog(join_index_menu_item);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					visualizador.setText("");
					modelo_lista.clear();
					index.join(jfc.getSelectedFile());
				}
			}
		}
	}

}
