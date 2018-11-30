

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SendfileGUI{
	private String ip;
	private String name;
	private Client client;
	
  public SendfileGUI(Client client) {
	  this.client = client;
  }

  public void run() {
	 JFrame f = new JFrame();
	 JPanel panel = new JPanel();
	 f.getContentPane().add(panel);
	 f.setSize(400, 300);
	 f.setVisible(true);

	  
    
    GridBagConstraints constraints = new GridBagConstraints();
    GridBagLayout layout = new GridBagLayout();
    panel.setLayout(layout);
    constraints.insets= new Insets(2,2,2,2);
    constraints.anchor = GridBagConstraints.WEST;

    constraints.gridy = 0;
    JLabel label = new JLabel("Ip address: ");
    panel.add(label, constraints);

    JTextField tf1 = new JTextField(20);
    tf1.setText("localhost");
    
    tf1.addMouseListener(new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(arg0.getClickCount()==2) {
				tf1.setText("");
			}
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    });
    panel.add(tf1, constraints);
    

    constraints.gridy = 1;
    label = new JLabel("Your directory: ");
    panel.add(label, constraints);
    
    JTextField tf2 = new JTextField(20);
    
    tf2.addMouseListener(new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(arg0.getClickCount()==2) {
				tf2.setText("");
			}
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    });

    panel.add(tf2, constraints);
    
    constraints.gridy=2;
    label = new JLabel("There directory");
    panel.add(label, constraints);
    
    JTextField tf3 = new JTextField(20);
    tf3.addMouseListener(new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if(arg0.getClickCount()==2) {
				tf3.setText("");
			}
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			 
		}
    	
    });    
    panel.add(tf3, constraints);
    
    constraints.gridy=3;
    constraints.gridx=1;
    JButton connect = new JButton("connect");
      panel.add(connect, constraints);
      connect.addActionListener(t->{
    	  String ip = tf1.getText();
    	  String port=tf2.getText();
    	  String file=tf3.getText();
    	  
    	  try {
			client.sendFile(ip, port, file);
    	  } catch (IOException e) {
    		  e.printStackTrace();
    	  }
    	  
      });

  }
}