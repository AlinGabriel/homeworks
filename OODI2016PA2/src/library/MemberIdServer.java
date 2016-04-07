package library;
/**
 * 
 * @author Brahma Dathan and Sarnath Ramnath
 * @Copyright (c) 2010

 * Redistribution and use with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - the use is for academic purpose only
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Neither the name of Brahma Dathan or Sarnath Ramnath
 *     may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS"AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
/**
 * Generates member ids.
 * @author Brahma Dathan and Sarnath Ramnath
 *
 */
public class MemberIdServer implements Serializable {
	private static final long serialVersionUID = 1L;
	private  int idCounter;
	private static MemberIdServer server;
	/*
	 * Private constructor for singleton pattern
	 * 
	 */
	private MemberIdServer() {
		idCounter = 1;
	}
	/**
	 * Supports the singleton pattern
	 * 
	 * @return the singleton object
	 */
	public static MemberIdServer instance() {
		if (server == null) {
			return (server = new MemberIdServer());
		} else {
			return server;
		}
	}
	/**
	 * Getter for id
	 * @return id of the member
	 */
	public int getId() {
		return idCounter++;
	}
	/** 
	 * String form of the collection
	 * 
	 */
	@Override
	public String toString() {
		return ("IdServer" + idCounter);
	}
	/**
	 * Retrieves the server object
	 * 
	 * @param input inputstream for deserialization
	 */
	public static void retrieve(ObjectInputStream input) {
		try {
			server = (MemberIdServer) input.readObject();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(Exception cnfe) {
			cnfe.printStackTrace();
		}
	}
	/*
	 * Supports serialization
	 * @param output the stream to be written to
	 */
	private void writeObject(java.io.ObjectOutputStream output) throws IOException {
		try {
			output.defaultWriteObject();
			output.writeObject(server);
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	/*
	 * Supports serialization
	 * @param input the stream to be read from
	 */
	private void readObject(java.io.ObjectInputStream input) throws IOException, ClassNotFoundException {
		try {
			input.defaultReadObject();
			if (server == null) {
				server = (MemberIdServer) input.readObject();
			} else {
				input.readObject();
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}