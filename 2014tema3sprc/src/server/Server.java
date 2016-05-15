package server;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import client.ClientDescriptor;
import client.FileDescriptor;
import client.MessageDescriptor;

//Arhip Alin-Gabriel Tema3 SPRC BitTorrent Server

// clasa ajutatoare ce contine implementarea de tip publish si retrieve de fisiere
class ConnectNewClient implements Runnable {
	
	static Logger log = Logger.getLogger(ConnectNewClient.class.getName());
	ObjectOutputStream oos=null;
	ObjectInputStream ois=null;
	Map<FileDescriptor, List<ClientDescriptor>> files=null;
	
	public ConnectNewClient(Socket cs,Map<FileDescriptor, List<ClientDescriptor>> files) {
		try {
			OutputStream os = cs.getOutputStream();
			oos = new ObjectOutputStream(os);
			InputStream is = cs.getInputStream();
			ois = new ObjectInputStream(is);
			this.files = new HashMap<FileDescriptor, List<ClientDescriptor>>(files);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		while(true) {
		
			try {
				MessageDescriptor m = (MessageDescriptor)ois.readObject();	
				
				// daca primeste un mesajul de tip publish,  publica fisierul
				if(m.getMessageBody().equals("publish")) {	
					
					log.info("publish " + m.getFileDescriptor().getFileName());		
					FileDescriptor fd=m.getFileDescriptor();
					ClientDescriptor cd=m.getClientDescriptor();
				
					// adauga fisierul hashmap-ul de fisiere publicate, sau daca exista deja adauga noul client in lista de owneri.
					for(Map.Entry<FileDescriptor, List<ClientDescriptor>> entry : files.entrySet()) {
						if(entry.getKey().getFileName().equals(fd.getFileName())) {
							entry.getValue().add(cd);
							return;
						}
					}
					List<ClientDescriptor> clients = new ArrayList<ClientDescriptor>();
					clients.add(cd);
					files.put(fd, clients);
				}
				// altfel daca primeste mesajul de tip retrieve
				else if(m.getMessageBody().equals("retrieve")) {
					
					log.info("retrieve " + m.getFileDescriptor().getFileName());
					MessageDescriptor res = new MessageDescriptor();
				
					// cauta fisierul in hashmap-ul files si face retrive la el daca il gaseste
					for(Map.Entry<FileDescriptor, List<ClientDescriptor>> entry : files.entrySet()) {
						if(entry.getKey().getFileName().equals(m.getFileDescriptor().getFileName())) {
							res.setFileDescriptor(entry.getKey());
							res.setOwnersOfFile(entry.getValue());
						}
					}
					oos.writeObject(res);
				}
			} catch (IOException e) {
				log.info("Connection lost!");
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}

// clasa ce implementeaza serverul
public class Server implements Runnable {
	
	static Logger log = Logger.getLogger(Server.class.getName());
	ServerSocket s=null;
	Map<FileDescriptor, List<ClientDescriptor>> files=null;
	
	public Server(int port) throws IOException {
		s = new ServerSocket(port);
		files = new HashMap<FileDescriptor, List<ClientDescriptor>>();
	}
	
	@Override
	public void run() {
		
		while(true) {
			try {
				Socket sc = s.accept();
				log.info("Client connected!");
				(new Thread(new ConnectNewClient(sc,files))).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length < 1) { 
			log.info("Usage: run-server port");
			return;
		}
		try {
			(new Server(Integer.parseInt(args[0]))).run();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}