package fr.xxathyx.mediaplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import fr.xxathyx.mediaplayer.client.Client;
import fr.xxathyx.mediaplayer.handler.Handler;

public class Server {
	
	public final static Map<InetAddress, String> clients = new HashMap<>();
	public final static Map<InetAddress, String> uploading = new HashMap<>();
	
	public final static Map<InetAddress, Handler> handlers = new HashMap<>();
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		
        System.out.println("Service started, waiting for connection");
        
		try {
						
			ServerSocket serverSocket = new ServerSocket(8888);
			
	        while(!serverSocket.isClosed()) {
	        	
		        Socket socket = serverSocket.accept();
    	        
				System.out.println("Connected: " + socket.getInetAddress().getHostAddress());

				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				String header = dataInputStream.readUTF();
				
				if(header.contains("mediaplayer.register: ")) {
					
					String token = header.replace("mediaplayer.register: ", "");
					Client client = new Client(token);
					
					if(!client.isRegistered()) {
						client.register();
					}
					
					System.out.println("Registered: " + token);
										
					DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
					dataOutputStream.writeUTF("Your token has been successfully registered. (keep it confidential)");
					
					System.out.println("Authentified: " + socket.getInetAddress().toString() + " : " + token);
					dataOutputStream.writeUTF("Successfully connected to audio server handling.");
					
					clients.put(socket.getInetAddress(), token);
					
					Handler handler = new Handler(socket);
					handler.run();
					
					handlers.put(socket.getInetAddress(), handler);
				}
				
				if(header.contains("mediaplayer.connect: ")) {
					
					String token = header.replace("mediaplayer.connect: ", "");
					Client client = new Client(token);
					
					if(client.isRegistered()) {
						
						clients.put(socket.getInetAddress(), token);
						
						System.out.println("Authentified: " + socket.getInetAddress().toString() + " : " + token);
						
						DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
						dataOutputStream.writeUTF("Successfully connected to audio server handling.");
						
						Handler handler = new Handler(socket);
						handler.run();
						
						handlers.put(socket.getInetAddress(), handler);
						
					}else {
						DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
						dataOutputStream.writeUTF("Your token seems to be not registered.");
			            dataOutputStream.close();
					}
				}
	            dataInputStream.close();
				socket.close();
	        }
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}