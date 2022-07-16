package fr.xxathyx.mediaplayer.handler;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;

import fr.xxathyx.mediaplayer.Server;
import fr.xxathyx.mediaplayer.client.Client;

public class Handler implements Runnable {
	
	private Socket socket;
	private Client client;
	
	private boolean uploading = false;
	
	public Handler(Socket socket) {
		this.socket = socket;
		this.client = new Client(Server.clients.get(socket.getInetAddress()));
	}
	
	@Override
	public void run() {
				
		while(!socket.isClosed()) {
			
			try {
				
				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                String header = dataInputStream.readUTF();
                				
                uploading = header.contains("mediaplayer.upload: ");
                
                if(uploading) {
                	
                	String name = header.replace("mediaplayer.upload: ", "");
                	
                    receiveFile(dataInputStream, (new File(client.getClientFolder(), String.valueOf(name) + ".zip")).getAbsolutePath());
                    
                    uploading = false;
                } 
				
				if(header.contains("mediaplayer.disconnect: ")) {
												            
		            dataInputStream.close();
		            Server.clients.remove(socket.getInetAddress());
		            Server.handlers.remove(socket.getInetAddress());
		            if(Server.uploading.containsKey(socket.getInetAddress())) Server.uploading.remove(socket.getInetAddress());
		            socket.close();
				}
			}catch (Exception e) {
			    e.printStackTrace();
			}
		}
	}
	
    private static void receiveFile(DataInputStream dataInputStream, String fileName) throws Exception {
    	
        int bytes = 0;
        
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        
        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4*1024];
        
        while(size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();
    }
}