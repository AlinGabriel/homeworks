package files_server.src;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Logger;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.net.ssl.*;
import authorization_server.src.ByteStream;
import authorization_server.src.DESEncrypter;


/* Arhip Alin Gabriel - Tema 4 SPRC 
 * Implementarea unui serviciu de gestiune a documentelor
 * DepartmentServer.java - aplicatia server :
 * are rolul de a desemna cu ajutorul serviciului de autentificare cine
 * are drept de download si cine este banat si de a raspunde la cele 3 
 * operatii posibile ale clientilor: list , download , upload */
public class DepartmentServer implements Runnable {

	/** Logger utilizat de aceasta clasa */
	private static Logger logger = Logger.getLogger(DepartmentServer.class.getName());
	
	int SAport=7000;	// portul Serviciului de Autorizare
	int port=0;		 // portul serverului, numele departamentului clientului, hostnameul serviciului de autorizare
	String name=null, hostname=null,keyStoreName=null, password=null; // numele si parola keystore-ului serverului
	SecretKey key = null;				   // cheia pentru criptarea/decriptarea fisierului cu utilizatorii banati
	Certificate CACertificate = null;
	SSLContext ctx = null;
	SSLServerSocket s = null;
	DESEncrypter crypt = null;
	KeyStore sks = null;
	KeyManagerFactory kmf = null;
	TrustManagerFactory tmf = null;
	HashMap<String, String[]> files=null;		// hash pentru stocarea tuturor fisierelor 
	Set<ClientHandler> connections = null;
		

	public DepartmentServer (int port, String hostname) {
		this.port = port;
		this.hostname = hostname;
		this.keyStoreName = "security/server.ks";
		this.password = "server_password";
		this.connections = new LinkedHashSet<ClientHandler>();
		this.files = new HashMap<String, String[]>();
	}
	
	/**
	 * Clasa ajutatoare Handler: se creeaza o instanta pentru fiecare client conectat la un DepartmentServer
	 * Acesta raspunde la mesajele de la client, si transmite mesajele de la fiecare client catre DepartmentServer respectiv 
	 * Clasa implementeaza Runnable: contine un constructor cu 4 parametri si override-ul metodei run() */
	private class ClientHandler implements Runnable {
		
		/** Logger utilizat de aceasta clasa */
		private final transient Logger logger = Logger.getLogger(ClientHandler.class.getName());
		
		String clientName=null,department=null;		// numele si departamentul clientului
		Socket clientSocket=null;					// socketul clientului
		DepartmentServer server=null;				// serverul care asculta conexiuni
		InputStream in = null;
		OutputStream out = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;

		public ClientHandler (DepartmentServer server, Socket clientSocket, String clientName, String department) {
			this.server = server;
			this.clientSocket = clientSocket;
			this.clientName = clientName;
			this.department = department;
			Thread thread = new Thread(this);
			thread.start();
		}

