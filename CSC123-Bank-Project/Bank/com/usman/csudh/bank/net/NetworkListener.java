package com.usman.csudh.bank.net;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.ServerCloneException;

import com.usman.csudh.bank.MainBank;

public class NetworkListener {

	public static void main(String[] args) throws IOException {
		ServerSocket server=new ServerSocket(80);
		while(true) {
			Socket sock =server.accept();
			banner(sock.getOutputStream());
			new MainBank(sock.getInputStream(), sock.getOutputStream()).run();
		}
		

	}
	
	private static void banner(OutputStream out) throws IOException{
		StringBuffer buff=new StringBuffer();
		out.write("\n\nWelcome to the Bank\n".getBytes());
		out.flush();
		
	}

}
