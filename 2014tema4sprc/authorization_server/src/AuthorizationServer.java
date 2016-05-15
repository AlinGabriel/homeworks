package authorization_server.src;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Logger;

import javax.crypto.*;
import javax.net.*;
import javax.net.ssl.*;


/* Arhip Alin Gabriel - Tema 4 SPRC 
 * Implementarea unui serviciu de gestiune a documentelor
 * AuthorizationServer.java - aplicatia client :
 * are rolul de a autoriza clientul sa downloadeze sau 
 * de a ii interzice sa downloadeze */
public class AuthorizationServer implements Runnable {
	
	/** Logger utilizat de aceasta clasa */
	static final transient Logger logger = Logger.getLogger(AuthorizationServer.class.getName());

	String keyStoreName=null,password=null;	// numele si parola keystore-ului serverului
	SecretKey key = null;		  // cheia pentru criptarea/decriptarea fisierului cu utilizatorii banati
	DESEncrypter crypt = null;
	SSLContext ctx = null;
	KeyStore sks = null;
	Certificate CACertificate = null;
	SSLServerSocket ss = null;
	Map<String, Integer> priorities=null;	// ce prioritate are un client
	KeyManagerFactory kmf = null;
	TrustManagerFactory tmf = null;
	

	public AuthorizationServer () {
		this.keyStoreName = "security/authorization_server.ks";
		this.password = "authorization_server_password";
		this.priorities = new LinkedHashMap<String, Integer>();
		this.priorities.put("HUMAN_RESOURCES", 1);
		this.priorities.put("ACCOUNTING", 1);
		this.priorities.put("IT", 2);				// am folosit 4 departamente
		this.priorities.put("MANAGEMENT", 3);
	}

	
	/* Clasa ajutatoare ce baneaza un client pe un timp determinat, setat la 15 secunde */
	private class Banned extends TimerTask {
		private AuthorizationServer server = null;
		private String clientName = null;
	
		public Banned (AuthorizationServer server, String clientName) {
			this.server = server;
			this.clientName = clientName;
		}
	
		public void run () {
			byte[] buffer = new byte[1024];
			AuthorizationServer.logger.info("Se scoate BAN-ul clientului: " + this.clientName);
			try {
				FileInputStream in = new FileInputStream("banned_decrypted");
				FileOutputStream out = new FileOutputStream("banned_decrypted");
				if (!this.server.crypt.decrypt(in,out)) {
					return;
				}
				in.read(buffer);
				in.close();
				String info = new String(buffer);
				String newInfo = info.replace(clientName, "");		// sterge clientName din banned list
				File file = new File("banned_encrypted");			// sterge informatia veche
				file.delete();
				File newEncryption = new File("banned_encrypted");	// cripteaza noua informatie
				if (newEncryption.createNewFile() == false) {
					AuthorizationServer.logger.severe("[ EROARE ] la crearea 'banned_encrypted' dupa stergerea clientului din banned list");
					out.close();
					return;
				}
				out.write(newInfo.getBytes());
				out.close();
				FileOutputStream fos = new FileOutputStream(newEncryption);
				if (!this.server.crypt.encrypt(new FileInputStream("banned_decrypted"), fos)) {
					AuthorizationServer.logger.severe("Nu s-a putut sterge banul clientului: " + clientName);
					fos.close();
					return;
				}
				fos.close();
			} catch (IOException e) {
				AuthorizationServer.logger.warning("[ EROARE ] Nu s-a putut sterge banul clientul:" + clientName);
				e.printStackTrace();
				return;
			}
			File temp = new File("banned_decrypted"); // sterge informatia veche
			temp.delete();
			return;
		}
	}	// sfarsit clasa Banned 

	public void createSSLConnection() {
		
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
		
		try {	// creearea socketului SSL	
			logger.info("Se creeaza server socket-ul SSL");
			ServerSocketFactory ssf = ctx.getServerSocketFactory();
			this.ss = (SSLServerSocket) ssf.createServerSocket(7000);
			ss.setNeedClientAuth(true);
			logger.info("Asculta comenzi... ");
		} catch (Exception e) {
			logger.severe("[ EROARE ] la crearea server socketului");
			e.printStackTrace();
			return;
		}
		
		try {	
			key = KeyGenerator.getInstance("DES").generateKey();	// se genereaza o cheie criptografica
			crypt = new DESEncrypter(key);							// se creeaza obiectul de cripteaza/decriptare
		} catch (Exception e) {
			logger.severe("[ EROARE ] Nu s-a putut genera o cheie criptografica");
			e.printStackTrace();
			return;
		}
	}
	
