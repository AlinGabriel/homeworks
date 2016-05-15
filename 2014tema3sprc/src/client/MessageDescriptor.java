package client;
import java.io.Serializable;
import java.util.List;

//Arhip Alin-Gabriel Tema3 SPRC BitTorrent
// clasa ce descrie un mesaj care va fi trimis prin socketi are 
// set-ere si get-ere pentru un fisier(File) Fragment, corpul unui 
// mesaj,file descriptor,client descriptor,si ownerii unui fisier.
public class MessageDescriptor implements Serializable {

	static final long serialVersionUID = 1L;
	byte[] file=null;
	int fragment=0;
	String messageBody=null;
	FileDescriptor fileDescriptor=null;
	ClientDescriptor clientDescriptor=null;
	List<ClientDescriptor> ownersOfFile=null;
	
	public int getFragment() {
		return fragment;
	}
	public byte[] getFile() {
		return file;
	}
	public void setFile(byte[] file) {
		this.file = file;
	}
	public String getMessageBody() {
		return messageBody;
	}
	public void setFragment(int fragment) {
		this.fragment = fragment;
	}
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}
	public void setFileDescriptor(FileDescriptor fileDescriptor) {
		this.fileDescriptor = fileDescriptor;
	}
	public ClientDescriptor getClientDescriptor() {
		return clientDescriptor;
	}
	public void setClientDescriptor(ClientDescriptor clientDescriptor) {
		this.clientDescriptor = clientDescriptor;
	}
	public List<ClientDescriptor> getOwnersOfFile() {
		return ownersOfFile;
	}
	public void setOwnersOfFile(List<ClientDescriptor> ownersOfFile) {
		this.ownersOfFile = ownersOfFile;
	}
}