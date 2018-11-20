import java.io.BufferedReader;
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
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class User {
	private Socket s;
	private DataOutputStream dos;
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
		dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
		dos = new DataOutputStream(s.getOutputStream());
		dos.writeBytes(userName + " " + localHost + " " + connectionSpeed + " " + localPort);

		File fileList = new File("./filelist.xml");

		if (fileList.exists()) {

			try {

				SAXReader reader = new SAXReader();
				Document document = reader.read(fileList);
				List<Node> nodes = document.selectNodes("/filelist/file");
				dos.writeUTF("200" + " " + nodes.size());

				for (Node node : nodes) {

					String fileName = node.selectSingleNode("name").getText();
					String fileDescription = node.selectSingleNode("description").getText();
					dos.writeBytes(fileName + "<DIV>" + fileDescription);

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
						ServerSocket localServer = new ServerSocket(User.localPort); // localServerPort
						Socket client = localServer.accept();
						DataOutputStream out = new DataOutputStream(client.getOutputStream());
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						String targetFile = in.readLine();
						BufferedReader contentRead = new BufferedReader(new FileReader(targetFile));

						PrintWriter pwrite = new PrintWriter(out, true);

						String str;
						while ((str = contentRead.readLine()) != null) {
							pwrite.println(str);
						}
						contentRead.close();
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

	public void search(String keyword) {
		StringTokenizer tokens;

		try {

			dos.writeUTF(keyword);
			String str = "";
			str = dis.readLine();
			while (!str.equals("EOF")) {

				tokens = new StringTokenizer(str);
				String hostSpeed = tokens.nextToken();
				String hostName = tokens.nextToken();
				int hostPort = Integer.parseInt(tokens.nextToken());
				String hostFileName = tokens.nextToken();
				AvailableFile file = new AvailableFile(hostName, hostPort, hostFileName, hostSpeed);
				availableFiles.add(file);
				str = dis.readLine();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void retrieve(String file) throws UnknownHostException, IOException {
		for (int i = 0; i < availableFiles.size(); i++) {
			if (availableFiles.get(i).fileName == file) {
				AvailableFile targetFile = availableFiles.get(i);
				Socket ret = new Socket(targetFile.hostName, targetFile.port);
				DataOutputStream out = new DataOutputStream(ret.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(ret.getInputStream()));
				String command = "retr: " + file;
				out.writeUTF(command);
				String response = in.readLine();
				if (!response.equals("505")) {

					String str = "";
					FileWriter fw = new FileWriter("./" + file, true);
					PrintWriter writer = new PrintWriter(fw);
					while ((str = in.readLine()) != null) {
						writer.println(str);
					}
					writer.close();

				} else {
					System.out.println("File Not Found!");
				}

				in.close();
				ret.close();
			}
		}
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

	public String hostName;
	public String fileName;
	public String speed;
	public int port;

	public AvailableFile(String hn, int p, String fn, String speed) {
		this.hostName = hn;
		this.port = p;
		this.fileName = fn;
		this.speed = speed;
	}

}
