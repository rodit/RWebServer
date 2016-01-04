package net.site40.rodit.webserver.session;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Pattern;

import net.site40.rodit.webserver.util.FileUtil;

public class Session {
	
	public static final long MEM_KEEP_TIME = 5L * 60000L;
	public static final File SESSIONS_DIR = new File("sessions");
	
	static{
		if(!SESSIONS_DIR.exists())
			SESSIONS_DIR.mkdir();
	}
	
	private long lastUsed = 0L;
	private File file;
	private boolean loaded = false;
	
	private String id;
	private HashMap<String, String> properties;
	
	public Session(){
		this("");
	}
	
	public Session(String id){
		this.file = new File(SESSIONS_DIR, id);
		if(file.exists())
			file.delete();
		this.id = id;
		this.properties = new HashMap<String, String>();
		this.loaded = true;
		used();
	}
	
	protected void used(){
		lastUsed = System.currentTimeMillis();
		if(!loaded)
			load();
	}
	
	protected void load(){
		String full = FileUtil.readString(file);
		for(String line : full.split("\n")){
			if(line.isEmpty())
				continue;
			String[] parts = line.split(Pattern.quote(":"));
			if(parts.length != 2)
				continue;
			properties.put(parts[0], parts[1]);
		}
		this.loaded = true;
	}
	
	protected void save(){
		StringBuilder full = new StringBuilder();
		for(String key : properties.keySet())
			full.append(key + ":" + properties.get(key) + "\n");
		FileUtil.write(file, full.toString().getBytes());
		properties.clear();
		this.loaded = false;
	}
	
	public String getId(){
		return id;
	}
	
	public String read(String key){
		used();
		return properties.get(key);
	}
	
	public void set(String key, String value){
		used();
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
	
	public void update(){
		if(System.currentTimeMillis() - lastUsed >= MEM_KEEP_TIME && !loaded)
			save();
	}
}