		@Override
		public void run () {
			String line = null;
			try {
				logger.info("Se creeaza un reader pentru socketul clientului");
				in = clientSocket.getInputStream();		
				InputStreamReader isr = new InputStreamReader(in); 		// asteapta mesaje de la client
				reader = new BufferedReader(isr);
				logger.info("Se creeaza un writer pentru socketul clientului");
				out = clientSocket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(out);	
				writer = new BufferedWriter(osw);
			} catch (IOException e) {
				logger.severe("Nu s-a putut crea un reader/writer pentru socketul clientului");
				e.printStackTrace();
				return;
			}

			while(true) {
				try {
					line = reader.readLine();
					if (line == null) {
						logger.severe("S-a ajuns la sfarsitul mesajului");
						break;
					}
				} catch (IOException e) {
					logger.info("[ EROARE ] Nu s-a putut citi mesajul clientului");
					e.printStackTrace();
					break;
				}
			
				if (line.indexOf("bomba") != -1 || line.indexOf("greva") != -1) {	// verifica lista de cuvinte interzise
					System.out.println("Clientul: " + clientName + " va fi banat! ");
					logger.info("Clientul '" + clientName + "' va fi banat pentru 15 secunde");
					String message = "BAN" + " " + clientName + " " + department;
					String response = null;
					SSLSocket auth = null;			// trimite mesaj la serviciu de autorizare si asteapta raspuns 
					BufferedReader authReader = null;
					BufferedWriter authWriter = null;
					BufferedWriter writer = null;
					try {	// conectarea la serviciul de autorizare pe portul 7000
						auth = (SSLSocket) ctx.getSocketFactory().createSocket(DepartmentServer.this.hostname, SAport);
						logger.info("S-a creat socketul SSL pentru comunicarea cu serviciul de autorizare");
						InputStream is = auth.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						authReader = new BufferedReader(isr);	
						OutputStream os = auth.getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(os);
						authWriter = new BufferedWriter(osw);
						authWriter.write(message);
						authWriter.newLine();
						authWriter.flush();
						response = authReader.readLine();	// asteapta raspuns de la serviciul de autentificare
						if (response == null)
							auth.close();					
						writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
						writer.write("DENIED");				// interzice accesul la documente unui client
						writer.newLine();
						writer.flush();
					} catch (IOException e) {
						logger.severe("[ EROARE ] Nu s-a putut mentine legatura cu serviciul de autorizare");
						e.printStackTrace();
						try {
							auth.close();
						} catch (IOException e2) {
							logger.severe("Nu s-a putut inchide socketul");
							e2.printStackTrace();
						}
						response = null;
					}
				}
			
				if (line.indexOf("list") == 0) {							// Operatia de listare a documentelor
					System.out.println("Clientul " + clientName + "doreste listarea documentelor existente");
					StringBuilder message = new StringBuilder("Lista de fisiere: ");
					String listOfFilenames;
					if(files.size() == 0)
						listOfFilenames =  "";
					StringBuilder sb = new StringBuilder();
					for (Iterator<String> it = files.keySet().iterator(); it.hasNext();) {
						sb.append(it.next()).append(", ");
					}
					listOfFilenames =  sb.toString().substring(0, sb.length() - 2);
					message.append(listOfFilenames);									// lista de fisiere
					try {
						this.writer.write(message.toString());
						this.writer.newLine();
						this.writer.flush();
						logger.info("S-a trimis cu succes lista de documente catre client");
					} catch (IOException e) {
						logger.severe("[ EROARE ] Nu s-a putut trimite lista de documente catre client");
						return;
					}
					continue;
				}

				if (line.indexOf("upload") == 0) {					// Operatia de upload a unui document
					System.out.println("Clientul: " + clientName + " doreste uploadarea unui document");
					StringTokenizer st = new StringTokenizer(line, " \t");
					String filename = null;
					st.nextToken();									// primul token este "upload"
					filename = st.nextToken();						// urmat de numele documentului
					String fileDetails[] = new String[2];
					fileDetails[0] = clientName;					// clientul owner
					fileDetails[1] = department;					// departamentul ownerului
					try {			
						logger.info("Se uploadeaza fisierul <" + filename + "> ");
						File file=new File("files", filename);		// primeste fisierul si il salveaza local
						ByteStream.toFile(in, file);				// in directorul "files"
						logger.info("Upload complet!");
					} catch (IOException e) {
						logger.severe("[ EROARE ] Nu s-a putut termina uploadul fisierului");
						e.printStackTrace(System.out);
					}
					
					server.files.put(filename, fileDetails);		// adauga fisierului in files-store
					logger.info("File added: <" + filename + "> <" + Arrays.toString(fileDetails) + ">");
					
					try {	// facem update la files-store-ul criptat in mod persistent al serverului
						FileOutputStream out = new FileOutputStream("temp_decrypted", true);	// il inlocuim pe cel vechi
						String serialized=null;
						if(files.size() == 0)
							serialized = "";
						StringBuilder sb = new StringBuilder();
						for (Iterator<Map.Entry<String, String[]>> it = files.entrySet().iterator(); it.hasNext();) {
							Map.Entry<String, String[]> c = it.next();
							sb.append(c.getKey()).append("~")			// filename~owner_client~owner_dept|...
								.append(c.getValue()[0]).append("~")
								.append(c.getValue()[1]).append("|");
						}
						serialized = sb.toString().substring(0, sb.length() - 1);
						out.write(serialized.getBytes());
						out.close();
						File oldEncryption = new File("files_list_encrypted");
						oldEncryption.delete();									// sterge criptarea veche
						File newEncryption = new File("files_list_encrypted");	// cripteaza noua informatie
						if (newEncryption.createNewFile() == false) {
							logger.severe("Nu s-a putut rescrie fisierul:files_list_encrypted ");
							return;
						}
						FileOutputStream fos = new FileOutputStream("files_list_encrypted");
						if (!crypt.encrypt(new FileInputStream("temp_decrypted"), fos)) {
							logger.severe("Nu s-a putut face update la file-store ");
							fos.close();
							return;
						}
						fos.close();
						logger.info("S-a facut update la file-store");
					} catch (Exception e) {
						logger.severe("Nu s-a putut face update file-store");
						e.printStackTrace();
						return;
					}
					File old = new File("temp_decrypted");	// sterge informatia veche
					old.delete();	
					
					String message = "Fisier " + filename + " uploadat cu succes.";	// raspunde catre client
					logger.info("Se trimite raspunul catre client");
					try {
						this.writer.write(message);
						this.writer.newLine();
						this.writer.flush();
						logger.info("S-a trimis cu succes raspunsul catre client");
					} catch (IOException e) {
						logger.severe("[ EROARE ] Nu s-a putut trimite raspunsul catre client");
						return;
					}
					continue;
				}

				if (line.indexOf("download") == 0) {			// Operatia de download a unui document
					System.out.println("Clientul " + clientName + " doreste downloadarea unui document");
					String message, filename = null;
					StringTokenizer st = new StringTokenizer(line, " \t");
					st.nextToken();									// primul token este "upload"
					filename = st.nextToken();	
					String fileDetails[] = server.files.get(filename);		// clientul owner, departamentul ownerului
					if(fileDetails == null){
						message = "Fisierul " + filename + " nu exista.";
						logger.info("S-a cerut un fisier: <" + filename + "> inexistent.");
						try {
							this.writer.write(message);
							this.writer.newLine();
							this.writer.flush();
							logger.info("S-a trimis cu succes raspunsul catre client");
						} catch (IOException e) {
							logger.severe("[ EROARE ] Nu s-a putut trimite raspunsul catre client");
							return;
						}
						continue;
					}
					message = fileDetails[1] + " " + clientName + " " + department;	// verifica daca este autorizat clientul
					String response = null;			// sa downloadeze un fisier
					SSLSocket auth = null;			// send a message to the authorization server and wait for a response
					BufferedReader authReader = null;
					BufferedWriter authWriter = null;
					try {	// conectarea la serviciul de autorizare pe portul 7000
						SSLSocketFactory ssf = ctx.getSocketFactory();
						auth = (SSLSocket) ssf.createSocket(DepartmentServer.this.hostname, SAport);
						InputStream is = auth.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						authReader = new BufferedReader(isr);
						OutputStream os = auth.getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(os);
						authWriter = new BufferedWriter(osw);
						authWriter.write(message);
						authWriter.newLine();
						authWriter.flush();
						response = authReader.readLine();	// asteapta raspuns de la serviciul de autorizare
						if (response == null) {
							logger.severe("[EROARE] Nu s-a putut comunica cu serviciul de autorizare");
							auth.close();
							return;
						}
						System.out.println("Raspunsul de la serviciul de autorizare pentru clientul: " + clientName + " este: " + response);
						if (response.compareTo("DENIED") == 0 || response.compareTo("ERROR") == 0) {
							BufferedWriter writer = null;		// interzice accesul clientului
							os = clientSocket.getOutputStream();
							osw = new OutputStreamWriter(os);
							writer = new BufferedWriter(osw);
							writer.write("DENIED");
							writer.newLine();
							writer.flush();
							continue;
						}
						logger.info("Fisier downloadat: <" + filename + "> <" + Arrays.toString(fileDetails) + ">");
						message = "Fisier " + filename + " downloadat.";
						logger.info("Se trimite raspunsul catre client");
						this.writer.write(message);
						this.writer.newLine();
						this.writer.flush();
						logger.info("S-a trimis cu succes raspunsul catre client");
					} catch (IOException e) {
						logger.severe("[EROARE]  Nu s-a putut mentine comunicarea cu serviciul de autorizare");
						e.printStackTrace();
						try {
							auth.close();
						} catch (IOException e2) {
							logger.severe("[EROARe] Nu s-a putut inchide socketul");
							e2.printStackTrace();
						}
						response = null;
					}
			
					try {												
						File file=new File("files", filename);		// directorul se numeste "files"
						ByteStream.toStream(out, file);				// in care se va pune fisierul downloadat
					} catch (IOException ex) {
						ex.printStackTrace(System.out);
					}
					continue;
				}
						
				try {	// daca s-a ajuns aici inseamna ca nu s-a dat una din cele 3 operatii valide
					this.writer.write("Operatie necunoscuta.Operatii disponibile: list, upload, download");
					this.writer.newLine();
					this.writer.flush();
					this.clientSocket.close();		// terminarea metodei run() cu inchiderea socketului
					logger.info("Socketul clientului inchis cu succes");
				} catch (IOException e) {
					logger.info("[ EROARE ] Nu s-a putut inchide socketul clientului");
					e.printStackTrace();
					return;
				}
			}	// iesirea din bucla
			this.server.connections.remove(this);	// scoatere conexiune
		}
	}
	
