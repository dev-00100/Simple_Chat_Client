package chatclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;


/*
 * Singleton
 */
public class BotClient {
	private static BotClient botInstance;
	WebSocket ws;
	String help = "/help - display a list of commands \n "
			+ "/time - display the current time \n "
			+ "/date - display the current date \n "
			+ "/dt - display the current date and time";
	String date = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now());
	String time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now());
	String dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now()); 
	
	private BotClient() {}
	
	public static BotClient getInstance() { //BotClient.getInstance()
		if(botInstance == null) {
			botInstance = new BotClient();
		}
		return botInstance; 
	}
	
	//Establish connection
	public void connectBot() {
		CountDownLatch latch = new CountDownLatch(1);
		WebSocket.Builder client = HttpClient
			.newHttpClient()
			.newWebSocketBuilder();		
			client.header("X-USERNAME", "BOIX-BOT"); 
			ws = client.buildAsync(URI.create("ws://boixchat.herokuapp.com"), new WebSocketClient(latch)).join();
	}
	
	//Disables bot
	public void disconnectBot() {
		ws.abort();
	}
	
	/*
	 * This class is an implementation of the WebSocket.Listener interface. 
	 * The receiving interface of WebSocket. 
	 */
	private static class WebSocketClient implements WebSocket.Listener {
        private final CountDownLatch latch;

        public WebSocketClient(CountDownLatch latch) { this.latch = latch; }

        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("Bot connected" + webSocket.getSubprotocol());
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            latch.countDown();
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("BOT Error: " + error.toString());
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
	
	//Handle commands for user
	public void sendInfoLocally(String command) {
		switch(command) {
			case "/help":
				ClientUI.convo.append(help + "\n"); 
				ClientUI.convo.setCaretPosition (ClientUI.convo.getDocument ().getLength ());
				ClientUI.tf.setText("");
				break;
			
			case "/time":
				ClientUI.convo.append(time + "\n"); 
				ClientUI.convo.setCaretPosition (ClientUI.convo.getDocument ().getLength ());
				ClientUI.tf.setText("");
				break;
			
			case "/date":
				ClientUI.convo.append(date + "\n"); 
				ClientUI.convo.setCaretPosition (ClientUI.convo.getDocument ().getLength ());
				ClientUI.tf.setText("");
				break;
			
			case "/dt":
				ClientUI.convo.append(dt + "\n"); 
				ClientUI.convo.setCaretPosition (ClientUI.convo.getDocument ().getLength ());
				ClientUI.tf.setText("");
				break;
			
			case "/weather":
				ClientUI.convo.append(GetWeatherData.getInstance().getWeatherData() + "\n"); 
				ClientUI.convo.setCaretPosition (ClientUI.convo.getDocument ().getLength ());
				ClientUI.tf.setText("");
				break;
				
			default:
				break; 
		}
	}
	
	//Handle commands over web
	public void sendInfoOverWeb(String command) {
		switch(command) {
			case "/help":
				ws.sendText(help, true);
				break;
				
			case "/time":
				ws.sendText("Current time: " + time, true);
				break;
				
			case "/date":
				ws.sendText("Current date: " + date, true);
				break;
				
			case "/dt":
				ws.sendText("Current date/time: " + dt, true);
				break;
				
			case "/weather":
				ws.sendText("Current weather in " + GetWeatherData.getInstance().LOCATION + "\n" + GetWeatherData.getInstance().getWeatherData(), true);
				break;
				
			default:
				break; 
		}
	}
}
