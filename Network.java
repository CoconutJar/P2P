

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public abstract class Network {
protected DataOutputStream dout;
protected DataInputStream dis;
protected Socket socket;

protected ServerSocket serverSocket;
protected ServerSocket server;




public void message() { 
				//System.out.println("press q to exit the program");
		String word =" ";
		try {	
		
	
		while(!word.equals("q")) {	
				dout.writeUTF(word);
			
			System.out.println(dis.readUTF());
			dout.flush();
			
		}} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
protected abstract void exit();


protected void sendfile(String path,Connections con) {
	try {

		File file = new File(path);
		InputStream in = new FileInputStream(file);
		OutputStream out = socket.getOutputStream();
		int count;
		byte[] bytes = new byte[16*1024];
		while((count = in.read(bytes))>0) {
			out.write(bytes,0,count);
		}
		 
	}catch(IOException e) {e.printStackTrace();}
}


void acceptfile(String path, Connections con) {
	FileOutputStream out;
	InputStream in=null;
try {
	in = socket.getInputStream();
}catch(IOException e) {
	e.printStackTrace();
	System.err.println("Can't get socket input stream. ");
}
	byte[] bytes = new byte[16*1024];
	int count=0;
	try {
		out = new FileOutputStream(path);
		while((count=in.read(bytes))>0) {
			out.write(bytes,0,count);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Failed in the process");
		}
	}
}

class Connections{
	InetAddress ipaddress;
	int portnumber;
	Connections(InetAddress ipaddress, int portnumber){
		this.ipaddress= ipaddress;
		this.portnumber=portnumber;
	}
	
}