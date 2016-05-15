package client.src;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import authorization_server.src.ByteStream;


/* Arhip Alin Gabriel - Tema 4 SPRC 
 * Implementarea unui serviciu de gestiune a documentelor
 * Client.java - aplicatia client :
 * are rolul de a citi documente si de a incarca pe server noi documente 
 * operatii posibile: list , download , upload */
public class Client implements Runnable {
	
	/** Logger utilizat de aceasta clasa */
	static final transient Logger logger = Logger.getLogger(Client.class.getName());
	
	int port=0; 
	String hostname=null;						// portul si numele serverului
	String name=null, department=null;  		// numele si departamentul clientului
	String keyStoreName=null, password=null;	// numele keystore-ului clientului si parola de acces a keystore-ul serverului
	SSLSocket s = null;
	SSLSocketFactory ssf=null;
	SSLContext ctx = null;
	Certificate CACertificate = null;			// certificatul clientului
	KeyStore cks = null;						// keystore-ul clientului
	KeyManagerFactory kmf = null;
	TrustManagerFactory tmf = null;

	
	public Client (String name, String department, String hostname, int port) {
		this.name = name;
		this.department = department;
		this.hostname = hostname;
		this.port = port;
		this.keyStoreName = "security/" + name + "/" + name + ".ks";
		this.password = name + "_password";
	}

	public void createSSLConnection() {
		
		char[] storepswd = this.password.toCharArray(); 
		
		try {	
			logger.info("Se obtine o referinta catre keyManagerFactory-ul clientului");
			this.kmf = KeyManagerFactory.getInstance("SunX509");	// am presupus ca e Sun vm 	
			logger.info("Se obtine o referinta catre keystore-ul clientului");
			this.cks = KeyStore.getInstance("JKS");					// nu am folosit default, am urmat exemplul din lab.
			FileInputStream fis = new FileInputStream(this.keyStoreName);
			this.cks.load(fis, storepswd);
		} catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
			logger.severe("[ EROARE ] la keystore-ul/keyManagerFactory-ul clientului");
			e.printStackTrace();
			return;
		}
	
		try {	// initializarea keyManagerFactory cu cheia-material provenita din keystore-ul clientului
			logger.info("Se initializeaza keyManagerFactory-ul cu keystore-ul clientului");
			this.kmf.init(this.cks, storepswd);						// obtinerea unui TrustManagerFactory
			logger.info("Se obtine o referinta catre TrustManagerFactory-ul clientului");	
			this.tmf = TrustManagerFactory.getInstance("SunX509");	// am presupus ca e Sun vm 
			// initializarea TrustManagerFactory cu cheia-material provenita de la keystore-ul serverului
			logger.info("Se initializeaza TrustManagerFactory-ul cu keystore-ul serverului");
			this.tmf.init(this.cks);
		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			logger.severe("[ EROARE ] la initializarea keyManagerFactory-ul/TrustManagerFactory-ul clientului");
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
		
		try {	// obtinerea certificatului de la autoritatea de certificare a clientului
			logger.info("Se obtine certificatul de la autoritatea de certificare a clientului");
			this.CACertificate = this.cks.getCertificate("certification_authority");
		} catch (KeyStoreException e) {
			logger.severe("[ EROARE ] Nu s-a putut obtine certificatul de la autoritatea de certificare a clientului");
			e.printStackTrace();
			return;
		}
		
		try {	// creearea socketului SSL				
			logger.info("Se creeaza socket-ul SSL");
			this.ssf = this.ctx.getSocketFactory();			
			this.s = (SSLSocket) this.ssf.createSocket(hostname, port);
			logger.info("Se initializeaza handshake-ul");
			this.s.startHandshake();						// initalizarea handshake-ului
		} catch (IOException e) {
			logger.severe("[ EROARE ] la crearea socketului/ initializarea handshake-ului");
			e.printStackTrace();
			return;
		}
	}
	
	public void checkCertificates() {
		
		X509Certificate[] peerCertificates = null;
		
		try {
			logger.info("Se verifica existenta certificatelor");
			SSLSession ses = this.s.getSession();
			peerCertificates = (X509Certificate[]) ses.getPeerCertificates();
			if (peerCertificates.length < 2) {
				logger.severe("[ EROARE ] 'peerCertificates' contine mai putin de 2 certificate");
				return;
			}
			if (CACertificate.equals(peerCertificates[1]))
				logger.info("Certificatul CA al peer-ului este identic cu certificatul CA al clientului");
			else {
				logger.severe("[ EROARE ] Certificatul CA al peer-ului difera de certificatul CA al clientului");
				return;
			}
		} catch (Exception e) {
			logger.severe("[ EROARE ] Nu s-au putut obtine 'peerCertificates' ");
			e.printStackTrace();
			return;
		}
	}
	
