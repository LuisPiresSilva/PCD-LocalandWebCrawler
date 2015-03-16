package gui;

import index.Index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Scanner;

import query.ParallelQuery;

public class TrataConexao extends Thread {

	final int INDEXACAO = 1;
	final int PESQUISA = 2;
	final int CONSULTA = 3;
	ObjectInputStream clienteSocketIn;
	ObjectOutputStream clienteSocketOut;
	Index index;
	PCDGoogle serverInterface;
	SocketAddress id;
	
	ParallelQuery query;
	public TrataConexao(SocketAddress id, ObjectInputStream clienteSocketIn,
			ObjectOutputStream clienteSocketOut, Index index, PCDGoogle serverInterface) {
		this.id=id;
		this.clienteSocketIn = clienteSocketIn;
		this.clienteSocketOut = clienteSocketOut;
		this.index=index;
		this.serverInterface=serverInterface;

	}

	public void run() {
		try {
			while (true) {
				int servico = (Integer) clienteSocketIn.readObject();
				if (servico == PESQUISA) {
					String string_Pesquisa= (String) clienteSocketIn.readObject();
					query = new ParallelQuery(index,string_Pesquisa);
					serverInterface.consola.append("O Cliente "+ id+ " solicitou uma pesquisa por: "+ string_Pesquisa+ "\n");
					HashSet<String> results = query.getResults();
					if(results!=null){
						clienteSocketOut.writeObject(results);
						serverInterface.consola.append("Enviados resultados para o cliente "+ id+ " da pesquisa pesquisa por: "+ string_Pesquisa+ "\nHits: "+query.getNumResults()+"\n");
					}else{
						clienteSocketOut.writeObject(new HashSet<String>());
						serverInterface.consola.append("Enviados resultados para o cliente "+ id+ " da pesquisa pesquisa por: "+ string_Pesquisa+ "\nHits: 0"+"\n");
					}
				} else if (servico == CONSULTA) {
					String path = (String) clienteSocketIn.readObject();
					serverInterface.consola.append("O Cliente "+ id+ " solicitou uma consulta por: "+ path+"\n");
					if(path.startsWith("http://")){
						clienteSocketOut.writeObject(path);
						serverInterface.consola.append("Enviado endereço web para o Cliente " + id+ "\n");
					}else{
						try{
							File file = new File(path);
							Scanner leitura = new Scanner(file);
							StringBuffer buffer = new StringBuffer("");
							while(leitura.hasNextLine())
								buffer.append(leitura.nextLine()+"\n");
							clienteSocketOut.writeObject(buffer);
							serverInterface.consola.append(file.length()/1024+" Kbs enviados para o Cliente " + id+ "\n");
						} catch (FileNotFoundException e) {
							clienteSocketOut.writeObject("FileNotFoundException");
						}
					}
					
					
				} else if (servico == INDEXACAO) {
					String path = (String)clienteSocketIn.readObject();
					int num_crawlers= (Integer)clienteSocketIn.readObject();
					int crawl_depth = (Integer)clienteSocketIn.readObject();
					serverInterface.consola.append("O Cliente "+ id+ " solicitou uma indexação em: "+ path+ "\nCom Crawlers: "+ num_crawlers+" Crawl Depth: "+ crawl_depth+ "\n");
					serverInterface.crawlService(path, num_crawlers, crawl_depth, index);
				}
			}
		} catch (IOException e) {

		} catch (ClassNotFoundException e) {
		} finally {
			serverInterface.consola.append("O Cliente " + id + " desconectou-se\n");
		}
	}

}
