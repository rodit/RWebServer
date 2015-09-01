package net.site40.rodit.webserver.permissions;

import java.io.File;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import net.site40.rodit.webserver.Configuration;
import net.site40.rodit.webserver.util.FileUtil;
import net.site40.rodit.webserver.util.Log;

import com.sun.net.httpserver.HttpExchange;

public class AuthenticationManager {

	private Log log = new Log("Auth");
	private HashMap<File, File> locations;

	public static enum AuthScope{
		DIR, FILE, RECURSIVE, NONE
	}

	private Configuration config;

	public AuthenticationManager(Configuration config){
		this.config = config;
		this.locations = new HashMap<File, File>();
	}

	public boolean authenticate(HttpExchange exchange, File file){
		return authenticate(exchange, file, false);
	}
	
	public boolean authenticate(HttpExchange exchange, File file, boolean delete){
		if(!config.readBool("server.authentication"))
			return true;
		File dir = getDir(file.getPath());
		File auth = new File(dir, ".auth");
		log.i("Consulting auth file @ " + auth.getPath() + ".");
		if(!auth.exists()){
			log.i("Auth file was not found. Checking parent locations...");
			for(File location : locations.keySet()){
				if(isSubDir(location, file)){
					FileUtil.write(auth, FileUtil.read(locations.get(location)));
					return authenticate(exchange, file, true);
				}
			}
			return true;
		}
		locations.put(dir, auth);
		String authTxt = FileUtil.readString(auth);
		AuthScope scope = AuthScope.NONE;
		String scopeArgs = "";
		String realm = "Restricted Page";
		String hashes = "";
		for(String line : authTxt.split("\n")){
			if(line.trim().startsWith("#"))
				continue;
			String[] words = line.trim().split("\\s+");
			if(words.length < 2){
				log.i("Invalid line in auth file '" + line + "'.");
				continue;
			}
			String kw = words[0];
			if(kw.equals("scope")){
				try{
					scope = AuthScope.valueOf(words[1].toUpperCase());
				}catch(Exception e){
					scope = AuthScope.NONE;
					continue;
				}
				if(scope == AuthScope.FILE && words.length > 2)
					scopeArgs = words[2];
			}else if(kw.equals("pass"))
				hashes = FileUtil.readString(words[1]);
			else if(kw.equals("realm"))
				realm = line.substring(6);
		}
		boolean authed = false;
		if(fits(file, scope, scopeArgs)){
			String supplied = exchange.getRequestHeaders().getFirst("Authorization");
			if(supplied != null)
				for(String hash : hashes.split("\n"))
					if(supplied.equals("Basic " + DatatypeConverter.printBase64Binary(hash.getBytes())))
						authed = true;
		}else
			authed = true;
		if(delete)
			auth.delete();
		if(!authed)
			exchange.getResponseHeaders().set("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		return authed;
	}

	public boolean fits(File file, AuthScope scope, String scopeArgs){
		return !(scope == AuthScope.NONE || (scope == AuthScope.FILE && !file.getName().equals(scopeArgs)));
	}

	public boolean isSubDir(File parent, File sub){
		return sub.getAbsolutePath().startsWith(parent.getAbsolutePath());
	}

	public File getDir(String file){
		File f = new File(file);
		if(f.isDirectory())
			return f;
		return f.getParentFile();
	}

	public String hash(String username, String password){
		return username + ":" + password;
	}
}
