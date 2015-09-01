package net.site40.rodit.webserver.session;

import java.util.HashMap;

public class Session {
	
	private String id;
	private HashMap<String, String> properties;
	
	public Session(){
		this("");
	}
	
	public Session(String id){
		this.id = id;
		this.properties = new HashMap<String, String>();
	}
	
	public String getId(){
		return id;
	}
	
	public String read(String key){
		return properties.get(key);
	}
	
	public void set(String key, String value){
		properties.put(key, value);
	}
	
	public int readInt(String key){
		String val = read(key);
		return val != null ? Integer.valueOf(val) : 0;
	}
	
	public void setInt(String key, int value){
		set(key, value + "");
	}
	
	public boolean readBool(String key){
		String val = read(key);
		return val != null ? Boolean.valueOf(val) : false;
	}
	
	public void setBool(String key, boolean value){
		set(key, value + "");
	}
}
