
import java.awt.Frame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class Client  {
	
	Socket s;
	DataInputStream dis;
	boolean loggedIn;
	DataOutputStream dos;
	Client client;
	
	
	/****
	 * 
	 * Sends the messages to the server using the output stream. If the message is
	 * 'QUIT' the client disconnects from the server.
	 * 
	 ****/
	public void sendMessage(String message) throws IOException {

		// Send the message using TCP.
		dos.writeUTF(message);

		if (message.equals("QUIT")) {

			// Closes the connection.
			s.close();
			loggedIn = false;
		}

	}
	
	public void sendFile(String userIP, String userPort, String filename) throws IOException {
		if(new File(filename).exists()) {
			int port = Integer.parseInt(userPort);
			Socket sock = new Socket(userIP, port);
			DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
			FileInputStream fis = new FileInputStream(filename);
			byte[] buffer = new byte[4096];
			
			while (fis.read(buffer) > 0) {
				dos.write(buffer);
			}
			
			fis.close();
			dos.close();
			sock.close();
		}
		else {
			Files.write(Paths.get(filename),JOptionPane.showInputDialog(new Frame(), "This file is currently empty. Write something to it?").getBytes());
		}
	}
	
	/****
	 * 
	 * Forms the TCP socket connection to the server in order to receive and send
	 * messages.
	 * 
	 ****/
	public void makeConnection(String Name) throws IOException {

		// IP of the server to connect to.
		//InetAddress ip = InetAddress.getByName("localhost");
		
		StringTokenizer st = new StringTokenizer(Name);
		String ip = st.nextToken();
		Name = st.nextToken();
		String port = st.nextToken();
		
		// Connection to server.
		s = new Socket(ip, 3158);

		// Set up input and output stream to send and receive messages.
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());

		// Sets logged in status to true.
		loggedIn = true;

		// Sends the UserName of the client to the server.
		dos.writeUTF(Name + " " + port);
		
		Thread recieveFiles = new Thread(new Runnable() {
			@Override
			public void run() {

				// Will hold all messages received from server.
				String chatText = "";
				ServerSocket sock = null;
				try {
					sock = new ServerSocket(Integer.parseInt(port));
				} catch (NumberFormatException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// If the client is loggedOff they wont receive any messages.
				while (loggedIn) {

					// If the socket is still open.
					// Read the message sent to this client.
					try {
						Socket s = sock.accept();
							
						try {
									
							DataInputStream datainput = new DataInputStream(s.getInputStream());
							FileOutputStream fos = new FileOutputStream("file");
							byte[] buffer = new byte[4096];
								
							int filesize = 15123; // Send file size in separate msg
							int read = 0;
							int totalRead = 0;
							int remaining = filesize;
							while((read = datainput.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
								totalRead += read;
								remaining -= read;
								System.out.println("read " + totalRead + " bytes.");
								fos.write(buffer, 0, read);
							}
							
							fos.close();
							datainput.close();
							s.close();
						}
						catch(IOException e) {
							e.printStackTrace();
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		});

		// Starts the thread.
		recieveFiles.start();
		
		// Responsible for reading in any data from the server input stream.
		// Adds any text received to the chatText box.
		Thread recieveMessages = new Thread(new Runnable() {
			@Override
			public void run() {

				// Will hold all messages received from server.
				String chatText = "";

				// If the client is loggedOff they wont receive any messages.
				while (loggedIn) {

					// If the socket is still open.
					// Read the message sent to this client.
					try {
						if (!s.isClosed()) {
							while (dis.available() > 0) {
								String msg = dis.readUTF();
								chatText += msg + "\n";
								
								// Update the chat in the GUI.
								GUI.chat.setText(chatText);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		});

		// Starts the thread.
		recieveMessages.start();

	}
}