	public void run () {
		
		createSSLConnection();
		
		while (true) {
			
			String request = null;
			String response = null;
			SSLSocket socket = null;
			BufferedReader reader = null;
			BufferedWriter writer = null;
			X509Certificate[] peerCertificates = null;
			
			try {
				socket = (SSLSocket) this.ss.accept();
				logger.info("S-a initializat HandShakeul");
				socket.startHandshake();
			} catch (IOException e) {
				logger.severe("[ EROARE ] la handshake");
				e.printStackTrace();
				return;
			}
			
			try {
				logger.info("Se verifica existenta certificatelor");
				SSLSession ses = socket.getSession();
				peerCertificates = (X509Certificate[])ses.getPeerCertificates();
				if (peerCertificates.length < 2) {
					logger.severe("[ EROARE ] 'peerCertificates' contine mai putin de 2 certificate");
					continue;
				}
				if (CACertificate.equals(peerCertificates[1]))
					logger.info("Certificatul CA al peer-ului este identic cu certificatul CA al serviciului de autorizare");
				else {
					logger.severe("[ EROARE ] Certificatul CA al peer-ului difera de certificatul CA al serviciului de autorizare");
					continue;
				}
			} catch (IOException e) {
				logger.severe("[ EROARE ] Nu s-au putut obtine 'peerCertificates'");
				e.printStackTrace();
				continue;
			}
	
			try {
				logger.severe(" Se pregateste conexiunea cu peer-ul");
				InputStream is = socket.getInputStream(); 
				InputStreamReader isr = new InputStreamReader(is); 
				OutputStream os = socket.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				reader = new BufferedReader(isr);
				writer = new BufferedWriter(osw);
			} catch (IOException e) {
				logger.severe("Nu s-a putut comunica cu peer-ul");
				e.printStackTrace();
				continue;
			}
			try {
				request = reader.readLine();
				logger.info("S-a receptionat requestul");
				if (request == null) {
					logger.severe("S-a ajuns la sfarsitul requestului");
					try {
						socket.close();
					} catch (IOException ioe) {
						logger.severe("[EROARE ] la inchiderea socketului");
						ioe.printStackTrace();
					}
					continue;
				}
			
			} catch (IOException e) {
				logger.severe("[EROARE ] la citirea requestului");
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException ioe) {
					logger.severe("[EROARE ] la inchiderea socketului");
					ioe.printStackTrace();
				}
				continue;
			}
			
			StringTokenizer st = new StringTokenizer(request, " ");	// proceseaza requestul de la server
			if (st.countTokens() != 3) {
				logger.severe("Formatul este: fileDepartment clientName clientDepartment");
				response = "ERROR";
			}
			
			String fileDepartment = st.nextToken();
			String clientName = st.nextToken();
			String clientDepartment = st.nextToken();
			Integer filePriority = this.priorities.get(fileDepartment);
			Integer clientPriority = this.priorities.get(clientDepartment);
			
			if (fileDepartment.compareTo("BAN") == 0) {
				logger.info("S-a primit un request de BAN");
				
				try {	// adauga un client in lista de banati
					FileOutputStream fos = new FileOutputStream("banned_decrypted");
					if (!crypt.decrypt(new FileInputStream("banned_encrypted"),fos)) { // daca nu se poate decripta (e corupta)
						fos.close();
						return;
					}
					fos.close();
					FileOutputStream out = new FileOutputStream("banned_decrypted", true);	
					String line = clientName + " " + System.currentTimeMillis() + "\n";	
					out.write(line.getBytes());
					out.close();
					File file = new File("banned_encrypted");		// sterge informatia veche 
					file.delete();
					File newEncryption = new File("banned_encrypted");		// cripteaza noua informatie
					if (newEncryption.createNewFile() == false) {
						logger.severe("[ EROARE ] Nu s-a creat banned_encrypted corect cand s-a banat clientul: " + clientName);
						return;
					}
					fos = new FileOutputStream("banned_encrypted");
					if (!crypt.encrypt(new FileInputStream("banned_decrypted"), fos)) {
						logger.severe("[ EROARE ] Nu s-a putut bana clientul: " + clientName );
						fos.close();
						return;
					}
					fos.close();
					int after = 15000; 		// setat timer-ul la 15 secunde
					Date timeToRun = new Date(System.currentTimeMillis() + after);
					Timer timer = new Timer();
					Banned banned = new Banned(this, clientName);
					timer.schedule(banned, timeToRun);
					File temp = new File("banned_decrypted");	// sterge informatia veche
					temp.delete();
					response = "BANNED";
					if (filePriority == null || clientPriority == null) {
						logger.severe("Lista de prioritati nu contine prioritati pentru server/ departamentul clientului");
						response = "ERROR";
					}
				} catch (Exception e) {
					logger.severe("[ EROARE ] Nu s-a putut bana clientul: " + clientName );
					e.printStackTrace();
					try {
						reader.close();
						writer.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					return;
				}
			}
			
			byte[] buffer = new byte[1024];			
			boolean isBanned;			
			
			try {	// in isBanned retin  true daca clientName se afla in banned list
				FileOutputStream fos = new FileOutputStream("banned_decrypted");
				if (!crypt.decrypt(new FileInputStream("banned_encrypted"), fos)) {
					fos.close();
					isBanned = false;
				}
				fos.close();
				FileInputStream in = new FileInputStream("banned_decrypted");
				in.read(buffer);
				in.close();
				String info = new String(buffer);
				if (info.indexOf(clientName) == -1) {
					isBanned = false;
				}
				else {
					isBanned = true;
				}
				File temp = new File("banned_decrypted");		// sterge informatia veche
				temp.delete();
				if ((clientPriority.intValue() >= filePriority.intValue()) && (isBanned == false)) 
					response = "ALLOWED";						// daca prioritatea clientului este mai mare, ii este permis
				else 
					response = "DENIED";						// altfel nu ii este permis sa faca operatii
			} catch (IOException e) {
				logger.severe(" Nu s-a putut afla daca clientul: " + clientName + " este banat sau nu");
				e.printStackTrace();
				isBanned = false;
			}
			try {											// asupra documentelor din alt departament
				logger.info("Se trimite raspuns : <" + response + ">");
				writer.write(response);
				writer.newLine();
				writer.flush();
			} catch (IOException e) {
				logger.severe("[ EROARE ] La trimiterea mesajului pe socket");
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException ioe) {
					logger.severe("[ EROARE ] La inchiderea socketului");
					ioe.printStackTrace();
				}
				continue;
			}
		}
	}

	
	public static void main (String args[]) {
		AuthorizationServer authorizationServer = new AuthorizationServer();
		Thread thread = new Thread(authorizationServer);
		thread.start();
	}
}