package authorization_server.src;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.crypto.*;


/* Arhip Alin Gabriel - Tema 4 SPRC 
 * Implementarea unui serviciu de gestiune a documentelor
 * DESEncrypter.java - clasa ajutatoare pentru criptarea 
 * si decriptarea unui fisier, are 2 metode:
 * encrypt - cripteaza un fisier
 * decrypt - decripteaza un fisier */
public class DESEncrypter {
	
	int bytesRead=0;
	byte[] buf = null;
	private Cipher ec=null;		// cifrul de criptare
	private Cipher dc=null;		// cifrul de decriptare
	CipherInputStream cin = null;
	CipherOutputStream cout = null;

	
	public DESEncrypter(SecretKey key) throws Exception {
		ec = Cipher.getInstance("DES");
		ec.init(Cipher.ENCRYPT_MODE, key);
		dc = Cipher.getInstance("DES");
		dc.init(Cipher.DECRYPT_MODE, key);
	}

	public boolean encrypt(InputStream in, OutputStream out) throws IOException {	
		cout = new CipherOutputStream(out, ec);
		buf = new byte[1024];
		bytesRead = 0;
		while ((bytesRead = in.read(buf)) >= 0) {	// citeste informatia de la in
			cout.write(buf, 0, bytesRead);			// o cripteaza si o scrie la out
		}
		cout.close();
		return true;
	}

	public boolean decrypt(InputStream in, OutputStream out) throws IOException {
		cin = new CipherInputStream(in, dc);
		buf = new byte[1024];
		bytesRead = 0;
		while ((bytesRead = cin.read(buf)) >= 0) {	// citeste informatia de la in
			out.write(buf, 0, bytesRead);			// o decripteaza si o scrie la out
		}
		cin.close();
		return true;
	}
}