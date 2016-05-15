package authorization_server.src;
import java.io.*;


/* Arhip Alin Gabriel - Tema 4 SPRC 
 * Implementarea unui serviciu de gestiune a documentelor
 * ByteStream.java - clasa ajutatoare pentru uploadul si
 * downloadul unui fisier , are 2 metode:
 * toFile - converteste un stream de octeti intr-un fisier
 * toStream - converteste fiserul primit intr-un stream de octeti */
public class ByteStream {

	public static void toStream(OutputStream os, File file) throws FileNotFoundException, IOException {
		int numRead = 0, in_int = (int) file.length();
		byte[] a = new byte[4], b = new byte[1024];
		InputStream is = new FileInputStream(file);
		for (int i = 0; i < 4; i++) {				// transforma intr-un ByteArray
			int b_int = (in_int >> (i * 8)) & 255;
			byte c = (byte) (b_int);
			a[i] = c;
		}
		os.write(a);
		while ((numRead = is.read(b)) > 0) {		// scrie la OutputStream b bytes
			os.write(b, 0, numRead);
		}
		is.close();
		os.flush();
	}

	public static void toFile(InputStream ins, File file) throws FileNotFoundException, IOException {
		byte[] buffer=null;
		int buf_size = 1024,total_len = 0, len = 0,len_read=0;
		FileOutputStream fos = new FileOutputStream(file);
		byte[] byte_array_4 = new byte[4];			 // converteste in ByteArray
		byte_array_4[0] = (byte) ins.read();
		byte_array_4[1] = (byte) ins.read();
		byte_array_4[2] = (byte) ins.read();
		byte_array_4[3] = (byte) ins.read();
		for (int i = 0; i < 4; i++) {				// si apoi din ByteArray transforma in Int
			int b = (int) byte_array_4[i];
			if (i < 3 && b < 0)
				b = 256 + b;
			len += b << (i * 8);
		}
		while(total_len < len) {					// cat timp am citit mai putini bytes decat len 
			buffer = new byte[buf_size];
			len_read = 0;
			total_len = 0;
			while (total_len + buf_size <= len) {
				len_read = ins.read(buffer);
				total_len += len_read;
				fos.write(buffer, 0, len_read);
			}
			len = len - total_len;
			buf_size = buf_size / 2;
		}
		fos.close();
	}
}