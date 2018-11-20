import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class User {
	private Socket s;
	private DataOutputStream dos;
	private DataInputStream is;
	private BufferedReader dis;
	private ArrayList<AvailableFile> availableFiles;
	private String userName;
	private String localHost;
	private String serverPort;
	private String connectionSpeed;
	private boolean loggedOn;

	/****
	 * 
	 * Forms a connection to the Centralized_Server and starts the local server
	 * thread.
	 * 
	 ****/
	public void makeConnection(String userName, String serverHostName, String serverPort, String connectionSpeed,
			String localHost, String localPort) throws IOException {

		InetAddress ip = InetAddress.getByName("localhost");
		// Connection to server.
		s = new Socket(ip, Integer.parseInt(serverPort));

		// IP of the server to connect to.
		this.localHost = localHost;
		this.userName = userName;
		this.connectionSpeed = connectionSpeed;

		// Set up input and output stream to send and receive messages.
		is = new DataInputStream(s.getInputStream());

		dos = new DataOutputStream(s.getOutputStream());

		// Sends the initial connectionString that holds information about the client.
		dos.writeUTF(userName + " " + localHost + " " + connectionSpeed + " " + localPort);

		// Checks to see in the client has a XML file called fileList that holds
		// information of files available for download.
		File fileList = new File("./filelist.xml");
		if (fileList.exists()) {

			// Code From
			// https://www.tutorialspoint.com/java_xml/java_dom4j_parse_document.htm
			// This code parses the XML file with the filelist on it and sends the file
			// information to the Central server.
			try {

				SAXReader reader = new SAXReader();
				Document document = reader.read(fileList);
				System.out.println("Root element :" + document.getRootElement().getName());

				Element classElement = document.getRootElement();

				List<Node> nodes = document.selectNodes("/filelist/file");

				// Let the server know data is coming and how much
				dos.writeUTF("200" + " " + nodes.size());

				for (Node node : nodes) {

					String fileName = node.selectSingleNode("name").getText();
					String fileDescription = node.selectSingleNode("description").getText();
					dos.writeUTF(fileName + "$" + fileDescription);

				}
			} catch (DocumentException e) {
				e.printStackTrace();
			}

		} else {

			// If the XML file isnt found let the server and client know!
			dos.writeUTF("505");
			System.out.println("You need a XML file with your fileList!");
		}

		loggedOn = true;

		// Handles other clients when they need to get a file from this localServer.
		Thread localServer = new Thread(new Runnable() {
			public void run() {
				while (loggedOn) {
					try {
						ServerSocket localServer = new ServerSocket(Integer.parseInt(localPort)); // localServerPort
						Socket client = localServer.accept();

						DataOutputStream out = new DataOutputStream(client.getOutputStream());
						DataInputStream in = new DataInputStream(client.getInputStream());

						// Read in the request from the server.
						String command = in.readUTF();
						StringTokenizer tokens = new StringTokenizer(command);

						String targetFile = tokens.nextToken();
						targetFile = tokens.nextToken();

						// Checks to see if the targetFile exists on this server.
						File file = new File("./" + targetFile);
						if (file.exists()) {

							// Tell the client we have the file.
							out.writeUTF("200");
							BufferedReader contentRead = new BufferedReader(new FileReader(targetFile));

							PrintWriter pwrite = new PrintWriter(out, true);

							String str;
							while ((str = contentRead.readLine()) != null) {
								pwrite.println(str);
							}
							contentRead.close();
						} else {

							// Tell the client the file could not be found.
							out.writeUTF("505");
						}
						client.close();
						localServer.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		// Start up the local Server.
		localServer.start();
	}

	/****
	 * 
	 * Allows the client to search the Central Server for available files to
	 * download.
	 * 
	 ****/
	public boolean search(String keyword) {
		StringTokenizer tokens;

		try {

			dos.writeUTF(keyword);
			String str = "";

			// If no files match the search 'str' will equal EOF
			str = is.readUTF();

			availableFiles = new ArrayList<AvailableFile>();

			// While there are more files to read. Read them.
			while (!str.equals("EOF")) {

				tokens = new StringTokenizer(str);
				String hostSpeed = tokens.nextToken();
				String hostName = tokens.nextToken();
				int hostPort = Integer.parseInt(tokens.nextToken());
				String hostFileName = tokens.nextToken();
				String hostUserName = tokens.nextToken();

				// Store the file information in an AvailableFile object.
				AvailableFile file = new AvailableFile(hostUserName, hostName, hostPort, hostFileName, hostSpeed);
				availableFiles.add(file);
				str = is.readUTF();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Return whether or not matches were found
		if (availableFiles.isEmpty())
			return false;
		else
			return true;
	}

	/****
	 * 
	 * Returns the List of Available Files.
	 * 
	 ****/
	public ArrayList<AvailableFile> getAvailableFiles() {
		return availableFiles;
	}

	/****
	 * 
	 * Checks to see if the file requested is in the available files list on the
	 * server. If the file is available the client forms a socket with the server
	 * that holds the file. The client then downloads the file.
	 * 
	 ****/
	public boolean retrieve(String file) throws UnknownHostException, IOException {
		boolean downloaded = false;
		for (int i = 0; i < availableFiles.size(); i++) {

			if (availableFiles.get(i).fileName.equals(file) && !availableFiles.get(i).hostUserName.equals(userName)) {

				AvailableFile targetFile = availableFiles.get(i);
				InetAddress ip = InetAddress.getByName("localhost");

				// New Socket for file Transfer.
				Socket ret = new Socket(ip, targetFile.port);

				DataOutputStream out = new DataOutputStream(ret.getOutputStream());
				DataInputStream din = new DataInputStream(ret.getInputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(ret.getInputStream()));

				String command = "retr: " + file;
				out.writeUTF(command);

				String response = din.readUTF();

				// If the server has the file start the download else nothing.
				if (!response.equals("505")) {

					String str = "";
					File newFile = new File("./" + file);
					FileWriter fw = new FileWriter("./" + file);
					PrintWriter writer = new PrintWriter(fw);

					// Read in the file.
					while ((str = in.readLine()) != null) {
						writer.println(str);
					}
					writer.close();
					downloaded = true; // download flag.

				} else {
					System.out.println("File Not Found!");
				}

				in.close();
				ret.close();
			}
		}
		return downloaded;
	}

	/****
	 * 
	 * Sends a message to the server closing the connection.
	 * 
	 ****/
	public void quit() {

		try {
			dos.writeUTF("QUIT");
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Ends the thread.
		loggedOn = false;
	}
}

/*******************************************************************************************
 * 
 * Holds the information for the available files that matched in the search.
 * 
 ******************************************************************************************/
class AvailableFile {

	public String hostUserName;
	public String hostName;
	public String fileName;
	public String speed;
	public int port;

	public AvailableFile(String hostUserName, String hn, int p, String fn, String speed) {
		this.hostUserName = hostUserName;
		this.hostName = hn;
		this.port = p;
		this.fileName = fn;
		this.speed = speed;
	}

}
