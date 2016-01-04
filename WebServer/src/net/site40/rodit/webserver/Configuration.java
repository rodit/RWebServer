package net.site40.rodit.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import net.site40.rodit.webserver.util.Log;

public class Configuration {
	
	private static final String KVP_SEPERATOR = "=";
	private static final String KVP_LINE_SEPERATOR = "\n";

	private Log log = new Log("Config");
	private LinkedHashMap<String, String> properties;
	
	public Configuration(){
		properties = new LinkedHashMap<String, String>();
	}
	
	public String read(String key){
		return properties.get(key);
	}
	
	public void write(String key, String value){
		properties.put(key, value);
	}
	
	public int readInt(String key){
		return Integer.valueOf(read(key));
	}
	
	public void write(String key, int value){
		write(key, value + "");
	}
	
	public boolean readBool(String key){
		return Boolean.valueOf(read(key));
	}
	
	public void write(String key, boolean value){
		write(key, value + "");
	}
	
	public void writeDefaults(){
		//server config
		write("server.port", 1337);
		write("server.local", false);
		write("server.dir", "htdocs");
		write("server.index", "index.rsc");
		write("server.list", true);
		write("server.permissions", true);
		write("server.sessions", true);
		write("server.sessions.ipcheck", true);
		write("server.sessions.clear", true);
		write("server.authentication", true);
		write("server.https", false);
		write("server.keystore", "keystore.jks");
		write("server.keystorepassword", "securepassword");
		write("server.shell", true);
		write("server.notfound", "res/notfound.rsc");
		write("server.forbidden", "res/forbidden.rsc");
		write("server.notauthed", "res/notauthed.rsc");
		//log config
		write("log.write", true);
		write("log.writestdout", true);
		write("log.file", "server.log");
		write("log.clear", false);
		//rscript config
		write("rscript.enabled", true);
		//plugins
		write("plugins.enabled", true);
		write("plugins.dir", "plugins");
		//mime types
		write("mime.html", "text/html");
		write("mime.txt", "text/plain");
		write("mime.css", "text/css");
		write("mime.json", "application/json");
		write("mime.xml", "application/xml");
		write("mime.mp3", "audio/mpeg");
		write("mime.mpeg", "video/mpeg");
		write("mime.mp4", "video/mp4");
		write("mime.*", "application/octet-stream");
	}
	
	public String getMime(String extension){
		String mime = read("mime." + extension);
		if(mime == null)
			mime = read("mime.*");
		return mime;
	}
	
	public void writeMime(String extension, String type){
		write("mime." + extension, type);
	}
	
	public void load(String file){
		if(!new File(file).exists()){
			writeDefaults();
			save(file);
			return;
		}
		try{
			FileInputStream fin = new FileInputStream(file);
			byte[] buff = new byte[fin.available()];
			fin.read(buff);
			fin.close();
			String conf = new String(buff);
			for(String line : conf.split(KVP_LINE_SEPERATOR)){
				String[] parts = line.split(Pattern.quote(KVP_SEPERATOR));
				write(parts[0], parts[1]);
			}
		}catch(IOException e){
			log.e("Error while loading configuration - " + e.getMessage());
		}
	}
	
	public void save(String file){
		try{
			FileOutputStream fout = new FileOutputStream(file);
			String full = "";
			for(String key : properties.keySet())
				full += key + KVP_SEPERATOR + properties.get(key) + KVP_LINE_SEPERATOR;
			if(!full.isEmpty())
				full = full.substring(0, full.length() - 1);
			fout.write(full.getBytes());
			fout.flush();
			fout.close();
		}catch(IOException e){
			log.e("Error while saving configuration - " + e.getMessage());
		}
	}
}