	public void prepareSSLConnection() {
		
		char[] storepswd = this.password.toCharArray(); 
		
		try {	
			logger.info("Se obtine o referinta catre keyManagerFactory-ul serverului");
			this.kmf = KeyManagerFactory.getInstance("SunX509");	// am presupus ca e Sun vm 	
			logger.info("Se obtine o referinta catre keystore-ul serverului");
			this.sks = KeyStore.getInstance("JKS");					// nu am folosit default, am urmat exemplul din lab.
			FileInputStream fis = new FileInputStream(this.keyStoreName);
			this.sks.load(fis, storepswd);
		} catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
			logger.severe("[ EROARE ] la keystore-ul/keyManagerFactory-ul serverului");
			e.printStackTrace();
			return;
		}
	
		try {	// initializarea keyManagerFactory cu cheia-material provenita din keystore-ul clientului
			logger.info("Se initializeaza keyManagerFactory-ul cu keystore-ul serverului");
			this.kmf.init(this.sks, storepswd);						// obtinerea unui TrustManagerFactory
			logger.info("Se obtine o referinta catre TrustManagerFactory-ul serverului");	
			this.tmf = TrustManagerFactory.getInstance("SunX509");	// am presupus ca e Sun vm 
			// initializarea TrustManagerFactory cu cheia-material provenita de la keystore-ul serverului
			logger.info("Se initializeaza TrustManagerFactory-ul cu keystore-ul serverului");
			this.tmf.init(this.sks);
		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			logger.severe("[ EROARE ] la initializarea keyManagerFactory-ul/TrustManagerFactory-ul serverului");
			e.printStackTrace();
			return;
		}
	
