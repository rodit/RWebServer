package net.site40.rodit.webserver.permissions;

import java.io.File;
import java.util.HashMap;

import net.site40.rodit.webserver.Configuration;
import net.site40.rodit.webserver.util.FileUtil;
import net.site40.rodit.webserver.util.Log;

import com.sun.net.httpserver.HttpExchange;

public class PermissionManager {

	public static enum PermissionWord{
		IP, LOCAL, EXTERNAL, ALL
	}

	public static enum PermissionScope{
		FILE, DIR, RECURSIVE, NONE
	}

	private Log log = new Log("Permissions");
	private Configuration config;
	private HashMap<File, File> locations;

	public PermissionManager(Configuration config){
		this.config = config;
		this.locations = new HashMap<File, File>();
	}
	
	public boolean canAccess(HttpExchange exchange, File file){
		return canAccess(exchange, file, false);
	}
	
	public boolean canAccess(HttpExchange exchange, File file, boolean deletePerms){
		try{
		if(!file.canRead())
			return false;
		if(!config.readBool("server.permissions"))
			return true;
		if(file.getName().equals(".access") || file.getName().equals(".auth") || file.getName().equals(".pass"))
			return false;
		File dir = getDir(file.getPath());
		File perms = new File(dir, ".access");
		if(perms.length() == 0)
			perms.delete();
		log.i("Consulting access file @ " + perms.getPath() + ".");
		if(!perms.exists()){
			log.i("Access file was not found. Checking parent locations...");
			for(File location : locations.keySet()){
				log.i("Checking location " + location.getPath());
				if(isSubDir(location, file)){
					FileUtil.write(perms, FileUtil.read(locations.get(location)));
					return canAccess(exchange, file, true);
				}
			}
			File newLoc = new File(file.getParentFile(), ".access");
			int tries = 0;
			while(newLoc == null || !newLoc.exists()){
				if(tries >= 5){
					log.w("Tried to find parent .access file " + tries + " times.");
				}
				if(newLoc.getParentFile() == null)
					break;
				newLoc = new File(newLoc.getParentFile().getParentFile(), ".access");
				tries++;
			}
			if(newLoc != null && newLoc.exists()){
				log.i("Found parent permissions @ " + newLoc.getPath() + ".");
				FileUtil.write(perms, FileUtil.readString(newLoc).replace("scope dir", "scope none").replace("scope file", "scope none"));
				return canAccess(exchange, file, true);
			}
			return true;
		}
		if(!deletePerms)
			locations.put(dir, perms);
		String permsTxt = FileUtil.readString(perms);
		PermissionScope scope = PermissionScope.NONE;
		String scopeArgs = "";
		boolean isAllowed = false;
		boolean isDenied = false;
		for(String line : permsTxt.split("\n")){
			if(line.trim().startsWith("#"))
				continue;
			String[] words = line.trim().split("\\s+");
			if(words.length < 2){
				log.i("Invalid line in permissions file '" + line + "'.");
				continue;
			}
			String kw = words[0];
			if(kw.equals("scope")){
				try{
					scope = PermissionScope.valueOf(words[1].toUpperCase());
				}catch(Exception e){
					scope = PermissionScope.NONE;
					continue;
				}
				if(scope == PermissionScope.FILE && words.length > 2)
					scopeArgs = words[2];
			}else if(kw.equals("allow")){
				PermissionWord word = PermissionWord.ALL;
				try{
					word = PermissionWord.valueOf(words[1].toUpperCase());
				}catch(Exception e){
					continue;
				}
				isAllowed = fits(exchange, word, words.length > 2 ? words[2] : "", scope, scopeArgs, file);
			}else if(kw.equals("deny")){
				PermissionWord word = PermissionWord.ALL;
				try{
					word = PermissionWord.valueOf(words[1].toUpperCase());
				}catch(Exception e){
					continue;
				}
				isDenied = fits(exchange, word, words.length > 2 ? words[2] : "", scope, scopeArgs, file);
			}
		}
		if(deletePerms)
			perms.delete();
		return !(isDenied && !isAllowed);
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}

	public boolean fits(HttpExchange exchange, PermissionWord word, String wordArgs, PermissionScope scope, String scopeArgs, File file){
		if(scope == PermissionScope.NONE || (scope == PermissionScope.FILE && !file.getName().equals(scopeArgs)))
			return false;
		String ip = exchange.getRemoteAddress().getAddress().getHostAddress();
		switch(word){
		case IP:
			return wordArgs.equals(ip);
		case LOCAL:
			return isLocal(ip);
		case EXTERNAL:
			return !isLocal(ip);
		case ALL:
			return true;
		}
		return false;
	}

	public boolean isSubDir(File parent, File sub){
		return sub.getAbsolutePath().startsWith(parent.getAbsolutePath());
	}

	public boolean isLocal(String ip){
		return ip.equals("127.0.0.1") || ip.equals("::1") || ip.equals("localhost") || ip.equals("0.0.0.0");
	}

	public File getDir(String file){
		File f = new File(file);
		if(f.isDirectory())
			return f;
		return f.getParentFile();
	}
}
