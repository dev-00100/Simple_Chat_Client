package chatclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

public class ClientUI {
	//Static UI Components
	static JTextField tf; 
	static JTextArea convo;
	
	//Stores username
	static String usrName;
	
	//Web communication components
	String receivedMsg;
	WebSocket ws; 
	
	//BOT status
	boolean botIsActive; 	
	
	/*
	 * This class is an implementation of the WebSocket.Listener interface. 
	 * The receiving interface of WebSocket. 
	 */
	private class WebSocketClient implements WebSocket.Listener {
        private final CountDownLatch latch;

        public WebSocketClient(CountDownLatch latch) { this.latch = latch; }

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("Connection established" + webSocket.getSubprotocol());
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            receivedMsg = data.toString();
            if((receivedMsg.matches("^[a-zA-Z0-9_]+:\\s*/(help|time|date|dt|weather)")) && botIsActive) {
            	convo.append(receivedMsg + "\n");
            	System.out.println("inside loop");
            	receivedMsg = receivedMsg.replaceAll("^[a-zA-Z0-9_]+:\\s*", "");
            	BotClient.getInstance().sendInfoOverWeb(receivedMsg);
                return WebSocket.Listener.super.onText(webSocket, data, last);
                
            } else if((receivedMsg.matches("^[a-zA-Z0-9_]+:\\s*/(help|time|date|dt|weather)")) && (!botIsActive)) { 
            	System.out.println("Inactive");
            	convo.append(receivedMsg + "\n");
            	ws.sendText("[BOT is not active]", true);
				tf.setText("");
				return WebSocket.Listener.super.onText(webSocket, data, last);
				
			} else { 
            System.out.println("outside loop");
            convo.append(receivedMsg + "\n");
            convo.setCaretPosition (convo.getDocument ().getLength ());
            latch.countDown();
            return WebSocket.Listener.super.onText(webSocket, data, last);
			}
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("Error: " + error.toString());
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
	
	//Window listener for JFrame.
	class Closing implements WindowListener {

		@Override
		public void windowOpened(WindowEvent e) {}

		@Override
		public void windowClosing(WindowEvent e) {
					
					
			int result = JOptionPane.showConfirmDialog(null, new JLabel("<html><center>Are you sure you want to exit?<br>"
						+ "Closing the application will permanently delete the conversation.</center></html>"));
					
			if(result == JOptionPane.YES_OPTION) {
						System.exit(0);
				}
			}
				
		//Unimplemented methods
		@Override
		public void windowClosed(WindowEvent e) {}

		@Override
		public void windowIconified(WindowEvent e) {}

		@Override
		public void windowDeiconified(WindowEvent e) {}

		@Override
		public void windowActivated(WindowEvent e) {}

		@Override
		public void windowDeactivated(WindowEvent e) {}	
	}
	