		try {	// initalizarea contextului SSL
			logger.info("Se initializeaza contextul SSL");
			this.ctx = SSLContext.getInstance("SSL");
			this.ctx.init(this.kmf.getKeyManagers(), this.tmf.getTrustManagers(), null);
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			logger.severe("[ EROARE ] Nu s-a putut initializa contextul SSL");
			e.printStackTrace();
			return;
		}
		
		try {	// obtinerea certificatului de la autoritatea de certificare a serverului
			logger.info("Se obtine certificatul de la autoritatea de certificare a serverului");
			this.CACertificate = this.sks.getCertificate("certification_authority");
		} catch (KeyStoreException e) {
			logger.severe("[ EROARE ] Nu s-a putut obtine certificatul de la autoritatea de certificare a serverului");
			e.printStackTrace();
			return;
		}
		
		try { 		//	citeste lista de documente disponibile din fisierul criptat 
			key = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec("parola".getBytes()));	
			crypt = new DESEncrypter(key);							// se creeaza obiectul de cripteaza/decriptare
		} catch (Exception e) {										// genereaza cheia criptografica denumita "parola"
			logger.severe("[ EROARE ] Nu s-a putut genera o cheie criptografica");
			e.printStackTrace();
			return;
		}
	}
	
	public void createSSLConnection() {
		try {	
			byte[] buffer = new byte[1024];	// decripteaza si citeste lista de fisiere
			FileOutputStream fos = new FileOutputStream("temp_decrypted");
			if (!crypt.decrypt(new FileInputStream("files_list_encrypted"), fos)) {
				logger.severe("Nu s-a putut decripta lista de fisiere");
				fos.close();
				return;
			}
			fos.close();
			FileInputStream in = new FileInputStream("temp_decrypted");
			in.read(buffer);
			in.close();
			String info = new String(buffer);
			files.clear();	// updateaza hashmapul de fisiere
			if(info.length() == 0 || info.charAt(0) == '\0'){
				return;
			}
			StringTokenizer st = new StringTokenizer(info, "|");
			while (st.hasMoreTokens()) {
				String group = st.nextToken();
				String tokens[] = group.split("~");
				String details[] = Arrays.copyOfRange(tokens, 1, 3);
				files.put(tokens[0], details);
			}
		} catch (Exception e) {
			logger.severe("Nu s-a putut citi din fisierul decriptat lista de nume");
			e.printStackTrace();
			return;
		}
		File old = new File("temp_decrypted");
		old.delete();		// sterge decriptarea veche
		try {
			SSLServerSocketFactory ssf = ctx.getServerSocketFactory();
			this.s = (SSLServerSocket) ssf.createServerSocket(this.port);
			logger.info("[ EROARE ] S-a creat cu succes server socketul SSL");
			s.setNeedClientAuth(true);
			logger.info(" Asteapta Conexiuni... ");
		} catch (Exception e) {
			logger.severe("[ EROARE ] Nu s-a putut crea socketul");
			e.printStackTrace();
			return;
		}
	}
	
	public void handleClients() {
		while (true) {
			try {
				Socket client = s.accept();
				((SSLSocket) client).startHandshake();
				logger.info("Hanshake initializat cu succese");
				X509Certificate[] peerCertificates = null;
				try {
					logger.info("Se verifica existenta certificatelor");
					SSLSession ses = ((SSLSocket) client).getSession();
					peerCertificates = (X509Certificate[]) ses.getPeerCertificates();
					if (peerCertificates.length < 2) {
						logger.severe("[ EROARE ] 'peerCertificates' contine mai putin de 2 certificate");
						return;
					}
					if (CACertificate.equals(peerCertificates[1]))
						logger.info("Certificatul CA al peer-ului este identic cu certificatul CA al serverului");
					else {
						logger.severe("[ EROARE ] Certificatul CA al peer-ului difera de certificatul CA al serverului");
						return;
					}
				} catch (Exception e) {
					logger.severe("[ EROARE ] Nu s-au putut obtine 'peerCertificates' ");
					e.printStackTrace();
					return;
				}
				String clientDN = peerCertificates[0].getSubjectX500Principal().getName();	// retine CN si OU al clientului din certificat
				StringTokenizer st = new StringTokenizer(clientDN, " \t\r\n,=");
				st.nextToken();
				String clientCN = st.nextToken();
				st.nextToken();
				String clientOU = st.nextToken();
				if (clientCN == null || clientOU == null) {
					logger.severe(" 'clientCN' sau 'clientOU' nule ");
					continue;
				}
				if (connections.add(new ClientHandler(this, client, clientCN, clientOU)) == false) 
					logger.info("[server] Eroare la ClientHandler");
			} catch (Exception e) {
				logger.severe("Eroare la mentinerea conexiunii cu clientul");
				e.printStackTrace();
				continue;
			}
		}
	}

	public void run () {
		prepareSSLConnection();	// // creeaza contextul, genereaza cheia secreta
		createSSLConnection();	// creeaza socketul si initiaza handshake-ul
		handleClients();	// accepta conexiuni, initiaza handshake-ul si 
	}						//creeaza un ClientHandle pentru fiecare client conectat	
	

	public static void main (String args[]) {
		if (args.length != 2) {
			System.err.println("Format de utilizare: java DepartmentServer port hostname_AuthorizationServer");
			System.exit(1);
		}
		int port = Integer.parseInt(args[0]);
		String hostname = args[1];
		DepartmentServer departmentServer = new DepartmentServer(port, hostname);
		Thread thread = new Thread(departmentServer);
		thread.start();
	}
}