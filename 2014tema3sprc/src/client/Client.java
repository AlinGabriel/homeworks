package client;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

// Arhip Alin-Gabriel Tema3 SPRC BitTorrent Client

//clasa care creaza un thread nou pentru conectarea unui client in vederea downloadarii unui fragment
class Connect implements Runnable {
	
	static Logger log = Logger.getLogger(Connect.class.getName());	
	int port=0;
	int fragment=0;
	String address=null;
	Socket s=null;
	FileDescriptor fd=null;
	ObjectOutputStream oos=null;
	ObjectInputStream ois=null;

	public Connect(String address, int port, int fragment, FileDescriptor fd) {
		this.fd = fd;
		this.port=port;
		this.address  = address;
		this.fragment = fragment;
	}
	
	@Override
	public void run() {
		
		try {
			s = new Socket(address, port);
			OutputStream os2 = s.getOutputStream();
			InputStream is2 = s.getInputStream();
			oos = new ObjectOutputStream(os2);
			ois = new ObjectInputStream(is2);
			
			// trimite mesaj de fragment request catre alt client
			MessageDescriptor req = new MessageDescriptor();	
			req.setFileDescriptor(fd);
			req.setFragment(fragment);	
			req.setClientDescriptor(new ClientDescriptor(address, port));
			oos.writeObject(req);
			
			// primeste fragmentul respectiv
			MessageDescriptor fileMsg = (MessageDescriptor)ois.readObject();
			log.info("Received fragment: " + fragment + " from " + port);
			// scrie ce a primit in fisierul downloaded:
			String pathToFile = "./../downloaded/" + fd.getFileName();
			// scrie octetii primitii intr-un nou fisier
			RandomAccessFile raf = new RandomAccessFile(new File(pathToFile), "rw");
			raf.seek(fragment * fd.getFragmentLength());
			raf.write(fileMsg.getFile());
			
			raf.close();
			s.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


//clasa care creaza un thread nou pentru conectarea unui client in vederea uploadarii unui fragment
class NewClient implements Runnable {
	
	static Logger log = Logger.getLogger(NewClient.class.getName());
	ServerSocket socket;
	
	public NewClient(int port) throws IOException {
		socket = new ServerSocket(port);
	}
	
	@Override
	public void run() {
		
		while(true) {
			try {
				log.info("Waiting...");
				Socket wait = socket.accept();
				log.info("Client connected...");
				OutputStream out = wait.getOutputStream();
				InputStream in = wait.getInputStream();
				ObjectOutputStream oosToClient = new ObjectOutputStream(out);
				ObjectInputStream oisToClient = new ObjectInputStream(in);
				
				// asteapta un request 
				MessageDescriptor req = (MessageDescriptor)oisToClient.readObject();
				log.info("Request Received...");
				String pathToFile = "./../files/" + req.getFileDescriptor().getFileName();
				File file = new File(pathToFile);		// citeste fisierul de trimis
				
				int fragment = req.getFragment();
				long fragmentLength = req.getFileDescriptor().getFragmentLength();
				int toSend = (int)fragmentLength;
				
				if((fragment + 1) * fragmentLength > (int) file.length()) 
					toSend = (int) file.length() - fragment * (int)fragmentLength;
				
			    byte[] fileData = new byte[toSend];
			    RandomAccessFile raf = new RandomAccessFile(file, "r");
			    raf.seek(fragment * (int)fragmentLength);
			    raf.read(fileData);
			    raf.close();
			    MessageDescriptor res = new MessageDescriptor();
			    res.setFile(fileData);
			    res.setClientDescriptor(req.getClientDescriptor());
				oosToClient.writeObject(res);		// trimite fisierul la client
				
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}


// clasa ce implementeaza un client
public class Client extends AbstractClient {
	
	static Logger log = Logger.getLogger(Client.class.getName());
	static final int FRAGMENTS = 3;
	int clientPort=0;
	String ClientIp=null;
	Socket s=null;
	ObjectInputStream ois=null;
	ObjectOutputStream oos=null;
	
	public Client(String ClientIp, int clientPort, String serverHost, int serverPort) 
	{
		this.ClientIp = ClientIp;
		this.clientPort = clientPort;
		
		try {
			(new Thread(new NewClient(clientPort))).start();
			s = new Socket(serverHost, serverPort);
			OutputStream out = s.getOutputStream();
			InputStream in = s.getInputStream();
			oos = new ObjectOutputStream(out);
			ois = new ObjectInputStream(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publishFile(File file)  {
		
		FileDescriptor fd = new FileDescriptor(file.getName(),  file.length(), file.length()/ FRAGMENTS);
		MessageDescriptor m = new MessageDescriptor();
		m.setMessageBody("publish");
		m.setFileDescriptor(fd);
		ClientDescriptor clh = new ClientDescriptor(ClientIp, clientPort);
		m.setClientDescriptor(clh);
		
		try {
			oos.writeObject(m);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void retrieveFile(String fileName) {
		
		MessageDescriptor m1 = new MessageDescriptor();
		m1.setMessageBody("retrieve");
		FileDescriptor fd1 = new FileDescriptor(fileName);
		m1.setFileDescriptor(fd1);
		
		try {
			oos.writeObject(m1);
			MessageDescriptor m2 = (MessageDescriptor)ois.readObject();
			
			// contactez clientii si incep download-ul fisierelui 
			FileDescriptor fd=m2.getFileDescriptor();
			List<ClientDescriptor> owners = m2.getOwnersOfFile();
			List<Thread> list = new ArrayList<Thread>();
			ClientDescriptor ownerClient = null;
			int counter = 0;
			
			// pentru fiecare fragment al unui fisier , 
			// alege un peer diferit de la care sa il downloadeze
			for(int i = 0; i < FRAGMENTS; i++) {
				// pentru fiecare fragment creez un thread nou si il adaug in lista, incrementand counter
				ownerClient = owners.get(counter);
				String adress = ownerClient.getAddress();
				int p = ownerClient.getPort();
				Connect cl = new Connect(adress, p , i, fd);
				Thread thread = new Thread(cl);
				list.add(thread);
				thread.start();
				counter++;
				if(counter == owners.size())
					counter = 0;
			}
			for(Thread thread : list)		
					thread.join();			// asteapta threadul sa termine downloadul
			
			MessageDescriptor m = new MessageDescriptor();
			m.setMessageBody("publish");	// informeaza server-ul ca avem un fisier nou de share-uit
			m.setFileDescriptor(fd);
			ClientDescriptor clh = new ClientDescriptor(ClientIp, clientPort);
			m.setClientDescriptor(clh);
			oos.writeObject(m);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	public static void main(String[] args) throws IOException {
		if(args.length < 4) { 
			log.info("Usage: run-client address port serveradress serverport");
			return;
		}
		new Client(args[0],Integer.parseInt(args[1]),args[2],Integer.parseInt(args[3]));
	}	
}