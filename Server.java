
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

/*******************************************************************************************
 * 
 * Forms connections with clients.
 * 
 ******************************************************************************************/
public class Server  {

	// Socket that awaits client connections.
	private static ServerSocket welcomeSocket;

	// Holds all client UserNames that have connected to the server.
	public static ArrayList<ClientHandler> con = new ArrayList<ClientHandler>();

	/****
	 * 
	 * Starts up a ServerSocket and constantly waits for clients connections and
	 * starts a thread for
	 * 
	 ****/
	public static void main(String[] args) throws IOException {

		try {
			welcomeSocket = new ServerSocket(3158); // ServerPort
			
		} catch (Exception e) {
			System.err.println("ERROR: Server could not be started.");
		}

		try {
			while (true) {

				// Waits for a client to connect.
				Socket connectionSocket = welcomeSocket.accept();
				
				String userIP = connectionSocket.getInetAddress().toString();

				System.out.println(connectionSocket.getRemoteSocketAddress().toString() + " has connected!");

				// Set up input and output stream with the client to send and receive messages.
				DataInputStream dis = new DataInputStream(connectionSocket.getInputStream());
				DataOutputStream dos = new DataOutputStream(connectionSocket.getOutputStream());

				// Creates a clientHandler object with the client.
				ClientHandler client = new ClientHandler(connectionSocket, dis, dos, userIP);

				// Adds the client to the arrayList of clients.
				con.add(client);

				// Makes a thread to allow the client and clientHandler to interact.
				Thread t1 = new Thread(client);
				t1.start();

			}

		} catch (Exception e) {
			System.err.println("ERROR: Connecting Client");
			e.printStackTrace();

		} finally {
			try {
				// Close the Socket in the event of an error.
				welcomeSocket.close();
				System.out.println("Server socket closed.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}

/*******************************************************************************************
 * 
 * Handles the client.
 * 
 ******************************************************************************************/
class ClientHandler implements Runnable {

	Socket connectionSocket;
	String fromClient;
	String clientName;
	String ip;
	DataInputStream dis;
	DataOutputStream dos;
	boolean loggedIn;
	int port;

	/****
	 * 
	 * 
	 * 
	 ****/
	public ClientHandler(Socket connectionSocket, DataInputStream dis, DataOutputStream dos, String ip) {

		this.connectionSocket = connectionSocket;
		this.dis = dis;
		this.dos = dos;
		this.loggedIn = true;
		this.ip = ip;
		

	}

	/****
	 * 
	 * Runs after the start function is called. Uses a do while loop to process
	 * messages received by the server.
	 * 
	 ****/
	@Override
	public void run() {

		String name;
		try {

			// Sets the first string received as the UserName for the client.
			name = dis.readUTF();
			StringTokenizer st = new StringTokenizer(name);
			name = st.nextToken();
			String port2 = st.nextToken();
			port = Integer.parseInt(port2);
			this.clientName = name;

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		System.out.println("-- " + clientName + " is ready to chat! --");

		try {
	
			// Do while conditional.
			boolean hasNotQuit = true;

			// Breaks down the messages received by the client into a command.
			do {

				// Waits for data.
				fromClient = dis.readUTF();
				System.out.println(fromClient);

				// If the message is '-list' then the server responses with a list of
				// online users.
				if (fromClient.equals("-list")) {

					dos.writeUTF("Users currently online:");

					// Iterates through the server list of users and sends the
					// UserName of any client online.
					for (int i = 0; i < Server.con.size(); i++) {
						if (Server.con.get(i).loggedIn)
							dos.writeUTF(Server.con.get(i).ip + " " + Server.con.get(i).clientName + " "+ Server.con.get(i).port);
					}

					// If the message is 'QUIT' set the while loop conditional to false.
				} else if (fromClient.equals("QUIT")) {

					hasNotQuit = false;

					// Helps the Users
				} else if (fromClient.equals("-help")) {

					dos.writeUTF("Enter '-list' to see who's available for messaging.\n"
							+ "To send a message: <message> <recipicant>\n." + "Enter 'QUIT' to exit.");

					// If the message is not a command then it is assumed the client is trying to
					// send a message.
				} else {

					// Break the string into message and recipient part.
					StringTokenizer st = new StringTokenizer(fromClient);
					String message = "";
					int number = st.countTokens() - 1;

					for (int i = 0; i < number; i++) {
						message += st.nextToken() + " ";
					}

					// Sets the last token to the recipient.
					String recipient = st.nextToken();
					boolean found = false;

					// Looks for the recipient userName in the server list of users.
					for (ClientHandler c : Server.con) {

						// If the recipient UserName is in the list and online then the message is sent.
						if (c.clientName.equals(recipient) && c.loggedIn == true) {

							// Grab the recipient's outputStream.
							c.dos.writeUTF(this.clientName + " sent : " + message);
							found = true;
							break;
						}
					}

					// If the Client is either offline or is not found in the arrayList send the
					// client a message.
					if (!found) {
						dos.writeUTF("Cant find user " + recipient
								+ "\nEnter '-list' to see who's available for messaging \n:)");
					}
				}
				
			
			} while (hasNotQuit);

			// Set the online status to offline.
			this.loggedIn = false;

			// Close the Socket.
			this.connectionSocket.close();
			System.out.println(clientName + " has disconnected!");

		} catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		}
	}
}