	private void ParseCommands() {
		InputStream in=null;
		OutputStream out=null;
		BufferedReader reader=null,inputReader=null;
		BufferedWriter writer=null;
		String request = null, response = null;
		
		try {
			in = s.getInputStream();
			InputStreamReader isr = new InputStreamReader(in);
			reader = new BufferedReader(isr);
			out = s.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(out);
			writer = new BufferedWriter(osw);
			logger.info("Se asteapta comenzi. Comenzile posibile sunt: list, download,upload");
			inputReader = new BufferedReader(new InputStreamReader(System.in));
		} catch (IOException e) {
			logger.severe("[ EROARE ] Nu s-a putut mentine comunicarea cu serverul");
			e.printStackTrace();
			return;
		}
	
		while (true) {
			
			System.out.println("Introduceti comanda catre server:");
			
			try {
				request = inputReader.readLine();
				if (request == null) {
					logger.severe("Nu s-a introdus nimic");
					return;
				}
				writer.write(request);
				writer.newLine();
				writer.flush();
				logger.info("Comanda s-a trimis catre server");

			} catch (IOException e) {
				logger.info("[ EROARE ] Nu s-a putut trimite comanda catre server. Reincercati");
				e.printStackTrace();
				continue;
			}
			
			if(request.indexOf("upload") == 0){				// verifica daca s-a tastat upload
				StringTokenizer st = new StringTokenizer(request, " \t");
				String filename = null;
				File file = null;
				st.nextToken();								// primul token este "upload"
				filename = st.nextToken();					// al doilea este numele fisierului 
				file=new File("upload", filename);	// trimite fisierul in directorul de upload
				try {	
					ByteStream.toStream(out, file);		
					logger.info("Asteapta raspuns de la server");
				} catch (IOException ex) {
					ex.printStackTrace(System.out);
				}
			}			
			
			try {
				response = reader.readLine();
			} catch (IOException e) {
				logger.info("[ EROARE ] Nu s-a putut citi raspunul trimis de catre server");
				e.printStackTrace();
				try {
					s.close();
				} catch (IOException e2) {
					logger.severe("Nu s-a putut inchide socket-ul");
					e2.printStackTrace();
				}
			}

			if (response == null) {
				logger.severe("Nu s-a trimis nimic");
				try {
					s.close();
				} catch (IOException e) {
					logger.severe("Nu s-a putut inchide socket-ul");
				}
				return;
			}
			
			if (response.compareTo("DENIED") == 0) {
				logger.info("Acces interzis .");
				System.out.println("Acest client este banat! Nu are acces la upload/download de fisiere");
				continue;
			}

			if(request.indexOf("download") == 0){				// daca s-a tastat download
				StringTokenizer st = new StringTokenizer(request, " \t");
				String filename = null;
				st.nextToken();
				filename = st.nextToken();
				try {											
					File file=new File("download", filename);	// primire fisier in directorul de download
					ByteStream.toFile(in, file);
					logger.info("Fisier downloadat cu succes");
				} catch (IOException ex) {
					ex.printStackTrace(System.out);
				}
			}
			
			System.out.println("Raspunsul serverului \n\t" + response);
		}
	}
	
	public void run () {		// pregatirea autentificarii de catre server
		createSSLConnection();	// creeaza socketul,contextul si initiaza handshake-ul
		checkCertificates();	// verifica existenta ambelor certificate si egalitatea dintre ele
		ParseCommands();		// parseaza si efectueaza comenzile list, download, upload
	}
	

	public static void main (String args[]) {
		if (args.length != 4) {
			System.err.println("Format de utilizare: java Client <nume> <departament> <hostname> <port>");
			System.exit(1);
		}
		String name = args[0];
		String department = args[1];
		String hostname = args[2];
		Integer port = Integer.parseInt(args[3]);
		Client client = new Client(name,department,hostname,port);
		Thread thread = new Thread(client);
		thread.start();		/* apeleaza metoda run() care apeleaza metodele clientului */
	}
}