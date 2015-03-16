package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class RemoteUserInterface extends JFrame implements WindowListener {

	// -----------NET----------------------------------------
	final static int PORTA = 8080;
	final int INDEXACAO = 1;
	final int PESQUISA = 2;
	final int CONSULTA = 3;

	Socket socket;
	ObjectInputStream serverSocketIn;
	ObjectOutputStream serverSocketOut;
	// ------------------------------------------------------

	// -----------Gráfico------------------------------------
	private JFrame frame = new JFrame("PCDGoogle - Cliente");
	// menu inicio
	// -----------------------------------------------------------------------------------
	private JMenuBar menu_bar = new JMenuBar();
	private JMenu file_menu = new JMenu("File");
	private JMenuItem quit_menu_item = new JMenuItem("Quit");
	private JMenuItem connect_button = new JMenuItem("Connect");
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
	private JTextArea consola = new JTextArea("", 5, 0);

	// painel de baixo fim
	// ---------------------------------------------------------------------------

	public RemoteUserInterface() {
		frame.setSize(1000, 700);
		frame.setLocationByPlatform(false);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.addWindowStateListener(windowListener);

		file_menu.add(connect_button);
		file_menu.add(quit_menu_item);

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

		connect_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				execute();
			}
		});

		menu_bar.add(file_menu);
		frame.setJMenuBar(menu_bar);

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
					JOptionPane.showMessageDialog(null, "caracteres invalidos");
				}

				if (num_crawlers > 0) {
					try {
						try {
							serverSocketOut.writeObject(INDEXACAO);
							serverSocketOut.writeObject(path);
//							System.out.println(path);
							serverSocketOut.writeObject(num_crawlers);
//							System.out.println(num_crawlers);
							serverSocketOut.writeObject(crawl_depth);
//							System.out.println(crawl_depth);
						} catch (SocketException e) {
							consola.append("Can't reach the server\n");
							socket.close();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

				} else {
					JOptionPane.showMessageDialog(null, "Minimo Crawlers: 1");
				}
			}
		});

		query_text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					visualizador.setText("");
					modelo_lista.clear();
					if (!query_text.getText().isEmpty()) {

						try {
							try {
								if (palavraAceitavel(query_text.getText()
										.trim())) {
									consola
											.append("Requisitando consulta ao servidor por: "
													+ query_text.getText()
													+ "\n");
									serverSocketOut.writeObject(PESQUISA);

									serverSocketOut.writeObject(query_text
											.getText().trim());
									HashSet<String> query_results = (HashSet<String>) serverSocketIn
											.readObject();
									if (query_results != null) {
										for (String s : query_results)
											modelo_lista.addElement(s);
									}
									consola
											.append("Recebidos resultados da pesquisa\n Hits: "
													+ query_results.size()
													+ "\n");
								} else {
									consola
											.append("A pesquisa contém caracteres inválidos: Só caracteres de A a Z são aceitavéis\n");
								}
							} catch (SocketException e1) {
								consola.append("Can't reach the server\n");
								socket.close();
							}
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (ClassNotFoundException e2) {
						}
					}
					if (query_text.getText().isEmpty())
						visualizador.setText("Não escreveu nada");
					else if (modelo_lista.isEmpty())
						visualizador
								.setText("Não foram encontrados resultados");
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
				if (lista.getValueIsAdjusting()) {// solved
					if (!modelo_lista.isEmpty() && !lista.isSelectionEmpty()) {
						visualizador.setText("");
						try {
							try {
								serverSocketOut.writeObject(CONSULTA);
								serverSocketOut.writeObject(""
										+ modelo_lista.getElementAt(lista
												.getSelectedIndex()));
								consola
										.append("Requisitando consulta ao servidor\n");
								String path = (String) modelo_lista
										.getElementAt(lista.getSelectedIndex());
								if (path.startsWith("http://")) {
									try {
										visualizador
												.setPage(new URL("" + path));

									} catch (IOException e) {
									}
								} else {
									StringBuffer ficheiro = (StringBuffer) serverSocketIn
											.readObject();
									visualizador.setText(ficheiro.toString());
									if (!visualizador.getText().startsWith(
											"FileNotFoundException"))
										consola
												.append("Recebido ficheiro: "
														+ modelo_lista
																.getElementAt(lista
																		.getSelectedIndex())
														+ "\n");
								}
								visualizador.setCaretPosition(0);
							} catch (SocketException e1) {
								try {
									consola.append("Can't reach the server\n");
									socket.close();
								} catch (IOException e) {
									e1.printStackTrace();
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		// painel central fim
		// --------------------------------------------------------------------------

		// painel de baixo inicio
		// ----------------------------------------------------------------------
		frame.getContentPane()
				.add(new JScrollPane(consola), BorderLayout.SOUTH);
		// painel de baixo fim
		// --------------------------------------------------------------------------

		// indexação remota---------------------------

	}

	public boolean palavraAceitavel(String queryString) {
		for (int i = 0; i < queryString.length(); ++i) {
			if ((queryString.charAt(i) < 'a' || queryString.charAt(i) > 'z')
					&& queryString.charAt(i) != ' ')
				return false;
		}
		return true;
	}

	public void execute() {

		String endereço = JOptionPane.showInputDialog(null,
				"Enter the server address", "Connect",
				JOptionPane.QUESTION_MESSAGE);

		consola.append("Tentando conectar a: " + endereço + "\n");

		frame.setVisible(true);
		try {
			try {
				InetAddress endereco = InetAddress.getByName(endereço);
				if (endereco != null) {
//					System.out.println("Endereço = " + endereco);
					socket = new Socket(endereco, PORTA);
					if (socket != null) {
						serverSocketOut = new ObjectOutputStream(socket
								.getOutputStream());
						serverSocketIn = new ObjectInputStream(socket
								.getInputStream());
					} else {
//						System.out.println("Can't communicate with the server");
					}
				} else {
//					System.out.println("Can't reach the server");
				}
			} catch (SocketException e) {
				consola.append("Can't reach the server\n");
				if (socket != null)
					socket.close();
			}
		} catch (UnknownHostException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (socket != null)
			consola.append("Conexão efectuada com sucesso a: "
					+ socket.getRemoteSocketAddress() + "\n");
	}
	
	
	WindowStateListener windowListener = new WindowStateListener() {
		public void windowStateChanged(WindowEvent wE) {
			//tentativa para fechar socket ao fazhar janela cliente
			if (wE.getNewState() == WindowEvent.WINDOW_CLOSED) {
				try {
//					System.out.println("Fechou socket");
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}

	};

	public static void main(String args[]) {
		new RemoteUserInterface().execute();
	}

	public void windowActivated(WindowEvent e) {
		//tentativa para remediar bug gráfico
		lista.setSize(100, 100);
	}

	public void windowClosed(WindowEvent e) {

	}

	public void windowClosing(WindowEvent e) {

	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

}
