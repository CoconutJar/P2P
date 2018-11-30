

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*******************************************************************************************
 * 
 * Chat+ interface.
 * 
 ******************************************************************************************/
public class GUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// buttons
	JButton iPaddress, send, connect;
	// text field
	static JTextArea chat;
	JTextField ipaddress, sent;
	JLabel ipaddressLabel;
	Client client;
	/****
	 * 
	 * Starts up the GUI
	 * 
	 ****/
	public static void main(String args[]) {
		
		GUI gui = new GUI();
		gui.setTitle("Chat Screen");
		gui.pack();
		gui.setVisible(true);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
	}
	
	/****
	 * 
	 * Sets up the User Interface.
	 * 
	 ****/
	public GUI() {
	
		JMenuBar menuBar = new JMenuBar();
		JMenu file= new JMenu("FILE");
		JMenuItem sendfile = new JMenuItem("sendfile");
		JMenuItem connection = new JMenuItem("connect");
		JMenuItem game = new JMenuItem("play Chess");
		file.add(sendfile);
		file.add(connection);
		file.add(game);
		menuBar.add(file);
		
		
		
		client = new Client();
		setLayout(new GridBagLayout());
		GridBagConstraints loc = new GridBagConstraints();

		// TEXT AREA
		chat = new JTextArea(20, 20);
		JScrollPane scrollPane = new JScrollPane(chat);
		loc.gridx = 0;
		loc.gridy = 1;
		loc.gridheight = 10;
		loc.insets.left = 20;
		loc.insets.right = 20;
		loc.insets.bottom = 20;
		add(scrollPane, loc);

		// IPADDRESS TEXT LABEL
		loc = new GridBagConstraints();
		loc.gridx = 0;
		loc.gridy = 0;
		loc.insets.bottom = 20;
		loc.insets.top = 20;
		add(new JLabel("CHAT"), loc);

		// TEXTFIELD
		ipaddress = new JTextField(15);
		loc.gridx = 2;
		loc.gridy = 3;
		loc.gridwidth = 1;
		add(ipaddress, loc);

		sent = new JTextField(15);
		loc.gridx = 2;
		loc.gridy = 9;
		loc.gridwidth = 1;
		sent.setText("Enter Message Here!");
		add(sent, loc);
		sent.setEnabled(false);

		// BUTTONS
		send = new JButton("Send Chat");
		loc.gridx = 2;
		loc.gridy = 10;
		loc.gridwidth = 1;
		add(send, loc);
		send.setEnabled(false);

		connect = new JButton("Connect");
		loc.gridx = 2;
		loc.gridy = 1;
		loc.gridwidth = 1;
		add(connect, loc);
		
		setJMenuBar(menuBar);
		
		sendfile.addActionListener(t->{
			
			SendfileGUI send = new SendfileGUI(client);
			send.run();
			
		});


		// ACTIONLISTENERS
		send.addActionListener(this);
		connect.addActionListener(this);
		
	}

	/****
	 * 
	 * When a user clicks a button the actionPerformed method is called with a
	 * parameter of the component clicked.
	 * 
	 ****/
	
	
	
	
	public void actionPerformed(ActionEvent e) {
		JComponent buttonPressed = (JComponent) e.getSource();

		// Grabs the text from a textfield and calls sendMessage.
		if (buttonPressed == send) {

			// holds message to send to server.
			String msg = sent.getText();
			try {
				client.sendMessage(msg);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			if (msg.equals("QUIT")) {

				// Disabled the connect button and field so you can connect again.
				ipaddress.setEnabled(true);
				connect.setEnabled(true);

				// Allows clients to now send messages.
				sent.setEnabled(false);
				send.setEnabled(false);
			}

		}

		// Try to form a connection using the information in the IPaddress text field.
		else if (buttonPressed == connect) {
			try {
				
				// Disabled the connect button and field so you can connect again.
				ipaddress.setEnabled(false);
				connect.setEnabled(false);

				// Allows clients to now send messages.
				sent.setEnabled(true);
				send.setEnabled(true);
				
				client.makeConnection(ipaddress.getText());
				
			} catch (IOException e1) {

			}
		}
	}

}