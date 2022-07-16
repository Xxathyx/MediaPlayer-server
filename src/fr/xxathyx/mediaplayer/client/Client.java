package fr.xxathyx.mediaplayer.client;

import java.io.File;
import java.io.IOException;

public class Client {
	
	private String token;
	
	public Client(String token) {
		this.token = token;
	}
	
	public void register() {
		getClientFolder().mkdir();
		
		System.out.println(getClientFolder().getAbsolutePath());
		
		try {
			Runtime.getRuntime().exec("chmod -R 771 " + getClientFolder().getAbsolutePath());
			Runtime.getRuntime().exec("sudo chown mediaplayer /" + getClientFolder().getAbsolutePath());
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRegistered() {
		return getClientFolder().exists();
	}
	
	public File getClientFolder() {
		return new File("/var/www/html/mediaplayer/users/" + token + "/");
	}
	
	public File[] getRegisteredAudios() {
		return getClientFolder().listFiles();
	}
}