	/*
	 * Action listener for text field and send button
	 */
	private class ChatListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = tf.getText();
			if(message.isBlank()) {
				JOptionPane.showMessageDialog(null, "Cannot send an empty message!", "Error", JOptionPane.ERROR_MESSAGE);
				return; 
			}
			if((message.matches("/help|/date|/time|/dt|/weather")) && botIsActive) {
				ws.sendText(message, true);
				convo.append(message + "\n"); 
				convo.setCaretPosition (convo.getDocument ().getLength ());
				tf.setText("");
				BotClient.getInstance().sendInfoLocally(message);
				return;
			} else if((message.matches("/help|/date|/time|/dt|/weather")) && (!botIsActive)) { 
				ws.sendText(message, true);
				convo.append(message + "\n"); 
				convo.append("[BOT is not active]" + "\n");
				convo.setCaretPosition (convo.getDocument ().getLength ());
				tf.setText("");
				return;
			}
			convo.append(message + "\n"); 
			ws.sendText(message, true);
			convo.setCaretPosition (convo.getDocument ().getLength ());
			tf.setText("");
		}
	}	

	//Prelaunch setup (get username)
	static {
		usrName = JOptionPane.showInputDialog(null, "Please enter a valid username:", "Boix Chat v1.0", JOptionPane.PLAIN_MESSAGE);
		while(usrName != null && usrName.isBlank()) {
			UIManager.put("OptionPane.messageForeground", Color.RED);
			usrName = JOptionPane.showInputDialog(null, "Please enter a valid username!", "Boix Chat v1.0", JOptionPane.WARNING_MESSAGE);
		}
		UIManager.put("OptionPane.messageForeground", Color.BLACK);
		if(usrName == null) {System.exit(0);} 
	}
	
	public ClientUI() {
		//Main window setup
		JFrame mainWindow = new JFrame("Java Chat v1.0");
		mainWindow.setSize(450, 500);
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Image icon = Toolkit.getDefaultToolkit()
				.getImage("res\\icon.png");
		mainWindow.setIconImage(icon);
		mainWindow.setResizable(false);
		mainWindow.setLocationRelativeTo(null);
		Closing closeAttempt = new Closing();
		mainWindow.addWindowListener(closeAttempt);
		
		//Area where conversation is displayed
		convo = new JTextArea(); 
		convo.setEditable(false);
		convo.setFont(new Font("Bold", Font.BOLD, 14));
		convo.setForeground(Color.BLUE);
		convo.setText("You are now chatting as " + usrName + "\n\n");
		convo.setLineWrap(true);
		
		//Scroll bar
		JScrollPane convoArea = new JScrollPane(convo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		convoArea.setViewportView (convo);
		
		JPanel mainPanel = new JPanel(); 
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(convoArea, BorderLayout.CENTER);
				
		JLabel instructions = new JLabel("Enter message here:");
		ChatListener chatListener = new ChatListener(); 
		
		//Where the user types their messages
		tf = new JTextField();
		tf.setToolTipText("Type your message here");
		tf.setColumns(12);
		tf.addActionListener(chatListener);
		
		//The send button
		JButton send = new JButton("Send");
		send.setToolTipText("Click to send your message");
		send.addActionListener(chatListener);
		
		JPanel usrInput = new JPanel(); 
		usrInput.setLayout(new FlowLayout());
		usrInput.setBackground(Color.LIGHT_GRAY);
		usrInput.add(instructions); usrInput.add(tf); usrInput.add(send);
		
		//Menu bar with menu and items for enabling/disabling bot.
		JMenuBar mb = new JMenuBar(); 
		JMenu menu = new JMenu("BOT");
		JRadioButtonMenuItem enable = new JRadioButtonMenuItem("Enable");
		enable.setActionCommand("enable");
		enable.addActionListener((e) -> {
			if(enable.getActionCommand().equals("enable") && !(botIsActive)) {
				botIsActive = true;
				BotClient.getInstance().connectBot();
				return; 
			}
		});
		enable.setSelected(true);
		JRadioButtonMenuItem disable = new JRadioButtonMenuItem("Disable");
		disable.setActionCommand("disable");
		disable.addActionListener((e) -> {
			if(disable.getActionCommand().equals("disable")) {
				botIsActive = false;
				BotClient.getInstance().disconnectBot();
				return; 
			}
		});
		
		//Only one radio button can be selected. 
		ButtonGroup options = new ButtonGroup();
		options.add(enable);
		options.add(disable);
		menu.add(enable); 
		menu.add(disable); 
		mb.add(menu); 
		
		//Add components to frame. 
		mainWindow.add(mb, BorderLayout.NORTH); 
		mainWindow.add(mainPanel, BorderLayout.CENTER); 
		mainWindow.add(usrInput, BorderLayout.SOUTH); 
		
		//Establish connection
		CountDownLatch latch = new CountDownLatch(1);
		WebSocket.Builder client = HttpClient
				.newHttpClient()
				.newWebSocketBuilder();		
		client.header("X-USERNAME", usrName); 
		ws = client.buildAsync(URI.create("ws://boixchat.herokuapp.com"), new WebSocketClient(latch)).join();
		
		/*
		 * Establish bot connection. Bot is enabled
		 * by default.
		 */
		if(enable.isSelected()) {
			botIsActive = true;
			BotClient.getInstance().connectBot();
		}
		
		//Display the UI
		mainWindow.setVisible(true);
	}	
}
