package client;
import java.io.Serializable;

//Arhip Alin-Gabriel Tema3 SPRC BitTorrent
// Clasa ce descrie un fisier: contine set-ere si get-ere
// pentru nume, dimensiune totata si dimensiunea unui fragment 
public class FileDescriptor implements Serializable {
	
	static final long serialVersionUID = 1L;
	long totalLength=0;
	long fragmentLength=0;
	String fileName=null;
	
	public FileDescriptor(String fileName) {
		this.fileName = fileName;
	}
	public FileDescriptor(String fileName, long totalLength, long fragmentLength) {
		this.fileName = fileName;
		this.totalLength = totalLength;
		this.fragmentLength = fragmentLength;
	}
	public String getFileName() {
		return fileName;
	}
	public long getFragmentLength() {
		return fragmentLength;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setTotalLength(long totalLength) {
		this.totalLength = totalLength;
	}
	public void setFragmentLength(long fragmentLength) {
		this.fragmentLength = fragmentLength;
	}
}