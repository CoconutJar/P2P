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
	private static int localPort;
	private boolean loggedOn;

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

		dos.writeUTF(userName + " " + localHost + " " + connectionSpeed + " " + localPort);

		File fileList = new File("./filelist.xml");

		if (fileList.exists()) {

			try {

				SAXReader reader = new SAXReader();
				Document document = reader.read(fileList);
				System.out.println("Root element :" + document.getRootElement().getName());

				Element classElement = document.getRootElement();

				List<Node> nodes = document.selectNodes("/filelist/file");

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
			dos.writeUTF("505");
			System.out.println("You need a XML file with your fileList!");
		}

		loggedOn = true;

		Thread localServer = new Thread(new Runnable() {
			public void run() {
				while (loggedOn) {
					try {
						ServerSocket localServer = new ServerSocket(Integer.parseInt(localPort)); // localServerPort
						Socket client = localServer.accept();
						DataOutputStream out = new DataOutputStream(client.getOutputStream());
						DataInputStream in = new DataInputStream(client.getInputStream());
						String command = in.readUTF();
						StringTokenizer tokens = new StringTokenizer(command);

						String targetFile = tokens.nextToken();
						targetFile = tokens.nextToken();

						File file = new File("./" + targetFile);
						System.out.println(targetFile);
						if (file.exists()) {
							out.writeUTF("200");
							BufferedReader contentRead = new BufferedReader(new FileReader(targetFile));

							PrintWriter pwrite = new PrintWriter(out, true);

							String str;
							while ((str = contentRead.readLine()) != null) {
								pwrite.println(str);
							}
							contentRead.close();
						} else {
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

		localServer.start();
	}

	public boolean search(String keyword) {
		StringTokenizer tokens;

		try {

			dos.writeUTF(keyword);
			String str = "";
			System.out.println("rec");
			str = is.readUTF();

			availableFiles = new ArrayList<AvailableFile>();
			while (!str.equals("EOF")) {

				tokens = new StringTokenizer(str);
				String hostSpeed = tokens.nextToken();
				String hostName = tokens.nextToken();
				int hostPort = Integer.parseInt(tokens.nextToken());
				String hostFileName = tokens.nextToken();
				String hostUserName = tokens.nextToken();
				AvailableFile file = new AvailableFile(hostUserName, hostName, hostPort, hostFileName, hostSpeed);
				availableFiles.add(file);
				str = is.readUTF();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (availableFiles.isEmpty())
			return false;
		else
			return true;
	}

	public ArrayList<AvailableFile> getAvailableFiles() {
		return availableFiles;
	}

	public boolean retrieve(String file) throws UnknownHostException, IOException {
		boolean downloaded = false;
		for (int i = 0; i < availableFiles.size(); i++) {

			if (availableFiles.get(i).fileName.equals(file) && !availableFiles.get(i).hostUserName.equals(userName)) {
				System.out.println("found");
				AvailableFile targetFile = availableFiles.get(i);
				InetAddress ip = InetAddress.getByName("localhost");
				Socket ret = new Socket(ip, targetFile.port);
				DataOutputStream out = new DataOutputStream(ret.getOutputStream());
				DataInputStream din = new DataInputStream(ret.getInputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(ret.getInputStream()));
				String command = "retr: " + file;
				out.writeUTF(command);
				String response = din.readUTF();
				if (!response.equals("505")) {
					System.out.println("here");
					String str = "";
					File newFile = new File("./" + file);
					FileWriter fw = new FileWriter("./" + file);
					PrintWriter writer = new PrintWriter(fw);
					while ((str = in.readLine()) != null) {
						writer.println(str);
					}
					writer.close();
					downloaded = true;

				} else {
					System.out.println("File Not Found!");
				}

				in.close();
				ret.close();
			}
		}
		return downloaded;
	}

	public void quit() {

		try {
			dos.writeUTF("QUIT");
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		loggedOn = false;
	}
}

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
