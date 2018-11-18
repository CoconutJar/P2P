import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class User {
	static Socket s;
	static boolean loggedIn;
	static DataOutputStream dos;
	static BufferedReader dis;
	static ArrayList<AvailableFile> availableFiles;

	public static void main(String[] args) throws IOException {
		// IP of the server to connect to.
		InetAddress ip = InetAddress.getByName("localhost");

		// Connection to server.
		s = new Socket(ip, 3158);

		String username = "bob";
		String connectionSpeed = "5";
		int port = 1234;

		// Set up input and output stream to send and receive messages.
		dis = new BufferedReader(new InputStreamReader(s.getInputStream()));
		dos = new DataOutputStream(s.getOutputStream());
		dos.writeUTF(username + " " + ip + " " + connectionSpeed + " " + port);

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
					dos.writeUTF(fileName + "<DIV>" + fileDescription);

				}
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			dos.writeUTF("505");
			System.out.println("You need a XML file with your fileList!");
		}

		loggedIn = true;
		StringTokenizer tokens;

		do {

			String command = "";

			if (command.startsWith("S:")) {

				String search = "";
				dos.writeUTF(search);
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
			} else if (command.startsWith("R: ")) {
				tokens = new StringTokenizer(command);
				String file = tokens.nextToken();
				retrieve(file);

			} else if (command.equals("QUIT")) {
				dos.writeUTF(command);
				loggedIn = false;
			}

		} while (loggedIn);

	}

	public static void retrieve(String file) {
		for (int i = 0; i < availableFiles.size(); i++) {
			if (availableFiles.get(i).fileName == file) {

				// Download File

			}
		}
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
