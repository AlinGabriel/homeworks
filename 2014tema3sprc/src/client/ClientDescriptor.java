package client;
import java.io.Serializable;

//Arhip Alin-Gabriel Tema3 SPRC BitTorrent
// Clasa descrie un Client: are set-ere si get-ere pentru adresa si port
public class ClientDescriptor implements Serializable {
	
	static final long serialVersionUID = 1L;
	int port=0;				// portul clientului
	String address=null;	// adresa clientului
	
	public ClientDescriptor(String address, int port) {
		this.port = port;
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public String getAddress() {
		return address;
	}
	public void setPort(int port){
		this.port = port;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}